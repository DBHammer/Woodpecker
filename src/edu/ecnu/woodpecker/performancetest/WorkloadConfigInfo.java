package edu.ecnu.woodpecker.performancetest;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import edu.ecnu.woodpecker.constant.FileConstant;
import edu.ecnu.woodpecker.constant.LogLevelConstant;
import edu.ecnu.woodpecker.constant.SignConstant;
import edu.ecnu.woodpecker.log.WpLog;

public class WorkloadConfigInfo implements Serializable {
	/**
	 * 分发器的IP地址和Server端口号;ip:port
	 */
	public static String dispatcher;
	
	/**
	 * 负载机的账户和密码，SSH连接端口号
	 */
	private static final long serialVersionUID = 1L;
	public static String serverUser = null;
	public static String serverPassword = null;
	public static int SSHPort;
	
    /**
     * 基本信息项
     */
	public static String DBMS = null;
	public static String dbInstance = null;
	public static String dbUser = null;
	public static String dbPassword = null;
	public static List<String> dbMachines;
	public static List<String> dbEntries;
	
	public static int defaultThreadNumber;
	public static int defaultThreadRunTimes;
	public static int defaultLoadmachineNumber;
	
	//测试初始化配置文件
	public static void main(String[] args) 
	{
		String configPath = System.getProperty("user.dir") + "\\st\\";
		String configName = "default";
//		String configName = "mysql";
		WorkloadConfigInfo.initConfigInfo(configPath, configName);
		System.out.println(DBMS);
		System.out.println(dbInstance);
		System.out.println(dbUser);
		System.out.println(dbPassword);
		System.out.println(defaultThreadNumber);
		System.out.println(defaultThreadRunTimes);
		System.out.println(defaultLoadmachineNumber);
		for (String s : dbMachines)
			System.out.println(s);
		for (String s : dbEntries)
			System.out.println(s);
	}
	
	public static void initConfigInfo(String configPath, String configName)
	{
		try(BufferedReader br = new BufferedReader(new InputStreamReader
				(new FileInputStream(configPath + configName + 
		FileConstant.WORKLOAD_CONFIG_FILE_SUFFIX), FileConstant.UTF_8)))
		{
			String line = null;
            StringBuilder stringBuilder = new StringBuilder();
            
            while ((line = br.readLine()) != null)
            {
                //空行或者注释行
                if (line.matches("(\\s*#.*)|(\\s*)"))
                    continue;
                //获取配置项对应的函数名字
                stringBuilder.append("set").append(line.substring(0, 
                		line.indexOf(SignConstant.ASSIGNMENT_CHAR)).trim());
                stringBuilder.setCharAt(3, Character.toUpperCase(stringBuilder.charAt(3)));
                //反射使用的方法名
                String methodName = stringBuilder.toString();
                //获取配置项的值ֵ
                String confValue = line.substring
                		(line.indexOf(SignConstant.ASSIGNMENT_CHAR) + 1).trim();
                int index = confValue.indexOf(SignConstant.SHARP_CHAR);
                confValue = (index == -1) ? confValue : confValue.substring(0, index).trim();
                stringBuilder.delete(0, stringBuilder.length());
                //使用反射
                Method method;
                switch (methodName) 
                {
                	//int型变量
                	case "setSSHPort":
					case "setDefaultThreadNumber":
					case "setDefaultThreadRunTimes":
					case "setDefaultLoadmachineNumber":
					case "setWorkloadMachineServerPort":
		                method = WorkloadConfigInfo.class.getMethod(methodName, int.class);
		                method.invoke(null, Integer.valueOf(confValue));
						break;
					//集合型变量
					case "setDbMachines":
					case "setDbEntries":
						List<String> tempList = new ArrayList<>();
						String[] temp = confValue.split(";");
						for (String s : temp)
							tempList.add(s.trim());
						method = WorkloadConfigInfo.class.getMethod(methodName, List.class);
		                method.invoke(null, tempList);
						break;
					//其他的字符串变量
					default:
						method = WorkloadConfigInfo.class.getMethod(methodName, String.class);
		                method.invoke(null, confValue);
						break;
				}
            }
		}
		catch (Exception e) 
		{
			WpLog.recordLog(LogLevelConstant.DEBUG, "Can't initialize configuration file!");
			e.printStackTrace();
		}
	}

	public static String getDispatcher() {
		return dispatcher;
	}

	public static void setDispatcher(String dispatcher) {
		WorkloadConfigInfo.dispatcher = dispatcher;
	}

	public static String getServerUser() {
		return serverUser;
	}

	public static void setServerUser(String serverUser) {
		WorkloadConfigInfo.serverUser = serverUser;
	}

	public static String getServerPassword() {
		return serverPassword;
	}

	public static void setServerPassword(String serverPassword) {
		WorkloadConfigInfo.serverPassword = serverPassword;
	}

	public static int getSSHPort() {
		return SSHPort;
	}

	public static void setSSHPort(int sSHPort) {
		SSHPort = sSHPort;
	}

	public static String getDBMS() {
		return DBMS;
	}

	public static void setDBMS(String dBMS) {
		DBMS = dBMS;
	}

	public static String getDbInstance() {
		return dbInstance;
	}

	public static void setDbInstance(String dbInstance) {
		WorkloadConfigInfo.dbInstance = dbInstance;
	}

	public static String getDbUser() {
		return dbUser;
	}

	public static void setDbUser(String dbUser) {
		WorkloadConfigInfo.dbUser = dbUser;
	}

	public static String getDbPassword() {
		return dbPassword;
	}

	public static void setDbPassword(String dbPassword) {
		WorkloadConfigInfo.dbPassword = dbPassword;
	}

	public static List<String> getDbMachines() {
		return dbMachines;
	}

	public static void setDbMachines(List<String> dbMachines) {
		WorkloadConfigInfo.dbMachines = dbMachines;
	}

	public static List<String> getDbEntries() {
		return dbEntries;
	}

	public static void setDbEntries(List<String> dbEntries) {
		WorkloadConfigInfo.dbEntries = dbEntries;
	}

	public static int getDefaultThreadNumber() {
		return defaultThreadNumber;
	}

	public static void setDefaultThreadNumber(int defaultThreadNumber) {
		WorkloadConfigInfo.defaultThreadNumber = defaultThreadNumber;
	}

	public static int getDefaultThreadRunTimes() {
		return defaultThreadRunTimes;
	}

	public static void setDefaultThreadRunTimes(int defaultThreadRunTimes) {
		WorkloadConfigInfo.defaultThreadRunTimes = defaultThreadRunTimes;
	}

	public static int getDefaultLoadmachineNumber() {
		return defaultLoadmachineNumber;
	}

	public static void setDefaultLoadmachineNumber(int defaultLoadmachineNumber) {
		WorkloadConfigInfo.defaultLoadmachineNumber = defaultLoadmachineNumber;
	}



}
