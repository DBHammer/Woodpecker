package edu.ecnu.Woodpecker.tools;

import edu.ecnu.Woodpecker.constant.ConfigConstant;
import edu.ecnu.Woodpecker.constant.LogLevelConstant;
import edu.ecnu.Woodpecker.controller.MySQLOperation;
import edu.ecnu.Woodpecker.controller.PostgreSQLOperation;
import edu.ecnu.Woodpecker.controller.TestController;
import edu.ecnu.Woodpecker.log.RecordReport;
import edu.ecnu.Woodpecker.log.WpLog;
import edu.ecnu.Woodpecker.util.ProcessExecInfo;
import edu.ecnu.Woodpecker.util.Util;
import net.sf.json.JSONObject;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * @Author: Chen Lixiang
 * @Email: lixiang3608@outlook.com
 * @Date: 2021/11/17
 */
public class TpcHTools {

    private String host;
    private int port;
    private String userName;
    private String password;

    public TpcHTools() throws Exception {
        retrieveConnection();
    }

    private void retrieveConnection() throws Exception {
        String database = TestController.getDatabase().getBrand();
        if (database.equals(ConfigConstant.MYSQL)) {
            this.host = MySQLOperation.getIP();
            this.port = MySQLOperation.getPort();
            this.userName = MySQLOperation.getDatabaseUser();
            this.password = MySQLOperation.getDatabasePassword();
        }
        else if (database.equals(ConfigConstant.POSTGRESQL)){
            this.host = PostgreSQLOperation.getIP();
            this.port = PostgreSQLOperation.getPort();
            this.userName = PostgreSQLOperation.getDatabaseUser();
            this.password = PostgreSQLOperation.getDatabasePassword();
        }
        else{
                throw new Exception("Tpc-H 暂不支持 " + database);
        }
    }

    public void dbGen(String dbSize) throws Exception {
        Util.execCommand("cd tools/BenchmarkTools/tpch-kit/dbgen && rm *.tbl");
        String cmd = "cd tools/BenchmarkTools/tpch-kit/dbgen && ./dbgen " + dbSize;
        WpLog.recordLog(LogLevelConstant.INFO, "开始执行 dbgen 生成负载，负载大小: %s", dbSize);
        ProcessExecInfo info = Util.execCommand(cmd);
        if (info.getExitValue() == 0) {
            WpLog.recordLog(LogLevelConstant.INFO, "负载生成成功: %s", info.getStdMsg());
        } else {
            WpLog.recordLog(LogLevelConstant.ERROR, "负载生成失败: %s", info.getErrMsg());
            throw new Exception("dbgen 负载生成失败");
        }
    }

    public void loadData() throws Exception {
        String cmd = null;
        if (TestController.getDatabase().getBrand().equalsIgnoreCase("mysql")
                || TestController.getDatabase().getBrand().equalsIgnoreCase("tidb")) {
            cmd = "cd tools/BenchmarkTools/tpch-kit/dbgen && ./load.sh " + userName + " "
                    + password + " " + host + " " + port;
        } else if (TestController.getDatabase().getBrand().equalsIgnoreCase("postgresql")) {
            cmd = "cd tools/BenchmarkTools/tpch-kit/dbgen && ./load_pgsql.sh " + userName + " "
                    + password + " " + host + " " + port;
        }
        WpLog.recordLog(LogLevelConstant.INFO, "开始将 dbgen 生成负载载入数据库");
        ProcessExecInfo info = Util.execCommand(cmd);
        if (info.getExitValue() == 0) {
            WpLog.recordLog(LogLevelConstant.INFO, "负载载入成功: %s", info.getStdMsg());
        } else {
            WpLog.recordLog(LogLevelConstant.ERROR, "负载载入失败: %s", info.getErrMsg());
            throw new Exception("负载载入失败");
        }
    }

    public void executeQueries() {
        String url = null;
        Connection conn = null;
        String driver = null;
        if (TestController.getDatabase().getBrand().equalsIgnoreCase("mysql")
        || TestController.getDatabase().getBrand().equalsIgnoreCase("tidb")) {
            url = "jdbc:mysql://" + host + ":" + port + "/TPCH_TEST?useSSL=false";
            driver = "com.mysql.jdbc.Driver";
        } else if (TestController.getDatabase().getBrand().equalsIgnoreCase("postgresql")) {
            url = "jdbc:postgresql://" + host + ":" + port + "/TPCH_TEST";
            driver = "org.postgresql.Driver";
        }

        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(url, userName, password);
            if (conn != null) {
                WpLog.recordLog(LogLevelConstant.INFO, "数据库连接成功");
            } else {
                WpLog.recordLog(LogLevelConstant.INFO, "数据库连接失败");
            }
            Statement statement = conn.createStatement();
            BufferedWriter out = new BufferedWriter(
                    new FileWriter("tools/BenchmarkTools/tpch-kit/result.txt", true));
            Date now = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            out.write("------TPC-H 测试 " + dateFormat.format(now) + "------\n");
            TestController.reportGenerator.appendNewBenchmark("TPC-H 查询速度测试", "");

            // 执行全部 22 个 TPCH 查询
            Map<String, String> jsonContent = new HashMap<>();
            for (int i = 1; i <= 22; i++) {
                File sqlFile = new File("tools/BenchmarkTools/tpch-kit/dbgen/queries/" + i + ".sql");
                InputStreamReader streamReader = new InputStreamReader(new FileInputStream(sqlFile));
                BufferedReader bufferedReader = new BufferedReader(streamReader);

                StringBuilder builder = new StringBuilder();
                String content = "";
                long start = System.currentTimeMillis();
                while ((content = bufferedReader.readLine()) != null) {
                    builder.append(content).append('\n');
                    if (content.endsWith(";")) {
                        statement.execute(builder.toString());
                        builder = new StringBuilder();
                    }
                }

                long taken = System.currentTimeMillis() - start;
                WpLog.recordLog(LogLevelConstant.INFO,
                        "TPC-H 第 "+ i + " 个查询执行完成，耗时 %ds%dms", taken / 1000, taken % 1000);
                out.write("TPC-H 第 " + i + " 个查询执行耗时 " + taken + " ms\n");
                TestController.reportGenerator.
                        appendBenchmarkDetail("- TPC-H 第 " + i + " 个查询耗时 " + (taken / 1000) + "s" + (taken % 1000) + "ms");
                jsonContent.put(String.valueOf(i), String.format("%ds%dms", (taken / 1000), (taken % 1000)));
            }
            conn.close();
            out.close();
            outJson("tpch_results_", jsonContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void outJson(String fileName, Map<String, String> content) {
        JSONObject jsonObject = JSONObject.fromObject(content);
        String jsonString = jsonObject.toString();
        Calendar rightNow = Calendar.getInstance();
        Integer year = rightNow.get(Calendar.YEAR);
        Integer month = rightNow.get(Calendar.MONTH)+1; //第一个月从0开始，所以得到月份＋1
        Integer day = rightNow.get(rightNow.DAY_OF_MONTH);
        Integer hour24 = rightNow.get(rightNow.HOUR_OF_DAY);
        Integer minute = rightNow.get(rightNow.MINUTE);
        Integer second = rightNow.get(rightNow.SECOND);
        String newFileName = fileName
                +year+"-"+month+"-"+day+"-"+hour24+"-"+minute+"-"+second;
        RecordReport.CreateFileUtil.createJsonFile(jsonString, "./tools/BenchmarkTools/tpch-kit", newFileName);
    }

}
