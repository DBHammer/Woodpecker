package edu.ecnu.Woodpecker.tools;

import edu.ecnu.Woodpecker.constant.LogLevelConstant;
import edu.ecnu.Woodpecker.controller.MySQLOperation;
import edu.ecnu.Woodpecker.controller.PostgreSQLOperation;
import edu.ecnu.Woodpecker.controller.TestController;
import edu.ecnu.Woodpecker.log.WpLog;
import edu.ecnu.Woodpecker.util.Util;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;


/**
 * @author Youshuhong
 * @create 2021/11/13 下午10:08
 */
public class OltpbenchTools {
    private String configPath;

    private String driver;
    private String IP;
    private Integer Port;
    private String user;
    private String password;
    private String databaseCon;

    private String bench;
    private String isolation;
    private String scalefactor;
    private String loaderThreads;
    private String terminal;
    private String time;
    private String rate;

    public OltpbenchTools(String bench, String isolation, String scalefactor, String loaderThreads, String terminal, String time, String rate){
        if(TestController.getDatabase().getBrand().equalsIgnoreCase("mysql") || TestController.getDatabase().getBrand().equalsIgnoreCase("tidb")) {
            // tidb和mysql都是mysql语法兼容的数据库
            this.driver = MySQLOperation.getDriver();
            this.IP = MySQLOperation.getIP();
            this.Port = MySQLOperation.getPort();
            this.user = MySQLOperation.getDatabaseUser();
            this.password = MySQLOperation.getDatabasePassword();
            this.configPath = "tools/BenchmarkTools/oltpbench/config/sample_" + bench + "_config.xml";
        }
        else if(TestController.getDatabase().getBrand().equalsIgnoreCase("postgresql")){
            this.driver = PostgreSQLOperation.getDriver();
            this.IP = PostgreSQLOperation.getIP();
            this.Port = PostgreSQLOperation.getPort();
            this.user = PostgreSQLOperation.getDatabaseUser();
            this.password = PostgreSQLOperation.getDatabasePassword();
            this.configPath = "tools/BenchmarkTools/oltpbench/config/sample_" + bench + "_config.xml";
        }

            this.bench = bench;
            this.isolation = isolation;
            this.scalefactor = scalefactor;
            this.loaderThreads = loaderThreads;
            this.terminal = terminal;
            this.time = time;
            this.rate = rate;

        try {
            setConfig(this.configPath);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void SeizeOltpBench(String keyword) throws Exception {

        String[] parts = Util.removeBlankElement(keyword.split("\\[|;|]"));
        String bench = parts[1];
        Boolean create = Boolean.parseBoolean(parts[8]);
        Boolean load = Boolean.parseBoolean(parts[9]);
        Boolean execute = Boolean.parseBoolean(parts[10]);
        Integer sampling_window = Integer.parseInt(parts[11]);
        String outputfile = parts[12];

        String cmd = "cd tools/BenchmarkTools/oltpbench && ./oltpbenchmark -b " + bench + " -c " + "config/" + bench + "_config.xml" + " --create " + create.toString() + " --load " + load.toString() +
                " --execute " + execute.toString() + " -s " + sampling_window + " -o " +outputfile;
        WpLog.recordLog(LogLevelConstant.INFO,"开始执行oltpbench");
        WpLog.recordLog(LogLevelConstant.INFO, "oltpbench命令: "+cmd);
        String result = exec(cmd).toString();
        System.out.println("==========获得值=============");
        System.out.println(result);
        TestController.reportGenerator.appendNewBenchmark("OLTP-BENCH 测试报告", "```")
                .appendBenchmarkDetail(result).appendBenchmarkDetail("```");

        //result = result.replaceAll("\u001B\\[0;1m", "").replaceAll("\u001B\\[0;0m","");
        //删除result中的控制字符、打印不出的字符
        result = result.replaceAll("[\\p{Cntrl}&&[^\\p{Space}]]\\[\\d+;\\d+m", "");

        WpLog.recordLog(LogLevelConstant.INFO, "oltpbench执行结果: "+result);
        String newFileName = "";
        newFileName = result.split(".csv")[0].split("results/")[1]; //不带格式的结果文件名
        outCsv(newFileName);//将res文件转存成csv

    }

    public static void outCsv(String filename) throws IOException {
        String resFileName = filename+".res";
        File file=new File("tools/BenchmarkTools/oltpbench/results/" + resFileName);
        File newfile = new File("tools/BenchmarkTools/oltpbench/results/" + resFileName +".csv");
        file.renameTo(newfile);
        WpLog.recordLog(LogLevelConstant.INFO, "oltpbench细粒度测试指标结果已存为csv格式");

    }

    private void setConfig(String configPath) throws  IOException, DocumentException {

        SAXReader sr = new SAXReader();
        Document document = sr.read(configPath);
        Element root = document.getRootElement();


        List<Element> elements = root.elements();
        if(!root.elements().contains("loaderThreads")){//配置文件中需要增加loaderThreads节点
            Element loadThrds = root.addElement("loaderThreads");
            loadThrds.setText(this.loaderThreads);
        }
        for (Element element : elements) {

            if(TestController.getDatabase().getBrand().equalsIgnoreCase("mysql") || TestController.getDatabase().getBrand().equalsIgnoreCase("tidb")) {
                if (element.getName().equalsIgnoreCase("dbtype")) {
                    element.setText("mysql");
                }
                if (element.getName().equalsIgnoreCase("DBUrl")) {
                    element.setText("jdbc:mysql://" + this.IP + ":" + this.Port + '/' + this.bench);
                    this.databaseCon = element.getText();
                }
            }
            else if(TestController.getDatabase().getBrand().equalsIgnoreCase("postgresql")){
                if (element.getName().equalsIgnoreCase("dbtype")) {
                    element.setText("postgres");//注意dbtype不是postgresql
                }
                if (element.getName().equalsIgnoreCase("DBUrl")) {
                    element.setText("jdbc:postgresql://" + this.IP + ":" + this.Port + '/' + this.bench);
                    this.databaseCon = element.getText();
                }
            }
            if(element.getName().equalsIgnoreCase("driver")){
                element.setText(this.driver);
            }
            if(element.getName().equalsIgnoreCase("username")){
                element.setText(this.user);
            }
            if(element.getName().equalsIgnoreCase("password")){
                if(this.password != "") {
                    element.setText(this.password);
                }
                else {
                    element.setText("");
                }
            }
            if(element.getName().equalsIgnoreCase("isolation")){
                element.setText(this.isolation);
            }
            if(element.getName().equalsIgnoreCase("scalefactor")){
                element.setText(this.scalefactor);
            }
            if(element.getName().equalsIgnoreCase("terminals")){
                element.setText(this.terminal);
            }
            if(element.getName().equalsIgnoreCase("works")) {
                element.element("work").element("time").setText(this.time);
                element.element("work").element("rate").setText(this.rate);
            }
            if(element.getName().equalsIgnoreCase("uploadCode")){
                element.setText("");
            }
            if(element.getName().equalsIgnoreCase("uploadUrl")){
                element.setText("");
            }

        }
        saveDocument(document, new File("tools/BenchmarkTools/oltpbench/config/" + bench + "_config.xml"));
    }

    private Connection oltpbenchGetConnection(){
        //此时还没新建数据库
        Connection conn = null;
        String driver = this.driver;
        try {
            Class.forName(driver);
            String connWithoutDatabase = null;
            String[] prefix = databaseCon.split("/");
            if(TestController.getDatabase().getBrand().equalsIgnoreCase("mysql") || TestController.getDatabase().getBrand().equalsIgnoreCase("tidb")) {
                connWithoutDatabase = prefix[0] + "//" + prefix[2] + "?useSSL=false&useServerPrepStmts=true&useConfigs=maxPerformance&rewriteBatchedStatements=true";
            }
            //注意pg和mysql的语法差异。
            else if(TestController.getDatabase().getBrand().equalsIgnoreCase("postgresql")){
                connWithoutDatabase = prefix[0] + "//" + prefix[2] + '/';
            }
            System.out.println(connWithoutDatabase);
            conn = DriverManager.getConnection(connWithoutDatabase, user, password);
            WpLog.recordLog(LogLevelConstant.INFO,"数据库已连接");
        }catch (Exception e){
            e.printStackTrace();
        }
        return conn;
    }

    public void create_database(String bench, Boolean flag) throws SQLException {
        Connection conn = oltpbenchGetConnection();
        Statement statement = conn.createStatement();
        if(flag) {
            String dropDB = "drop database if exists " + bench;
            statement.execute(dropDB);
            String createDb = "create database " + bench;
            statement.execute(createDb);
            WpLog.recordLog(LogLevelConstant.INFO,"创建数据库成功");
        }
    }

    public static String exec(String cmd) {
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

    public static void saveDocument(Document document, File xmlFile) throws IOException {
        Writer osWrite = new OutputStreamWriter(new FileOutputStream(xmlFile));
        OutputFormat format = OutputFormat.createPrettyPrint();
        XMLWriter writer = new XMLWriter(osWrite, format);

        writer.write(document);
        writer.flush();
        writer.close();
    }




}
