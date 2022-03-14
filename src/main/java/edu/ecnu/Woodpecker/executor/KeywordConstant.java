package edu.ecnu.Woodpecker.executor;

import java.util.HashMap;
import java.util.Map;

import edu.ecnu.Woodpecker.constant.SignConstant;

/**
 * The class contains keyword constant and the map between keyword and handle class name
 * Attention: when add new keyword, must add here
 * 该类包含关键字常量和关键字与句柄类名之间的映射
 * 注意：添加新关键字时，必须在此处添加
 */
public class KeywordConstant
{
    // Keyword constant
    public static final String GET_CONNECTION = "get_conn";//
    public static final String GET_STATEMENT = "get_stat";//
    public static final String GET_PREPARED_STATEMENT = "get_pstat";
    public static final String GET_CALLABLE_STATEMENT = "get_cstat";
    public static final String SQL = "sql";//
    public static final String PSQL = "psql";
    public static final String CSQL = "csql";
    public static final String TRANSACTION = "tx";//
    public static final String BATCH_SQL = "batch_sql";//

    public static final String VERIFY = "verify";//
    public static final String SLEEP = "sleep";

    public static final String IMPORT_DBI = "import_dbi";
    public static final String CLEAR_DBI = "clear_dbi";
    public static final String IMPORT_IRS = "import_irs";//
    public static final String DEFINE_PROCEDURE = "def_proc";
    public static final String CONTAIN = "contain";
    public static final String ERROR = "error";
    public static final String CALCULATE = "CAL"; // upper case in .mr file
    public static final String INDEX_OF = "index_of";

    public static final String CPU_EXCEPTION = "cpu";
    public static final String MEMORY_EXCEPTION = "mem";
    public static final String DISK_EXCEPTION = "disk";
    public static final String NETWORK_EXCEPTION = "net";
    public static final String FILE_EXCEPTION = "file";

    public static final String PARALL = "parall";//
    public static final String OLTPBENCH = "oltpbench";//ysh added

    public static final String BENCHMARKSQL = "benchmarksql";

    public static final String MYSQL_TEST_FRAMEWORK = "mysql_test_framework";

    //add sysbench
    public static final String START_BENCHMARK = "start_benchmark";
    public static final String END_BENCHMARK = "end_benchmark";
    public static final String SYSBENCH_OLTP = "sysbench_oltp";
    public static final String SYSBENCH_CPU = "sysbench_cpu";
    public static final String SYSBENCH_IO = "sysbench_io";
    public static final String SYSBENCH_MEM = "sysbench_mem";
    public static final String SYSBENCH_THREADS = "sysbench_threads";
    public static final String SYSBENCH_MUTEX = "sysbench_mutex";
    public static final String TOUCHSTONE = "touchstone";
    public static final String TPCH = "tpch";
    public static final String NMON = "nmon";

    public static final String INIT_DATABENCH_CONFIG = "init_databench_config";
    public static final String START_DATABENCH = "start_databench";


    // All keyword class must locate in this directory
    public static final String CLASS_PREFIX = "edu.ecnu.Woodpecker.executor.keyword";

    /**
     * map key is keyword, value is corresponding class name
     */
    public static Map<String, String> keywordClassMap = new HashMap<>(30);

    // manual add when you need to add new keyword
    //所有的keyword及其对应的方法
    static
    {
        keywordClassMap.put(GET_CONNECTION, CLASS_PREFIX + SignConstant.DOT_CHAR + "ConnectionGetter");
        keywordClassMap.put(GET_STATEMENT, CLASS_PREFIX + SignConstant.DOT_CHAR + "StatementGetter");
        keywordClassMap.put(GET_PREPARED_STATEMENT, CLASS_PREFIX + SignConstant.DOT_CHAR + "PreparedStatementGetter");
        keywordClassMap.put(GET_CALLABLE_STATEMENT, CLASS_PREFIX + SignConstant.DOT_CHAR + "CallableStatementGetter");

        keywordClassMap.put(SQL, CLASS_PREFIX + SignConstant.DOT_CHAR + "SQLProcessor");
        keywordClassMap.put(PSQL, CLASS_PREFIX + SignConstant.DOT_CHAR + "PreparedSQLProcessor");
        keywordClassMap.put(CSQL, CLASS_PREFIX + SignConstant.DOT_CHAR + "CallableSQLProcessor");

        keywordClassMap.put(TRANSACTION, CLASS_PREFIX + SignConstant.DOT_CHAR + "TransactionProcessor");
        keywordClassMap.put(BATCH_SQL, CLASS_PREFIX + SignConstant.DOT_CHAR + "BatchSQLProcessor");


        keywordClassMap.put(VERIFY, CLASS_PREFIX + SignConstant.DOT_CHAR + "VerifyProcessor");
        keywordClassMap.put(SLEEP, CLASS_PREFIX + SignConstant.DOT_CHAR + "SleepProcessor");
        keywordClassMap.put(ERROR, CLASS_PREFIX + SignConstant.DOT_CHAR + "ErrorProcessor");

        keywordClassMap.put(IMPORT_DBI, CLASS_PREFIX + SignConstant.DOT_CHAR + "DBIImporter");
        keywordClassMap.put(CLEAR_DBI, CLASS_PREFIX + SignConstant.DOT_CHAR + "DBICleaner");
        keywordClassMap.put(IMPORT_IRS, CLASS_PREFIX + SignConstant.DOT_CHAR + "IdealResultSetImporter");

        keywordClassMap.put(DEFINE_PROCEDURE, CLASS_PREFIX + SignConstant.DOT_CHAR + "ProcedureDefiner");
        keywordClassMap.put(CONTAIN, CLASS_PREFIX + SignConstant.DOT_CHAR + "ContainProcessor");
        keywordClassMap.put(CALCULATE, CLASS_PREFIX + SignConstant.DOT_CHAR + "CalculateProcessor");
        keywordClassMap.put(INDEX_OF, CLASS_PREFIX + SignConstant.DOT_CHAR + "IndexOfProcessor");
        
        keywordClassMap.put(CPU_EXCEPTION, CLASS_PREFIX + SignConstant.DOT_CHAR + "CPUExceptionProcessor");
        keywordClassMap.put(MEMORY_EXCEPTION, CLASS_PREFIX + SignConstant.DOT_CHAR + "MemoryExceptionProcessor");
        keywordClassMap.put(DISK_EXCEPTION, CLASS_PREFIX + SignConstant.DOT_CHAR + "DiskExceptionProcessor");
        keywordClassMap.put(NETWORK_EXCEPTION, CLASS_PREFIX + SignConstant.DOT_CHAR + "NetworkExceptionProcessor");
        keywordClassMap.put(FILE_EXCEPTION, CLASS_PREFIX + SignConstant.DOT_CHAR + "FILEExceptionProcessor");
        
        keywordClassMap.put(PARALL, CLASS_PREFIX + SignConstant.DOT_CHAR + "ParallelProcessor");

        keywordClassMap.put(OLTPBENCH, CLASS_PREFIX + SignConstant.DOT_CHAR + "OltpbenchProcessor");
        keywordClassMap.put(BENCHMARKSQL, CLASS_PREFIX + SignConstant.DOT_CHAR + "BenchmarkSQLProcessor");
        keywordClassMap.put(MYSQL_TEST_FRAMEWORK, CLASS_PREFIX + SignConstant.DOT_CHAR + "MySQLTestFrameworkProcessor");
        keywordClassMap.put(SYSBENCH_OLTP, CLASS_PREFIX + SignConstant.DOT_CHAR + "SysbenchOLTPProcessor");

        keywordClassMap.put(START_BENCHMARK, CLASS_PREFIX + SignConstant.DOT_CHAR + "BenchmarkSessionGetter");
        keywordClassMap.put(END_BENCHMARK, CLASS_PREFIX + SignConstant.DOT_CHAR + "BenchmarkSessionClose");
        keywordClassMap.put(SYSBENCH_CPU, CLASS_PREFIX + SignConstant.DOT_CHAR + "SysbenchCPUProcessor");
        keywordClassMap.put(SYSBENCH_IO, CLASS_PREFIX + SignConstant.DOT_CHAR + "SysbenchIOProcessor");
        keywordClassMap.put(SYSBENCH_MEM, CLASS_PREFIX + SignConstant.DOT_CHAR + "SysbenchMemoryProcessor");
        keywordClassMap.put(SYSBENCH_THREADS, CLASS_PREFIX + SignConstant.DOT_CHAR + "SysbenchThreadsProcessor");
        keywordClassMap.put(SYSBENCH_MUTEX, CLASS_PREFIX + SignConstant.DOT_CHAR + "SysbenchMutexProcessor");
        keywordClassMap.put(TOUCHSTONE, CLASS_PREFIX + SignConstant.DOT_CHAR + "TouchstoneProcessor");
        keywordClassMap.put(TPCH, CLASS_PREFIX + SignConstant.DOT_CHAR + "TpcHProcessor");
        keywordClassMap.put(NMON, CLASS_PREFIX + SignConstant.DOT_CHAR + "NmonProcessor");

        keywordClassMap.put(INIT_DATABENCH_CONFIG, CLASS_PREFIX + SignConstant.DOT_CHAR + "InitConfigProcessor");
        keywordClassMap.put(START_DATABENCH, CLASS_PREFIX + SignConstant.DOT_CHAR + "StartDatabenchProcessor");

    }
}
