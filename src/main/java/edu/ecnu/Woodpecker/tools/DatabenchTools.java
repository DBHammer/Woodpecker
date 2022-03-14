package edu.ecnu.Woodpecker.tools;

import edu.ecnu.Woodpecker.constant.LogLevelConstant;
import edu.ecnu.Woodpecker.controller.MySQLOperation;
import edu.ecnu.Woodpecker.controller.TestController;
import edu.ecnu.Woodpecker.log.WpLog;
import edu.ecnu.Woodpecker.util.Util;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author : Youshuhong
 * @create : 2022/3/3 20:14
 */
public class DatabenchTools {
    private String driver;
    private String IP;
    private Integer Port;
    private String user;
    private String password;
    private String databaseCon;

    private String configPath;

    private List<String> config;

    public DatabenchTools() {
        this.configPath = "tools/databench/soft/application.properties";
        this.config = new ArrayList<>();

        if (TestController.getDatabase().getBrand().equalsIgnoreCase("mysql") || TestController.getDatabase().getBrand().equalsIgnoreCase("tidb")) {
            this.driver = MySQLOperation.getDriver();
            this.IP = MySQLOperation.getIP();
            this.Port = MySQLOperation.getPort();
            this.user = MySQLOperation.getDatabaseUser();
            this.password = MySQLOperation.getDatabasePassword();
        } else {
            WpLog.recordLog(LogLevelConstant.ERROR, "金融测评工具目前只适配mysql及其语法兼容的数据库");
        }
    }

    public void initConfig(String keyword) {
        String[] parts = Util.removeBlankElement(keyword.split("\\[|;|]"));
        String businesstest_initSize = parts[1].trim();
        String businesstest_minSize = parts[2].trim();
        String businesstest_maxSize = parts[3].trim();
        String local_initSize = parts[4].trim();
        String local_minSize = parts[5].trim();
        String local_maxSize = parts[6].trim();

        try {
            readConfig(businesstest_initSize, businesstest_minSize, businesstest_maxSize,
                    local_initSize, local_minSize, local_maxSize);
            setConfig();

        } catch (Exception e) {
            e.printStackTrace();
        }
        WpLog.recordLog(LogLevelConstant.INFO, "配置文件重写已完成");

    }

    public void startDatabench(String keyword) throws UnsupportedEncodingException {
        String[] parts = Util.removeBlankElement(keyword.split("\\[|;|]"));
        Integer datacfg_id = Integer.parseInt(parts[1]);
        Integer trancfg_id = Integer.parseInt(parts[2]);
        String isolation_level = parts[3];
        Integer round = Integer.parseInt(parts[4]);

        String create_businessdb = "mysql -h" + this.IP + " -u" + this.user + " -p" + this.password
                + " --execute=\"source /Users/irisyou/Woodpecker/tools/databench/sql/mysql/businesstest_database.sql;\"";
        WpLog.recordLog(LogLevelConstant.INFO, "开始导入businesstest_database.sql： " + create_businessdb);
        exec(create_businessdb);

        String create_localdb = "mysql -h" + this.IP + " -u" + this.user + " -p" + this.password
                + " --execute=\"source /Users/irisyou/Woodpecker/tools/databench/sql/mysql/local_database.sql;\"";
        WpLog.recordLog(LogLevelConstant.INFO, "开始导入local_database.sql： " + create_localdb);
        exec(create_localdb);


        String init_data_cmd = "cd tools/databench/soft && java -Dfile.encoding=utf-8 -jar ftdb.jar init ";
        init_data_cmd += datacfg_id + " --spring.config.location =" + this.configPath.substring(0, this.configPath.lastIndexOf(".")) + "-new.properties";
        ;
        WpLog.recordLog(LogLevelConstant.INFO, "导入数据： " + init_data_cmd);
        String init_repo = exec(init_data_cmd).toString();


        for (int i = 0; i < round; i++) {
            String cmd = "cd tools/databench/soft && java -Dfile.encoding=utf-8 -jar ftdb.jar test " + datacfg_id + " " +
                    trancfg_id + " " + isolation_level.trim() + " --spring.config.location =" + this.configPath.substring(0, this.configPath.lastIndexOf(".")) + "-new.properties";
            WpLog.recordLog(LogLevelConstant.INFO, "开始执行databench");
            WpLog.recordLog(LogLevelConstant.INFO, "databench命令: " + cmd);
            TestController.reportGenerator.appendNewBenchmark("DATABENCH 测试报告", "");

            String result = exec(cmd).toString();


            TestController.reportGenerator.appendBenchmarkDetail("第 "+ (i+1) + " 次测试：")
                    .appendBenchmarkDetail(result);


        }


    }

    private void readConfig(String businesstest_initSize, String businesstest_minSize, String businesstest_maxSize,
                            String local_initSize, String local_minSize, String local_maxSize) throws Exception {
        FileReader file = new FileReader(configPath);
        BufferedReader br = new BufferedReader(file);
        String line = "";
        while ((line = br.readLine()) != null) {
            while (line.startsWith("#")) {
                line = br.readLine();
            }
            if (line.contains("driverClassName")) {
                line = line.split("=")[0] + "=" + this.driver;
            } else if (line.contains("url")) {
                String originConn = line.split("=")[1];
                String[] parts = originConn.split("//");
                String[] p2 = parts[1].split(":");
                String[] p3 = p2[1].split("/");
                String newConn = parts[0] + "//" + this.IP + ":" + this.Port + "/" + p3[1];
                line = line.split("=")[0] + "=" + newConn;
                this.databaseCon = newConn;
            } else if (line.contains("username")) {
                line = line.split("=")[0] + "=" + this.user;
            } else if (line.contains("password")) {
                line = line.split("=")[0] + "=" + this.password;
            } else if (line.startsWith("main.datasource.initSize=")) {
                line = "main.datasource.initSize=" + businesstest_initSize;
            } else if (line.startsWith("main.datasource.minSize=")) {
                line = "main.datasource.minSize=" + businesstest_minSize;
            } else if (line.startsWith("main.datasource.maxSize=")) {
                line = "main.datasource.maxSize=" + businesstest_maxSize;
            } else if (line.startsWith("local.datasource.initSize=")) {
                line = "local.datasource.initSize=" + local_initSize;
            } else if (line.startsWith("local.datasource.minSize=")) {
                line = "local.datasource.minSize=" + local_minSize;
            } else if (line.startsWith("local.datasource.maxSize=")) {
                line = "local.datasource.maxSize=" + local_maxSize;
            }
            this.config.add(line);
        }
        br.close();
        file.close();
    }

    private void setConfig() {

        String newPath = this.configPath.substring(0, this.configPath.lastIndexOf(".")) + "-new.properties";
        System.out.println("开始重写配置文件" + newPath);
        File file = new File(newPath);
        file.delete();
        try {
            file.createNewFile();
            FileWriter fw = new FileWriter(file, true);
            for (String singleConfig : this.config) {
                System.out.println(singleConfig);
                fw.write(singleConfig);
                fw.write("\n");
            }
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String exec(String cmd) {
        try {
            String[] cmdA = {"/bin/sh", "-c", cmd};
            Process process = Runtime.getRuntime().exec(cmdA);
            LineNumberReader br = new LineNumberReader(new InputStreamReader(
                    process.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                sb.append(line).append("<br/>");
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
