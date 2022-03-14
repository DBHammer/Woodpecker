package edu.ecnu.Woodpecker.tools;

import edu.ecnu.Woodpecker.constant.LogLevelConstant;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import edu.ecnu.Woodpecker.controller.MySQLOperation;
import edu.ecnu.Woodpecker.controller.PostgreSQLOperation;
import edu.ecnu.Woodpecker.controller.TestController;
import edu.ecnu.Woodpecker.log.WpLog;
import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.*;

/**
 * @ClassName BenchmarkSQLTools.java
 * @Description 调用benchmarksqltools
 * @createTime 2021年11月09日 20:45:00
 */
public class BenchmarkSQLTools {
    private String configFile;
    private List<String> config;

    private String driver;
    private String IP;
    private int Port;
    private String user;
    private String password;
    private String databaseCon;

    private int warehouses;
    private int loadWorkers;
    private int terminals;
    private int runTxnsPerTerminal;
    private int runMins;
    private int limitTxnsPerMin;
    public BenchmarkSQLTools(int warehouses, int loadWorkers, int terminals, int runTxnsPerTerminal,int runMins,int limitTxnsPerMin){
        if(TestController.getDatabase().getBrand().equalsIgnoreCase("mysql") || TestController.getDatabase().getBrand().equalsIgnoreCase("tidb")) {
            this.configFile = "./tools/BenchmarkTools/benchmarksql/run/props.mysql";
            // tidb和mysql都是mysql语法兼容的数据库，所以用的是props.mysql
//        else if(){ postgresql 可以根据自己测试数据库的需求添加
//            this.configFile = "./tools/BenchmarkTools/benchmarksql/run/props.pg";
//        }
            this.config = new ArrayList<>();
            this.driver = MySQLOperation.getDriver();
            this.IP = MySQLOperation.getIP();
            this.Port = MySQLOperation.getPort();
            this.user = MySQLOperation.getDatabaseUser();
            this.password = MySQLOperation.getDatabasePassword();
        }
        else if(TestController.getDatabase().getBrand().equalsIgnoreCase("postgresql")){
            this.configFile = "./tools/BenchmarkTools/benchmarksql/run/props.pg";
            this.config = new ArrayList<>();
            this.driver = PostgreSQLOperation.getDriver();
            this.IP = PostgreSQLOperation.getIP();
            this.Port = PostgreSQLOperation.getPort();
            this.user = PostgreSQLOperation.getDatabaseUser();
            this.password = PostgreSQLOperation.getDatabasePassword();
        }
        this.warehouses = warehouses;
        this.loadWorkers = loadWorkers;
        this.terminals = terminals;
        this.runTxnsPerTerminal = runTxnsPerTerminal;
        this.runMins = runMins;
        this.limitTxnsPerMin = limitTxnsPerMin;
        try {
            readConfig();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    //读取config，并根据关键字修改为平台定义的属性
    private void readConfig() throws Exception{
        FileReader file = new FileReader(configFile);
        BufferedReader br=new BufferedReader(file);
        String line="";
        while ((line=br.readLine())!=null) {

            if(line.startsWith("driver=")){
                line = "driver=" + this.driver;
            }
            else if(line.startsWith("conn=")){
                String originConn = line.split("n=")[1];
                String[] parts = originConn.split("//");
                String[] p2 = parts[1].split(":");
                String[] p3 = p2[1].split("/");
                String newConn = parts[0] + "//" +this.IP+ ":" +this.Port+"/"+p3[1];
                line = "conn=" + newConn;
                this.databaseCon = newConn;
            }
            else if(line.startsWith("user=")){
                line = "user=" + this.user;
            }
            else if(line.startsWith("password=")){
                line = "password=" + this.password;
            }else if(line.startsWith("warehouses=")){
                line = "warehouses=" + this.warehouses;
            } else if(line.startsWith("loadWorkers=")){
                line = "loadWorkers=" + this.loadWorkers;
            }else if(line.startsWith("terminals=")){
                line = "terminals=" + this.terminals;
            } else if(line.startsWith("runTxnsPerTerminal=")){
                line = "runTxnsPerTerminal=" + this.runTxnsPerTerminal;
            }else if(line.startsWith("runMins=")){
                line = "runMins=" + this.runMins;
            } else if(line.startsWith("limitTxnsPerMin=")){
                line = "limitTxnsPerMin=" + this.limitTxnsPerMin;
            }
            config.add(line);
        }
        br.close();
        file.close();
    }
    public void setConfig(){
        System.out.println("开始重写配置文件"+this.configFile);
        File file = new File(this.configFile);
        file.delete();
        try {
            file.createNewFile();
            FileWriter fw = new FileWriter(file,true);
            for(String singleConfig : config){
                System.out.println(singleConfig);
                fw.write(singleConfig);
                fw.write("\n");
            }
            fw.close();
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    private Connection getConnection(){
        Connection conn = null;
        String driver = this.driver;
        try {
            Class.forName(driver);
            String connWithoutDatabase = null;
            String[] prefix = databaseCon.split("/");
            String[] prefix1 = databaseCon.split("\\?");
            String[] prefix2 = prefix1[0].split("/");
            if(TestController.getDatabase().getBrand().equalsIgnoreCase("mysql") || TestController.getDatabase().getBrand().equalsIgnoreCase("tidb")) {
                connWithoutDatabase = prefix2[0] + "//" + prefix2[1] + prefix2[2] + "/?" + prefix1[1];
            }
            else if(TestController.getDatabase().getBrand().equalsIgnoreCase("postgresql")) {
                connWithoutDatabase = prefix[0] + "//" + prefix[1] + prefix[2] + "/";
            }
            System.out.println(connWithoutDatabase);
            conn = DriverManager.getConnection(connWithoutDatabase, user, password);
        }catch (Exception e){
            e.printStackTrace();
        }
        return conn;
    }
    public void loadData() throws SQLException {
        Connection conn = getConnection();
        Statement statement = conn.createStatement();



        //执行createTable脚本
        if(TestController.getDatabase().getBrand().equalsIgnoreCase("mysql") || TestController.getDatabase().getBrand().equalsIgnoreCase("tidb")){
            String dropDB = "drop database if exists tpcc";
            statement.executeUpdate(dropDB);
            statement.executeUpdate("create database tpcc");
            statement.execute("use tpcc");

            SQLExec sqlExec = new SQLExec();
            sqlExec.setDriver(this.driver);
            sqlExec.setUrl(databaseCon);
            sqlExec.setUserid(user);
            sqlExec.setPassword(password);
            sqlExec.setSrc(new File("./tools/BenchmarkTools/benchmarksql/run/sql.mysql/tableCreates.sql"));
            sqlExec.setProject(new Project());
            sqlExec.execute();
            WpLog.recordLog(LogLevelConstant.INFO,"mysql执行createTable成功");

            //createIndex
            SQLExec sqlExec2 = new SQLExec();
            sqlExec2.setDriver(this.driver);
            sqlExec2.setUrl(databaseCon);
            sqlExec2.setUserid(user);
            sqlExec2.setPassword(password);
            sqlExec2.setSrc(new File("./tools/BenchmarkTools/benchmarksql/run/sql.mysql/indexCreates.sql"));
            sqlExec2.setProject(new Project());
            sqlExec2.execute();
            conn.close();
            WpLog.recordLog(LogLevelConstant.INFO,"执行createIndex成功");


            //load data
            WpLog.recordLog(LogLevelConstant.INFO,"mysql开始导入数据");
            String cmd = "cd tools/BenchmarkTools/benchmarksql/run && ./runLoader.sh props.mysql";
            String netsString = exec(cmd).toString();

            WpLog.recordLog(LogLevelConstant.INFO,netsString);

            WpLog.recordLog(LogLevelConstant.INFO,"mysql导入数据完成");
        }
        else if(TestController.getDatabase().getBrand().equalsIgnoreCase("postgresql")){
            String dropDB = "drop database if exists postgresql";
            statement.execute(dropDB);
            statement.executeUpdate("create database postgresql");

            //load data
            WpLog.recordLog(LogLevelConstant.INFO,"pg开始导入数据");
            String cmd = "cd tools/BenchmarkTools/benchmarksql/run && ./runDatabaseBuild.sh props.pg";
            String netsString = exec(cmd).toString();
            WpLog.recordLog(LogLevelConstant.INFO,netsString);

            WpLog.recordLog(LogLevelConstant.INFO,"pg导入数据完成");
        }


    }
    public static Object exec(String cmd) {
        try {
            String[] cmdA = { "/bin/sh", "-c", cmd };
            Process process = Runtime.getRuntime().exec(cmdA);
            LineNumberReader br = new LineNumberReader(new InputStreamReader(
                    process.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void startBenchmarkSQL(String config){
        WpLog.recordLog(LogLevelConstant.INFO,"开始执行BenchmarkSQL");
        String cmd = "cd tools/BenchmarkTools/benchmarksql/run && ./runBenchmark.sh "+config;
        String netsString = exec(cmd).toString();
        WpLog.recordLog(LogLevelConstant.INFO,"BenchmarkSQL执行结果");
        System.out.println(netsString);
        TestController.reportGenerator.appendNewBenchmark("Benchmark SQL Tools 测试报告", "```")
                .appendBenchmarkDetail(netsString)
                .appendBenchmarkDetail("```");

    }




}
