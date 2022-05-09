package edu.ecnu.Woodpecker.executor.keyword;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import edu.ecnu.Woodpecker.constant.LogLevelConstant;
import edu.ecnu.Woodpecker.constant.SignConstant;
import edu.ecnu.Woodpecker.executor.Executor;
import edu.ecnu.Woodpecker.executor.GrammarType;
import edu.ecnu.Woodpecker.log.WpLog;
import edu.ecnu.Woodpecker.util.Util;

import java.util.Properties;

public class BenchmarkSessionGetter extends Executor implements Keyword{
    public BenchmarkSessionGetter()
    {}
    @Override
    public void handle(String keyword, GrammarType type) throws Exception
    {
        switch (type)
        {
            case FIRST_GRAMMAR:
                WpLog.recordLog(LogLevelConstant.WARN, "Need variable to storage session");
                break;
            case SECOND_GRAMMAR:
                WpLog.recordLog(LogLevelConstant.INFO, "START_BENCHMARK: %s", keyword);
                // Index 0 is variable name, 1 is keyword
                String[] keywordParts = Util.removeBlankElement(keyword.split(SignConstant.ASSIGNMENT_STR, 2));
                handleSecondGrammar(keywordParts[0], keywordParts[1]);
                break;
            case THIRD_GRAMMAR:
                WpLog.recordLog(LogLevelConstant.INFO, "START_BENCHMARK: %s", keyword);
                // Index 0 is data type and variable name, 1 is keyword
                keywordParts = Util.removeBlankElement(keyword.split(SignConstant.ASSIGNMENT_STR, 2));
                //keywordParts[0]是Connection conn2
                //keywordParts[1]是get_conn[master]
                // Index 0 is data type, 1 is variable name
                String[] decVar = Util.removeBlankElement(keywordParts[0].split("\\s"));
                //decVar[0]是Connection
                //decVar[1]是conn2
                handleThirdGrammar(decVar[0], decVar[1], keywordParts[1]);
                break;
            default:
                throw new Exception("Grammar error");
        }
    }
    /**
     *
     * @param variableName  The name of variable which will be assigned by keyword
     * @param keyword   Doesn't include variableName
     */
    private void handleSecondGrammar(String variableName, String keyword) throws Exception
    {
        if (!varValueMap.containsKey(variableName))
        {
            WpLog.recordLog(LogLevelConstant.ERROR,
                    "Use variable without declaring in %s line %d", caseFileName, lineNumber);
            throw new Exception();
        }
        String category = Util.removeBlankElement(keyword.split("\\[|]"))[1];
        varValueMap.put(variableName, getSession(keyword));
    }

    /**
     * 处理start_benchmark，把varValueMap中放入测试Case变量名称
     * varValueMap是继承父类Executor的静态变量，若子类没有重新定义静态变量，子类和父类共享静态变量
     * @param dataType  The data type of variable
     * @param variableName
     * @param keyword
     */
    private void handleThirdGrammar(String dataType, String variableName, String keyword) throws Exception
    {
        WpLog.recordLog(LogLevelConstant.INFO,"把测试Case变量名称"+variableName+"和得到的Session连接引用放入varValueMap");
        varValueMap.put(variableName, getSession(keyword));
    }

    public static Session getSession(String keyword) throws Exception
    {
        String[] parts = keyword.split("\\[|;|]");
        for(int i=0; i< parts.length; i++)
            parts[i] = parts[i].trim();
        String host = parts[1];
        int port = Integer.parseInt(parts[2]);
        String user = parts[3];

        JSch jsch = new JSch();// 创建JSch对象
        jsch.addIdentity("~/.ssh/id_rsa","");
        Session session = jsch.getSession(user, host, port);// 根据用户名、主机ip、端口号获取一个Session对象

        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);// 为Session对象设置properties
        session.setTimeout(60000000);// 设置超时
        session.connect();// 通过Session建立连接
        return session;
    }

}
