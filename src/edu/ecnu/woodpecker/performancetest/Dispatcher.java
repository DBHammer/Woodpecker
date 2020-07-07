package edu.ecnu.woodpecker.performancetest;


import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.alibaba.druid.sql.dialect.oracle.ast.clause.ModelClause.ReturnRowsClause;

import edu.ecnu.woodpecker.constant.FileConstant;
import edu.ecnu.woodpecker.constant.LogLevelConstant;
import edu.ecnu.woodpecker.constant.SignConstant;
import edu.ecnu.woodpecker.log.WpLog;
import edu.ecnu.woodpecker.util.Util;

/**
 * 分发器类
 * @author 59173
 *
 */
public class Dispatcher {
	public static void main(String[] args) {
		// purpose null
	}

	/**
	 * 这个execute函数中的tableList和workloadList可能有以下四种情况：
	 * 1.tableList和workloadList均为空，这种情况do nothing
	 * 2.tableList为空，workloadList不为空，这种情可能有多次TXN_LOADING
	 * 3.tableList不为空，workloadList为空，这种情况即为导入数据，可能有多张表
	 * 4.tableList和workloadList均不为空，这种情况即导入数据后再执行负载
	 * @param tableList
	 * @param workloadList
	 */
	public static void execute(List<Table> tableList, List<Workload> workloadList) {
		if (tableList.size() == 0 && workloadList.size() == 0) //第一种情况
			WpLog.recordLog(LogLevelConstant.INFO, "tableList and workloadList are null, do nothing!");
		else { // 第二、三、四种情况
			//设置默认配置文件路径，且首先加载配置文件
			String configPath = null;
			try {
				configPath = new File(FileConstant.DEF_STRESS_TEST_DIR).getCanonicalPath() + File.separator;
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			String configName = FileConstant.DEF_STRESS_TEST_CONFIG_FILE;
			WorkloadConfigInfo.initConfigInfo(configPath, configName);
			initConfigurationInfo();

			// 启动Dispatcher端的Server
			DispatcherNetworkServerHandler.main(null);

			Dispatcher.tableList = tableList;
			Dispatcher.workloadList = workloadList;
			Dispatcher.allTypeWorkloadNumber = tableList.size() + workloadList.size();

			workloadIPList = new ArrayList<>();
			workloadPortList = new ArrayList<>();
			List<String> dbMachines = WorkloadConfigInfo.dbMachines;
			String[] entry;
			for (int i = 0; i < dbMachines.size(); ++i) {
				entry = dbMachines.get(i).split(SignConstant.COLON_STR);
				workloadIPList.add(entry[0]);
				workloadPortList.add(Integer.valueOf(entry[1]));
				try {
					// 将配置文件拷贝到负载机并启动Server.jar
					copyToServer(entry[0]);
					launchJar(entry[0]);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			// 将tableList转换为tableWorkload并添加到tableWorkloadList中
			if (tableList != null && tableList.size() != 0) {
				preprocess();
			}
			// 开启Dispatcher端的Client
			client = new NetworkClient(workloadIPList, workloadPortList, new DispatcherNetworkClientHandler());

			// 使用netty将tableWorkload分发到负载机上
			if (tableList != null && tableList.size() != 0) {
				for (int i = 0; i < tableWorkloadList.size(); ++i) {
					WpLog.recordLog(LogLevelConstant.INFO, "Sending NO." + (i + 1) + " tableWorkload...");
					Workload workload = tableWorkloadList.get(i);
					for (int j = 0; j < workloadIPList.size(); ++j) {
						client.send(workload, j);
					}
					while(!Dispatcher.isOneWorkloadFinished) {
						// sleep for serval seconds
						try {
							Thread.sleep(FileConstant.DEF_SEND_TIME * 1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					Dispatcher.isOneWorkloadFinished = false;
				}
			}

			// 将workloadList中的负载逐个发送到负载机上
			if (workloadList != null && workloadList.size() != 0) {
				for (int i = 0; i < workloadList.size(); ++i) {
					WpLog.recordLog(LogLevelConstant.INFO, "Sending NO." + (i + 1) + " transaction workload...");
					Workload workload = workloadList.get(i);
					for (int j = 0; j < workloadIPList.size(); ++j) {
						client.send(workload, j);
					}
					while(!Dispatcher.isOneWorkloadFinished) {
						// sleep for serval seconds
						try {
							Thread.sleep(FileConstant.DEF_SEND_TIME * 1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					Dispatcher.isOneWorkloadFinished = false;
				}
			}
		}
	}


	/**
	 * 负载机的账户和密码，SSH连接端口号
	 */
	private static String serverUser = null;
	private static String serverPassword = null;
	private static  int SSHPort;

	/**
	 *用于数据库连接的一些基本信息，新建Table对象的时候需要从WorkloadConfig那里
	 *读取各个项的值填充到此处
	 */
	private static String DBMS = null;
	private static String dbInstance = null;
	private static String dbUser = null;
	private static String dbPassword = null;
	//	private static List<String> dbMachines;
	private static List<String> dbEntries;
	private static int defaultThreadNumber;
	//	private static int defaultThreadRunTimes;
	private static int defaultLoadMachineNumber;

	private static List<String> workloadIPList = null;
	private static List<Integer> workloadPortList = null;

	private static List<Table> tableList = null;
	private static List<Workload> tableWorkloadList = null;
	private static List<Workload> workloadList = null;

	private static NetworkClient client = null;

	//标志某个workload是否完成
	public static boolean isOneWorkloadFinished = false;
	//记录所有负载类型的总数，包括tableWorkload和事务的workload
	public static int allTypeWorkloadNumber = 0;

	public Dispatcher() {
		initConfigurationInfo();
	}

	public Dispatcher(List<Table> tableList, List<Workload> workloadList) {
		initConfigurationInfo();
		Dispatcher.tableList = tableList;
		Dispatcher.workloadList = workloadList;
	}

	public static void initConfigurationInfo() {
		Dispatcher.serverUser = WorkloadConfigInfo.serverUser;
		Dispatcher.serverPassword = WorkloadConfigInfo.serverPassword;
		Dispatcher.SSHPort = WorkloadConfigInfo.SSHPort;
		Dispatcher.DBMS = WorkloadConfigInfo.DBMS;
		Dispatcher.dbInstance = WorkloadConfigInfo.dbInstance;
		Dispatcher.dbUser = WorkloadConfigInfo.dbUser;
		Dispatcher.dbPassword = WorkloadConfigInfo.dbPassword;
//		Dispatcher.dbMachines = WorkloadConfigInfo.dbMachines;
		Dispatcher.dbEntries = WorkloadConfigInfo.dbEntries;
		Dispatcher.defaultThreadNumber = WorkloadConfigInfo.defaultThreadNumber;
//		Dispatcher.defaultThreadRunTimes = WorkloadConfigInfo.defaultThreadRunTimes;
		Dispatcher.defaultLoadMachineNumber = WorkloadConfigInfo.defaultLoadmachineNumber;
	}

	/**
	 * 将系统配置信息以及压测模块的配置信息(包括在集群运行的jar包)均发送到负载机上
	 * @param IP
	 * @throws Exception
	 */
	public static void copyToServer(String IP) throws Exception {
		String command = "rm ~/performancetest -r;";
		WpLog.recordLog(LogLevelConstant.INFO, "Removing old folder on %s", IP);
		Util.exec(IP, serverUser, serverPassword, SSHPort, command);

		String testEnvConfPath = new File(FileConstant.DEF_TEST_ENV_CONF_PATH).getCanonicalPath();
		String stressTestDir = new File(FileConstant.DEF_STRESS_TEST_DIR).getCanonicalPath();
		String testEnvConfPathDestDir = String.format("/home/%s/", serverUser) +
				FileConstant.DEF_STRESS_TEST_DIR_ON_SERVER + FileConstant.DEF_GLOBAL_CONF_DIR_NAME_ON_SERVER;
		String stressTestDestDir = String.format("/home/%s/", serverUser) +
				FileConstant.DEF_STRESS_TEST_DIR_ON_SERVER;

		// 运行woodpecker的OS是Linux
		if (System.getProperty("os.name").matches("Linux.*")) {
			throw new Exception("OS is Linux, not supported now!");
		}
		else if (System.getProperty("os.name").matches("Windows.*"))
			WpLog.recordLog(LogLevelConstant.INFO, "Woodpecker-OS is Windows");
		else if (System.getProperty("os.name").matches("Mac OS X"))
			WpLog.recordLog(LogLevelConstant.INFO, "Woodpecker-OS is Mac");
		WpLog.recordLog(LogLevelConstant.INFO, "Uploading file to %s...", IP);

		SFTPConnection.connect(serverUser, serverPassword, IP, SSHPort);
		SFTPConnection.execCmd("mkdir -p ~/" + FileConstant.DEF_STRESS_TEST_DIR_ON_SERVER
				+ FileConstant.DEF_GLOBAL_CONF_DIR_NAME_ON_SERVER);
		// 上传系统配置文件
		SFTPConnection.upload(testEnvConfPathDestDir, testEnvConfPath);
		// 上传压测相关的配置文件
		SFTPConnection.upload(stressTestDestDir, stressTestDir);
		SFTPConnection.close();
		WpLog.recordLog(LogLevelConstant.INFO, "Uploading findished!", IP);
	}

	/**
	 * 运行指定IP负载机上的jar文件
	 * @throws Exception
	 */
	public static void launchJar(String IP) throws Exception {
		String command = "cd ~/performancetest/st/; java -jar Server.jar > nohup.out 2>&1 &";
		WpLog.recordLog(LogLevelConstant.INFO, "Running Server.jar on %s", IP);
		Util.exec(IP, serverUser, serverPassword, SSHPort, command);
		//获取jar运行的进程号
		command = "ps ux | grep Server.jar | awk '{print $2}'";
		WpLog.recordLog(LogLevelConstant.INFO, "Getting PID from %s...", IP);
		String result = Util.exec(IP, serverUser, serverPassword, SSHPort, command);
		WpLog.recordLog(LogLevelConstant.INFO, "PID:%s", result.subSequence(0, result.indexOf("\n")));
	}

	// 关闭各个负载机上的Server进程
	public static void closeProcess(String IP) throws Exception {
		String command = "ps ux | grep Server.jar | kill `awk '{print $2}'`";
		Util.exec(IP, WorkloadConfigInfo.serverUser, WorkloadConfigInfo.serverPassword, WorkloadConfigInfo.SSHPort, command);
	}

	// 创建表，若有索引，则同时创建
	public static boolean creaetTable(Table table) {

		int entryIndex = new Random().nextInt(dbEntries.size());
		String[] entries = dbEntries.get(entryIndex).split(SignConstant.COLON_STR);
		String IP = entries[0];
		int port = Integer.valueOf(entries[1]);
		Connection conn = new DBConnection(IP, port, dbInstance, dbUser, dbPassword, DBMS)
				.getDBConnection();
		Statement stmt = null;

		try {
			stmt = conn.createStatement();
			stmt.execute(table.geneDropTableSQL());
			stmt.execute(table.geneCreateTableSQL());
			// 创建索引
			if (table.getIndexList() != null && table.getIndexList().size() != 0) {
				for (Index index : table.getIndexList())
					stmt.execute(index.geneCreateIndexSQL());
			}
		}
		catch (Exception e) {
			WpLog.recordLog(LogLevelConstant.INFO, "创建" + table.getTableName() + "表或索引失败!");
			e.printStackTrace();
			return false;
		}

		try {
			stmt.close();
			conn.close();
		} catch (Exception e) {
			WpLog.recordLog(LogLevelConstant.INFO, "Closing connection failed!");
			return false;
		}
		return true;
	}

	/**
	 * CLEAR_TBL删除参数中的所有表，表的索引也会一起删除
	 * @param tableList
	 */
	public static void clearTables(List<Table> tableList) {
		//设置默认配置文件路径，且首先加载配置文件
		String configPath = null;
		try {
			configPath = new File(FileConstant.DEF_STRESS_TEST_DIR).getCanonicalPath() + File.separator;
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		String configName = FileConstant.DEF_STRESS_TEST_CONFIG_FILE;
		WorkloadConfigInfo.initConfigInfo(configPath, configName);
		initConfigurationInfo();

		System.out.println(dbEntries.toString());
		int entryIndex = new Random().nextInt(dbEntries.size());
		String[] entries = dbEntries.get(entryIndex).split(SignConstant.COLON_STR);
		String IP = entries[0];
		int port = Integer.valueOf(entries[1]);
		Connection conn = new DBConnection(IP, port, dbInstance, dbUser, dbPassword, DBMS)
				.getDBConnection();
		Statement stmt = null;
		List<String> failedTableList = new ArrayList<>();

		for (Table table : tableList) {
			String dropTableSQL = table.geneDropTableSQL();
			WpLog.recordLog(LogLevelConstant.INFO, "Drop table SQL:%s", dropTableSQL);
			try {
				stmt = conn.createStatement();
				stmt.execute(dropTableSQL);
			}
			catch (Exception e) {
				failedTableList.add(table.getTableName());
				WpLog.recordLog(LogLevelConstant.INFO, "Dropping" + table.getTableName() + " failed!");
			}
		}

		try {
			stmt.close();
			conn.close();
		} catch (Exception e) {
			WpLog.recordLog(LogLevelConstant.INFO, "Closing connection failed!");
		}

		if (failedTableList.size() != 0) {
			WpLog.recordLog(LogLevelConstant.INFO, "Failed table: " + failedTableList.toString());
		} else {
			WpLog.recordLog(LogLevelConstant.INFO, "All table are cleared successfully!");
		}
	}

	/**
	 * 将table转换为workload对象
	 * @param table
	 * @return Workload
	 */
	public static Workload transformTableToWorkload(Table table) {
		Distribution unique = new UniqueDistribution(1, table.getTableSize());
		Insert insert;
		// 无论Insert是否预编译，distribution都可以根据主键是否自增设置为null或unique
		if (table.getPrimaryKey().isAutoIncrement())
			insert =  new Insert(table, true, unique);
		else
			insert =  new Insert(table, true, unique);

		List<TransactionBlock> txbList = new ArrayList<>();
		txbList.add(insert);

		Transaction tx = new Transaction("createStudentTable",1, txbList);

		List<Transaction> transactionList = new ArrayList<>();
		transactionList.add(tx);
		// 每一台负载机插入的tuple总数和每台负载机开启的线程数。
		int tuplesOnMachine = (int)Math.ceil(table.getTableSize() * 1.0 / defaultLoadMachineNumber);
		int threadsOnMachine = (int)Math.ceil(defaultThreadNumber * 1.0 / defaultLoadMachineNumber);

		// 该workload的线程数量和每个线程运行的次数
		int threadNumber, threadRunTimes;
		if (tuplesOnMachine > threadsOnMachine && (tuplesOnMachine * 1.0 / threadsOnMachine) < 2) {
			threadNumber = tuplesOnMachine;
			threadRunTimes = 1;
		} else {
			threadNumber = Math.min(threadsOnMachine, tuplesOnMachine);
			int bigger = Math.max(threadNumber, tuplesOnMachine);
			threadRunTimes = (int)Math.ceil(bigger * 1.0 / threadNumber);

//			threadNumber = (threadsOnMachine > tuplesOnMachine) ? tuplesOnMachine : threadsOnMachine;
//			if (threadNumber == tuplesOnMachine)
//				threadRunTimes = 1;
//			else
//				threadRunTimes = (int)Math.ceil(tuplesOnMachine * 1.0 / threadsOnMachine);
		}
		WpLog.recordLog(LogLevelConstant.INFO, "Thread number:%d", threadNumber);
		WpLog.recordLog(LogLevelConstant.INFO, "Thread run times:%d", threadRunTimes);
		WpLog.recordLog(LogLevelConstant.INFO, "Default workload machine number:%d", defaultLoadMachineNumber);
		Workload workload = new Workload(transactionList, threadNumber, threadRunTimes, defaultLoadMachineNumber);
		return workload;
	}

	/**
	 * 创建表并将table转换为workload
	 */
	public static void preprocess() {
		tableWorkloadList = new ArrayList<>();
		for (Table table : tableList) {
			WpLog.recordLog(LogLevelConstant.INFO, "Creating table %s......", table.getTableName());
			if (creaetTable(table))
				WpLog.recordLog(LogLevelConstant.INFO, "Table %s was created!", table.getTableName());
			else
				System.exit(0);
			WpLog.recordLog(LogLevelConstant.INFO, "Transforming table %s to workload......", table.getTableName());
			tableWorkloadList.add(transformTableToWorkload(table));
			WpLog.recordLog(LogLevelConstant.INFO, "Transformed table %s to workload!", table.getTableName());
		}
	}

	public List<Table> getTableList() {
		return tableList;
	}

	public void setTableList(List<Table> tableList) {
		Dispatcher.tableList = tableList;
	}

	public List<Workload> getTableWorkloadList() {
		return tableWorkloadList;
	}

	public void setTableWorkloadList(List<Workload> tableWorkloadList) {
		Dispatcher.tableWorkloadList = tableWorkloadList;
	}

	public List<Workload> getWorkloadList() {
		return workloadList;
	}

	public void setWorkloadList(List<Workload> workloadList) {
		Dispatcher.workloadList = workloadList;
	}
}
