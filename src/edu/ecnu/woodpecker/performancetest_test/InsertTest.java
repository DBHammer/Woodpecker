package edu.ecnu.woodpecker.performancetest_test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.ecnu.woodpecker.constant.FileConstant;
import edu.ecnu.woodpecker.constant.LogLevelConstant;
import edu.ecnu.woodpecker.log.WpLog;
import edu.ecnu.woodpecker.performancetest.Branch;
import edu.ecnu.woodpecker.performancetest.Column;
import edu.ecnu.woodpecker.performancetest.Delete;
import edu.ecnu.woodpecker.performancetest.Dispatcher;
import edu.ecnu.woodpecker.performancetest.Distribution;
import edu.ecnu.woodpecker.performancetest.Filter;
import edu.ecnu.woodpecker.performancetest.Index;
import edu.ecnu.woodpecker.performancetest.Multiple;
import edu.ecnu.woodpecker.performancetest.PrimaryKey;
import edu.ecnu.woodpecker.performancetest.SQL;
import edu.ecnu.woodpecker.performancetest.Select;
import edu.ecnu.woodpecker.performancetest.SelectForUpdate;
import edu.ecnu.woodpecker.performancetest.Table;
import edu.ecnu.woodpecker.performancetest.Transaction;
import edu.ecnu.woodpecker.performancetest.TransactionBlock;
import edu.ecnu.woodpecker.performancetest.UniformDistribution;
import edu.ecnu.woodpecker.performancetest.UniqueDistribution;
import edu.ecnu.woodpecker.performancetest.Update;
import edu.ecnu.woodpecker.performancetest.Workload;
import edu.ecnu.woodpecker.performancetest.WorkloadConfigInfo;

public class InsertTest {
	public static Table geneTable() {
		//(Test-BEGIN)设置一个Table的demo
		List<Column> columns = new ArrayList<>();
		
		Column colSid = new Column("sid", "int");
		Column colSage = new Column("sage",	"int");
		Column colScore = new Column("score", "int");
		Column colShome = new Column("shome", "varchar(70)");
		Column colSdecimal = new Column("ssex", "char");
		
		columns.add(colSid);
		columns.add(colSage);
		columns.add(colScore);
		columns.add(colShome);
		columns.add(colSdecimal);
		
		List<String> pkColumnNames = new ArrayList<String>();
		pkColumnNames.add(colSid.getColumnName());
		boolean autoIncrement = false;
		
		PrimaryKey pKey =  new PrimaryKey(pkColumnNames, autoIncrement);
		
		List<String> idxColumnNames = new ArrayList<>();
		idxColumnNames.add(colSage.getColumnName());
		idxColumnNames.add(colScore.getColumnName());

		Index index = new Index("idxOn_age_score", "student", idxColumnNames);
		List<Index> idxList = new ArrayList<>();
		idxList.add(index);
		Table table = new Table("student", 1000000, columns, pKey, null, idxList);
		return table;
	}
	
	public static Workload selectWorkload() {
		//(Test-BEGIN)设置一个Table的demo
		List<Column> columns = new ArrayList<>();
		
		Column colSid = new Column("sid", "int");
		Column colSage = new Column("sage",	"int");
		Column colScore = new Column("score", "int");
		Column colShome = new Column("shome", "varchar(70)");
		Column colSdecimal = new Column("ssex", "char");
		
		columns.add(colSid);
		columns.add(colSage);
		columns.add(colScore);
		columns.add(colShome);
		columns.add(colSdecimal);
		
		List<String> pkColumnNames = new ArrayList<String>();
		pkColumnNames.add(colSid.getColumnName());
		boolean autoIncrement = false;
		
		PrimaryKey pKey =  new PrimaryKey(pkColumnNames, autoIncrement);
		
		Table table = new Table("student", 1000000, columns, pKey, null, null);
		
		List<String> selectedColumnList = new ArrayList<>();
		selectedColumnList.add("*");
//		selectedColumnList.add(colSid.getColumnName());
//		selectedColumnList.add(colSage.getColumnName());
//		selectedColumnList.add(colShome.getColumnName());
		
		List<Column> filterColumnList = new ArrayList<>();
		filterColumnList.add(colSid);
	
		List<String> relationalOperator = new ArrayList<>();
		relationalOperator.add("=");
		
		List<String> logicalOperator = new ArrayList<>();
		logicalOperator.add("OR");
//		Distribution zipf = new ZIPFDistribution(1, table.getTableSize(), 10, 3);
		Distribution unique = new UniqueDistribution(1, table.getTableSize());
		Filter filter = new Filter(filterColumnList, relationalOperator, null);
		Select select = new Select(table, true, unique, selectedColumnList, filter, null);

		ArrayList<SQL> multSQL = new ArrayList<>();
		multSQL.add(select);
		Multiple multiple = new Multiple(1, 2, multSQL);
		
		List<TransactionBlock> txbList = new ArrayList<>();
		txbList.add(select);
		
		Transaction tx = new Transaction("selectTxn", 1, txbList);
		
		List<Transaction> transactionList = new ArrayList<>();
		transactionList.add(tx);
		
		WpLog.recordLog(LogLevelConstant.INFO, "开始SELECT负载...");
		Workload workload = new Workload(transactionList, 100, 10000, 1);
		return workload;
	}
	
	public static Workload updateWorkload() {
		//(Test-BEGIN)设置一个Table的demo
		List<Column> columns = new ArrayList<>();
		
		Column colSid = new Column("sid", "int");
		Column colSage = new Column("sage",	"int");
		Column colScore = new Column("score", "int");
		Column colShome = new Column("shome", "varchar(70)");
		Column colSdecimal = new Column("ssex", "char");
		
		columns.add(colSid);
		columns.add(colSage);
		columns.add(colScore);
		columns.add(colShome);
		columns.add(colSdecimal);
		
		List<String> pkColumnNames = new ArrayList<String>();
		pkColumnNames.add(colSid.getColumnName());
		boolean autoIncrement = false;
		
		PrimaryKey pKey =  new PrimaryKey(pkColumnNames, autoIncrement);
		
		Table table = new Table("student", 1000000, columns, pKey, null, null);
		
		List<Column> updatedColumnList = new ArrayList<>();
		updatedColumnList.add(colSage);
		updatedColumnList.add(colScore);
		updatedColumnList.add(colShome);
		
		String [] operators = new String[]{"+=", "-=", "="};
		List<String> operatorList = Arrays.asList(operators);
		
		String [] upValues = new String[]{"4", "", "'tzhang'"};
		List<String> updatedValue = Arrays.asList(upValues);
		
		List<Column> filterColumnList = new ArrayList<>();
		filterColumnList.add(colSid);
		List<String> relationalOperator = new ArrayList<>();
		relationalOperator.add("=");
		
//		List<String> logicalOperator = new ArrayList<>();
//		logicalOperator.add("OR");
		Distribution unique = new UniqueDistribution(1, table.getTableSize());
		Filter filter = new Filter(filterColumnList, relationalOperator, null);
		Update update = new Update(table, true, unique, updatedColumnList, operatorList, updatedValue, filter);
		
		List<TransactionBlock> txbList = new ArrayList<>();
		txbList.add(update);
		
		Transaction tx = new Transaction("txn1", 1, txbList);
		
		List<Transaction> transactionList = new ArrayList<>();
		transactionList.add(tx);
		
		WpLog.recordLog(LogLevelConstant.INFO, "开始UPDATE负载...");
		Workload workload = new Workload(transactionList, WorkloadConfigInfo.defaultThreadNumber,
				WorkloadConfigInfo.defaultThreadRunTimes, WorkloadConfigInfo.defaultLoadmachineNumber);
		return workload;
	}
	
	public static Workload deleteWorkload() {
		//(Test-BEGIN)设置一个Table的demo
		List<Column> columns = new ArrayList<>();
		
		Column colSid = new Column("sid", "int");
		Column colSage = new Column("sage",	"int");
		Column colScore = new Column("score", "int");
		Column colShome = new Column("shome", "varchar(70)");
		Column colSdecimal = new Column("ssex", "char");
		
		columns.add(colSid);
		columns.add(colSage);
		columns.add(colScore);
		columns.add(colShome);
		columns.add(colSdecimal);
		
		List<String> pkColumnNames = new ArrayList<String>();
		pkColumnNames.add(colSid.getColumnName());
		boolean autoIncrement = false;
		
		PrimaryKey pKey =  new PrimaryKey(pkColumnNames, autoIncrement);
		
		Table table = new Table("student", 100000, columns, pKey, null, null);
		
		List<Column> filterColumnList = new ArrayList<>();
		filterColumnList.add(colSid);
		List<String> relationalOperator = new ArrayList<>();
		relationalOperator.add("=");
		
//		List<String> logicalOperator = new ArrayList<>();
//		logicalOperator.add("OR");
		
		Distribution unique = new UniqueDistribution(1, table.getTableSize());
		Filter filter = new Filter(filterColumnList, relationalOperator, null);
		Delete delete = new Delete(table, true, unique, filter);
		
		List<TransactionBlock> txbList = new ArrayList<>();
		txbList.add(delete);
		
		Transaction tx = new Transaction("deleteTxn", 1, txbList);
		
		List<Transaction> transactionList = new ArrayList<>();
		transactionList.add(tx);
		
		WpLog.recordLog(LogLevelConstant.INFO, "开始DELETE负载...");
		Workload workload = new Workload(transactionList, WorkloadConfigInfo.defaultThreadNumber,
				WorkloadConfigInfo.defaultThreadRunTimes, WorkloadConfigInfo.defaultLoadmachineNumber);
		return workload;
	}
	
	public static Workload hybridWorkload() {
		//(Test-BEGIN)设置一个Table的demo
		List<Column> columns = new ArrayList<>();
		
		Column colSid = new Column("sid", "int");
		Column colSage = new Column("sage",	"int");
		Column colScore = new Column("score", "int");
		Column colShome = new Column("shome", "varchar(70)");
		Column colSdecimal = new Column("ssex", "char");
		
		columns.add(colSid);
		columns.add(colSage);
		columns.add(colScore);
		columns.add(colShome);
		columns.add(colSdecimal);
		
		List<String> pkColumnNames = new ArrayList<String>();
		pkColumnNames.add(colSid.getColumnName());
		boolean autoIncrement = false;
		
		PrimaryKey pKey =  new PrimaryKey(pkColumnNames, autoIncrement);
		
		Table table = new Table("student", 1000000, columns, pKey, null, null);
		
		List<String> selectedColumnList = new ArrayList<>();
//	selectedColumnList.add("*");
		selectedColumnList.add(colSid.getColumnName());
		selectedColumnList.add(colSage.getColumnName());
		selectedColumnList.add(colShome.getColumnName());
		
		List<Column> selectFilterColumns = new ArrayList<>();
		selectFilterColumns.add(colSid);
	
		List<String> selectOperator = new ArrayList<>();
		selectOperator.add("=");
		
	//	List<String> logicalOperator = new ArrayList<>();
	//	logicalOperator.add("OR");
		
		Filter selectFilter = new Filter(selectFilterColumns, selectOperator, null);
		Distribution uniform = new UniformDistribution(1, table.getTableSize());
		Select select = new Select(table, true, uniform, selectedColumnList, selectFilter, null);	
		
		List<Column> updatedColumnList = new ArrayList<>();
		updatedColumnList.add(colSage);
		updatedColumnList.add(colScore);
		updatedColumnList.add(colShome);
		
		List<String> updateOperatorList = Arrays.asList(new String[]{"=", "=", "="});
		List<String> updatedValue = Arrays.asList(new String[]{"25", "", "'tzhang4'"});
		List<String> updatedValue2 = Arrays.asList(new String[]{"22", "", "'vc2'"});
		List<Column> updateFilterColumns = new ArrayList<>();
		updateFilterColumns.add(colSid);
		List<String> updateFilterOperator = Arrays.asList(new String[] {"="});
		
//		List<String> logicalOperator = new ArrayList<>();
//		logicalOperator.add("OR");
		
		Filter updateFilter = new Filter(updateFilterColumns, updateFilterOperator, null);
		Distribution unique = new UniqueDistribution(1, table.getTableSize());
		Update update = new Update(table, true, unique, updatedColumnList, updateOperatorList, updatedValue, updateFilter);
		
		Update update2 = new Update(table, true, unique, updatedColumnList, updateOperatorList, updatedValue2, updateFilter);
		
		Float[] branchRatios = new Float[] {(float) 1.0};
		ArrayList<SQL> sqlList1 = new ArrayList<>();
		sqlList1.add(update);
		ArrayList<SQL> sqlList2 = new ArrayList<>();
		sqlList2.add(update2);
		List<ArrayList<SQL>> branchBlockList = new ArrayList<>();
		branchBlockList.add(sqlList1);
//		branchBlockList.add(sqlList2);
		
		Branch branch = new Branch(branchRatios, branchBlockList);
		Multiple multiple = new Multiple(1, 1, sqlList1);
		
//		List<TransactionBlock> txbList = new ArrayList<>();
//		txbList.add(branch);
//		txbList.add(update);
//		txbList.add(select);
		List<TransactionBlock> txbList1 = new ArrayList<>();
//		txbList1.add(select);
		txbList1.add(branch);
		
		List<TransactionBlock> txbList2 = new ArrayList<>();
		txbList2.add(update);
		
		Transaction txn1 = new Transaction("txn1", (float) 1.0, txbList1);
		Transaction txn2 = new Transaction("txn2", (float) 0.6, txbList2);
//		
//		List<Transaction> transactionList = new ArrayList<>();
//		transactionList.add(txn2);
		
		List<Transaction> transactionList = new ArrayList<>();
		transactionList.add(txn1);
//		transactionList.add(txn2);
		
		WpLog.recordLog(LogLevelConstant.INFO, "开始Branch负载...");
		Workload workload = new Workload(transactionList, WorkloadConfigInfo.defaultThreadNumber,
				WorkloadConfigInfo.defaultThreadRunTimes, WorkloadConfigInfo.defaultLoadmachineNumber);
		return workload;
	}
	
	public static void main(String[] args) {
		try {
			System.out.println(new File(FileConstant.DEF_STRESS_TEST_DIR).getCanonicalPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		//设置默认配置文件路径，且首先加载配置文件 
//		String configPath = System.getProperty("user.dir") + FileConstant.DEF_STRESS_TEST_DIR;
//		String configName = FileConstant.DEF_STRESS_TEST_CONFIG_FILE;
//		WorkloadConfigInfo.initConfigInfo(configPath, configName);
//		
//		List<Table> tableList = new ArrayList<>();
//		List<Workload> workloadList = new ArrayList<>();
//		
//        tableList.add(geneTable());
////		workloadList.add(selectWorkload());
////		workloadList.add(updateWorkload());
////		workloadList.add(deleteWorkload());
////		workloadList.add(hybridWorkload());
//		
//		//新建Dispatcher类来模拟workload的执行
//		Dispatcher.execute(tableList, workloadList);
	}
}
