package edu.ecnu.woodpecker.constant;

/**
 * 文件常量类
 * 
 */
public final class FileConstant
{
    // 结果集文件后缀
    public final static String RESULT_FILE_SUFFIX = ".result";
    // DBI文件名后缀
    public final static String DBI_FILE_SUFFIX = ".dbi";
    // 目录分隔符
    public final static String FILE_SEPARATOR = "/";
    public final static char FILE_SEPARATOR_CHAR = '/';
    // 案例文件后缀
    public final static String CASE_FILE_SUFFIX = ".case";
    // 中间结果集文件后缀
    public final static String MIDDLE_RESULT_FILE_SUFFIX = ".mr";
    // 性能结果文件后缀
    public final static String PERFORMANCE_RESULT_FILE_SUFFIX = ".pr";
    // java jar文件后缀
    public final static String JAR_FILE_SUFFIX = ".jar";
    // UTF-8字符集
    public final static String UTF_8 = "utf-8";
    // 配置文件后缀
    public final static String CONFIG_FILE_SUFFIX = ".conf";
    // 负载配置文件后缀
    public final static String WORKLOAD_CONFIG_FILE_SUFFIX = ".wlconf";

    // CEDAR编译配置文件名
    public final static String CEDAR_COMPILE_FILE_NAME = "Cedar_compile.conf";
    // MySQL配置文件名
    public final static String MYSQL_CONFIG_FILE_NAME = "MySQL_configuration.conf";
    // PostgreSQL配置文件名
    public final static String POSTGRESQL_CONFIG_FILE_NAME = "PostgreSQL_configuration.conf";

    // Cedar killer配置文件路径
    public final static String CEDAR_KILLER_CONFIG_PATH = "./tools/CedarKiller/cedar_killer.conf";
    // Cedar merge配置文件路径
    public final static String CEDAR_MERGE_CONFIG_PATH = "./tools/CedarMerge/cedar_merge.conf";
    // Cedar reelect config file path
    public final static String CEDAR_REELECT_CONFIG_PATH = "./tools/CedarReelect/cedar_reelect.conf";
    // SyntaxChecker中间结果文件路径
    public final static String SYNTAX_CHECKER_OUTPUT_PATH = "./middle_result/check.mr";
    // log4j config file path
    public final static String LOG4J_CONFIG_PATH = "./config/log4j.properties";
    // default test environment config path
    public final static String DEF_TEST_ENV_CONF_PATH = "./config/";
    // default test case path
    public final static String DEF_TEST_CASE_PATH = "./test_case/";
    // default ideal result set path
    public final static String DEF_IDE_RES_PATH = "./ideal_result_set";
    // default database instance path
    public final static String DEF_DB_INST_PATH = "./database_instance/";
    // default stress test directory
//    public final static String DEF_STRESS_TEST_DIR = "./stress_test/";
    public final static String DEF_STRESS_TEST_DIR = "./st/";
    // default ip address
    public final static String DEF_IP = "localhost";
    // default report path
    public final static String DEF_REPORT_PATH = "./";
    // default retry time
    public final static int DEF_RETRY_COUNT = 2;
    
    // default time for sending result from workload machine to dispatcher
    public final static int DEF_SEND_TIME = 8;
    
    // default testing directory on server is "/home/username/performancetest/st/", st includes config and nohup.out
    public final static String DEF_STRESS_TEST_DIR_ON_SERVER = "performancetest/st/";
    
    // default global confuration file in st is "config"
    public final static String DEF_GLOBAL_CONF_DIR_NAME_ON_SERVER = "config";
    
    // default stress testing configuration file
//    public final static String DEF_STRESS_TEST_CONFIG_FILE = "default";
    public final static String DEF_STRESS_TEST_CONFIG_FILE = "mysql";
//    public final static String DEF_STRESS_TEST_CONFIG_FILE = "postgresql";
    
    // default nullRatio for all data types
    public final static float NULL_RATIO = 0;
    
    //default cardinity for int(integer) type
    public final static int CARDINALITY = 30000;
    
    // default minValue, maxValue for all numeric types
    public final static double MIN_VALUE = -100000000;
    public final static double MAX_VALUE = 100000000;
    
    // default minLength, maxLength, seedNumber for all character types
    public final static int MIN_LENGTH = 1;
    public final static int MAX_LENGTH = 50;
    public final static int SEEDS_NUMBER = 10000;
    

    // default time unit(microsecond)
    public final static long MILLISECOND = 1000;
    // default time unit for (millisecond)
    public final static long MICROSECOND = 1000000;
    // default time unit(nanosecond)
    public final static long NANOSECOND = 1000000000;
    
    // default mu in Gaussian distribution
    public final static double NORMAL_DIST_MU = 0;
    // default Gaussian distribution range
    public final static int NORMAL_DIST_RANGE = 3;
    
    // default Gaussian distribution array size
    public final static int NORMAL_DIST_LIST_SIZE = 10;
    // default Zipf distribution array size
    public final static int ZIPF_DIST_LIST_SIZE = 10;
    
    // ?跨喐鏋婚幏鐑芥晸???㈠殩?凤拷
    public final static char WIN_LINE_FEED_CHAR = '\n';
    public final static String WIN_LINE_FEED_STR = "\n";
    public final static String LINUX_LINE_FEED = "\r\n";
    public final static char MAC_LINE_FEED = '\r';
    // tab
    public final static char TAB_CHAR = '\t';
}
