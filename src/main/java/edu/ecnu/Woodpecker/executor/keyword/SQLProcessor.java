package edu.ecnu.Woodpecker.executor.keyword;

import java.math.BigDecimal;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import javax.servlet.jsp.jstl.sql.Result;
import edu.ecnu.Woodpecker.constant.DataTypeConstant;
import edu.ecnu.Woodpecker.constant.LogLevelConstant;
import edu.ecnu.Woodpecker.constant.SignConstant;
import edu.ecnu.Woodpecker.executor.Executor;
import edu.ecnu.Woodpecker.executor.GrammarType;
import edu.ecnu.Woodpecker.executor.SQLKeywordOperator;
import edu.ecnu.Woodpecker.log.WpLog;
import edu.ecnu.Woodpecker.sql.BasicSQLOperation;
import edu.ecnu.Woodpecker.util.Util;

/**
 * The class handle SQL keyword
 *
 */
public class SQLProcessor extends Executor implements Keyword
{
    public SQLProcessor()
    {
    }

    @Override
    public void handle(String keyword, GrammarType type) throws Exception
    {
        WpLog.recordLog(LogLevelConstant.INFO, "SQL: %s", keyword);
        switch (type)
        {
            case FIRST_GRAMMAR:
                String[] parts = Util.removeBlankElement(keyword.split("\"|;|]"));
                // 避免删除SQL中的分号
                //part[]= sql[ drop table if exists t1 stat
                StringBuilder sql = new StringBuilder(parts[1]).append(SignConstant.SEMICOLON_CHAR);
                if (parts[parts.length - 2].equals("ERROR"))
                {
                    for (int i = 2; i < parts.length - 3; i++)
                    {
                        sql.append(parts[i]).append(SignConstant.SEMICOLON_CHAR);
                    }
                    executeSQL(sql.toString(), parts[parts.length - 3],
                            SQLKeywordOperator.valueOf(getSQLOperator(sql.toString(), parts)));
                    contain(exceptionString, parts[parts.length - 1]);
                }
                else if (parts[parts.length - 1].equals("ERROR"))
                {
                    for (int i = 2; i < parts.length - 2; i++)
                    {
                        sql.append(parts[i]).append(SignConstant.SEMICOLON_CHAR);
                    }
                    executeSQL(sql.toString(), parts[parts.length - 2],
                            SQLKeywordOperator.valueOf(getSQLOperator(sql.toString(), parts)));
                }
                else
                {
                    for (int i = 2; i < parts.length - 1; i++)
                    {
                        sql.append(parts[i]).append(SignConstant.SEMICOLON_CHAR);
                    }
                    WpLog.recordLog(LogLevelConstant.INFO,"准备执行的SQL语句是"+sql.toString());

                    executeSQL(sql.toString(), parts[parts.length - 1],
                            SQLKeywordOperator.valueOf(getSQLOperator(sql.toString(), parts)));
                }
                break;
            case SECOND_GRAMMAR:
                // Index 0 is variable name, 1 is keyword
                String[] keywordParts = Util
                        .removeBlankElement(keyword.split(SignConstant.ASSIGNMENT_STR, 2));
                handleSecondGrammar(keywordParts[0], keywordParts[1]);
                break;
            case THIRD_GRAMMAR:
                // Index 0 is data type and variable name, 1 is keyword
                keywordParts = Util.removeBlankElement(keyword.split(SignConstant.ASSIGNMENT_STR, 2));
                // Index 0 is data type, 1 is variable name
                String[] decVar = Util.removeBlankElement(keywordParts[0].split("\\s"));
                handleThirdGrammar(decVar[0], decVar[1], keywordParts[1]);
                break;
            default:
                throw new Exception("Grammar error");
        }
    }

    /**
     *
     * @param variableName
     * @param keyword Starts with "sql["
     * @throws Exception
     */
    private void handleSecondGrammar(String variableName, String keyword) throws Exception
    {
        if (!varValueMap.containsKey(variableName))
        {
            WpLog.recordLog(LogLevelConstant.ERROR, "Use variable without declaring in %s line %d",
                    caseFileName, lineNumber);
            throw new Exception();
        }

        String[] parts = Util.removeBlankElement(keyword.split("\"|;|]"));
        // Avoid erasing semicolon in sql
        StringBuilder sql = new StringBuilder(parts[1]).append(SignConstant.SEMICOLON_CHAR);
        if (parts[parts.length - 2].equals("ERROR"))
        {
            for (int i = 2; i < parts.length - 3; i++)
            {
                sql.append(parts[i]).append(SignConstant.SEMICOLON_CHAR);
            }
        }
        else if (parts[parts.length - 1].equals("ERROR"))
        {
            for (int i = 2; i < parts.length - 2; i++)
            {
                sql.append(parts[i]).append(SignConstant.SEMICOLON_CHAR);
            }
        }
        else
        {
            for (int i = 2; i < parts.length - 1; i++)
            {
                sql.append(parts[i]).append(SignConstant.SEMICOLON_CHAR);
            }
        }
        // for (int i = 2; i < parts.length - 2; i++)
        // sql.append(parts[i]).append(SignConstant.SEMICOLON_CHAR);
        SQLKeywordOperator operator = SQLKeywordOperator
                .valueOf(getSQLOperator(sql.toString(), parts));

        switch (operator)
        {
            case READ:
                // executeQuery
                executeSQL(sql.toString(), parts[parts.length - 1], operator);
                dealDiffType(variableName, result);
//            varValueMap.put(variableName, result);
//            result = null;
                break;

            case WRITE:
                // executeUpdate
                String dataType = varTypeMap.get(variableName);
                if (!dataType.equals(DataTypeConstant.INT_SHORT)
                        || !dataType.equals(DataTypeConstant.LONG_SHORT)
                        || !dataType.equals(DataTypeConstant.FLOAT_SHORT)
                        || !dataType.equals(DataTypeConstant.DOUBLE_SHORT))
                    throw new Exception(
                            String.format("Variable's data type don't match keyword in %s line %d",
                                    caseFileName, lineNumber));
                varValueMap.put(variableName,
                        executeSQL(sql.toString(), parts[parts.length - 1], operator));
                break;

            case READ_ERROR:
                // -1 is executeQuery and exist exception
                if (parts[parts.length - 2].equals("ERROR"))
                {
                    executeSQL(sql.toString(), parts[parts.length - 3], operator);
                    varValueMap.put(variableName, exceptionString);
                    contain(exceptionString, parts[parts.length - 1]);
                }
                else
                {
                    executeSQL(sql.toString(), parts[parts.length - 2], operator);
                }
                exceptionString = null;
                result = null;
                break;

            case WRITE_ERROR:
                // -2 is executeUpdate and exist exception
                if (!varTypeMap.get(variableName).equals(DataTypeConstant.STRING_SHORT))
                    throw new Exception(
                            String.format("Variable's data type don't match keyword in %s line %d",
                                    caseFileName, lineNumber));
                if (parts[parts.length - 2].equals("ERROR"))
                {
                    executeSQL(sql.toString(), parts[parts.length - 3], operator);
                    varValueMap.put(variableName, exceptionString);
                    contain(exceptionString, parts[parts.length - 1]);
                }
                else
                {
                    executeSQL(sql.toString(), parts[parts.length - 2], operator);
                }
                exceptionString = null;
                break;

            case EXPLAIN:
                if (!varTypeMap.get(variableName).equals(DataTypeConstant.STRING_SHORT))
                    throw new Exception(
                            String.format("Variable's data type don't match keyword in %s line %d",
                                    caseFileName, lineNumber));
                executeSQL(sql.toString(), parts[parts.length - 1], operator);
                varValueMap.put(variableName, result.getRowsByIndex()[0][0]);
                result = null;
                break;

            case EXPLAIN_ERROR:

                if (!varTypeMap.get(variableName).equals(DataTypeConstant.STRING_SHORT))
                    throw new Exception(
                            String.format("Variable's data type don't match keyword in %s line %d",
                                    caseFileName, lineNumber));
                if (parts[parts.length - 2].equals("ERROR"))
                {
                    executeSQL(sql.toString(), parts[parts.length - 3], operator);
                    varValueMap.put(variableName, exceptionString);
                    contain(exceptionString, parts[parts.length - 1]);
                }
                else
                {
                    executeSQL(sql.toString(), parts[parts.length - 2], operator);
                }
                exceptionString = null;
                break;

            default:
                throw new Exception(String.format("Use SQL undefined type in %s line %d", caseFileName,
                        lineNumber));
        }
    }

    /**
     *
     * @param dataType The data type of variable
     * @param variableName
     * @param keyword Starts with "sql["
     * @throws Exception
     */
    private void handleThirdGrammar(String dataType, String variableName, String keyword)
            throws Exception
    {
        String[] parts = Util.removeBlankElement(keyword.split("\"|;|]"));
        // Avoid erasing semicolon in sql
        StringBuilder sql = new StringBuilder(parts[1]).append(SignConstant.SEMICOLON_CHAR);
        if (parts[parts.length - 2].equals("ERROR"))
        {
            for (int i = 2; i < parts.length - 3; i++)
            {
                sql.append(parts[i]).append(SignConstant.SEMICOLON_CHAR);
            }
        }
        else if (parts[parts.length - 1].equals("ERROR"))
        {
            for (int i = 2; i < parts.length - 2; i++)
            {
                sql.append(parts[i]).append(SignConstant.SEMICOLON_CHAR);
            }
        }
        else
        {
            for (int i = 2; i < parts.length - 1; i++)
                sql.append(parts[i]).append(SignConstant.SEMICOLON_CHAR);
        }
        WpLog.recordLog(LogLevelConstant.INFO, "SQL语句为："+sql.toString());
        // for (int i = 2; i < parts.length - 2; i++)
        // sql.append(parts[i]).append(SignConstant.SEMICOLON_CHAR);
        SQLKeywordOperator operator = SQLKeywordOperator
                .valueOf(getSQLOperator(sql.toString(), parts));

        switch (operator)
        {
            case READ:
                executeSQL(sql.toString(), parts[parts.length - 1], operator);
                //result也是继承Executor的静态变量
                dealDiffType(variableName, result);
                break;

            case WRITE:
                // executeUpdate
                if (!dataType.equals(DataTypeConstant.INT_SHORT)
                        && !dataType.equals(DataTypeConstant.LONG_SHORT)
                        && !dataType.equals(DataTypeConstant.FLOAT_SHORT)
                        && !dataType.equals(DataTypeConstant.DOUBLE_SHORT))
                    throw new Exception(
                            String.format("Variable's data type don't match keyword in %s line %d",
                                    caseFileName, lineNumber));
                varValueMap.put(variableName,
                        executeSQL(sql.toString(), parts[parts.length - 1], operator));
                break;

            case READ_ERROR:
                // executeQuery and exist exception
                //SQL[sql;stat;ERROR;err_message]
                if (parts[parts.length - 2].equals("ERROR"))
                {
                    executeSQL(sql.toString(), parts[parts.length - 3], operator);
                    varValueMap.put(variableName, exceptionString);
                    contain(exceptionString, parts[parts.length - 1]);
                }
                else
                    executeSQL(sql.toString(), parts[parts.length - 2], operator);
                exceptionString = null;
                result = null;
                break;

            case WRITE_ERROR:
                // executeUpdate and exist exception
                if (!varTypeMap.get(variableName).equals(DataTypeConstant.STRING_SHORT))
                    throw new Exception(
                            String.format("Variable's data type don't match keyword in %s line %d",
                                    caseFileName, lineNumber));
                if (parts[parts.length - 2].equals("ERROR"))
                {
                    executeSQL(sql.toString(), parts[parts.length - 3], operator);
                    varValueMap.put(variableName, exceptionString);
                    contain(exceptionString, parts[parts.length - 1]);
                }
                else
                    executeSQL(sql.toString(), parts[parts.length - 2], operator);
                exceptionString = null;
                break;

            case EXPLAIN:
                if (!varTypeMap.get(variableName).equals(DataTypeConstant.STRING_SHORT))
                    throw new Exception(
                            String.format("Variable's data type don't match keyword in %s line %d",
                                    caseFileName, lineNumber));
                executeSQL(sql.toString(), parts[parts.length - 1], operator);
                varValueMap.put(variableName, result.getRowsByIndex()[0][0]);
                result = null;
                break;

            case EXPLAIN_ERROR:
                if (!varTypeMap.get(variableName).equals(DataTypeConstant.STRING_SHORT))
                    throw new Exception(
                            String.format("Variable's data type don't match keyword in %s line %d",
                                    caseFileName, lineNumber));
                if (parts[parts.length - 2].equals("ERROR"))
                {
                    executeSQL(sql.toString(), parts[parts.length - 3], operator);
                    varValueMap.put(variableName, exceptionString);
                    contain(exceptionString, parts[parts.length - 1]);
                }
                else
                    executeSQL(sql.toString(), parts[parts.length - 2], operator);
                exceptionString = null;
                break;

            default:
                throw new Exception(String.format("Use SQL undefined type in %s line %d", caseFileName,
                        lineNumber));
        }
    }

    /**
     * @param firstParameter
     * @param secondParameter
     * @throws Exception
     */
    public static void contain(String firstParameter, String secondParameter) throws Exception
    {
        StringBuilder origin = new StringBuilder(firstParameter);
        int fromIndex = 0;
        fromIndex = origin.indexOf(secondParameter, fromIndex);
        if (fromIndex == -1)
        {
            throw new Exception("The error is different from expected error!");
        }
//        WpLog.recordLog(LogLevelConstant.INFO,
//                "The origin string is equal to specified number");
        return;
    }

    /**
     * 得到SQL语句的类型，是select类型 、 explain、等
     * @param sql
     * @return sqlOperator
     */
    public static int getSQLOperator(String sql, String[] parts) throws Exception
    {
        String sqlType = sql.substring(sql.indexOf("\"") + 1, sql.indexOf(" "));
        int sqlOperator = 0;
        if (sqlType.contains("select") || sqlType.contains("SELECT"))
        {
            if (parts[parts.length - 1].equals("ERROR") || parts[parts.length - 2].equals("ERROR")){
                sqlOperator = -1;
            }
            else{
                sqlOperator = 1;
            }
            return sqlOperator;
        }
        else if (sqlType.contains("explain") || sqlType.contains("EXPLAIN"))
        {
            if (parts[parts.length - 1].equals("ERROR") || parts[parts.length - 2].equals("ERROR")){
                sqlOperator = -3;
            }
            else{
                sqlOperator = 3;
            }
            return sqlOperator;
        }
        else{
            if (parts[parts.length - 1].equals("ERROR") || parts[parts.length - 2].equals("ERROR")){
                sqlOperator = -2;
            }
            else{
                sqlOperator = 2;
            }
            return sqlOperator;
        }
    }


    /**
     *
     * @param sql
     * @param statementName
     * @param operator Execute type
     * @return Rows if type is executeUpdate, otherwise return -1
     * @throws Exception
     */
    public static int executeSQL(String sql, String statementName, SQLKeywordOperator operator)
            throws Exception
    {
        /**
         * ��ģ��shell�б�����Ŀǰֻ֧��һ��������������ʽ��$׃����ͬ�r׃����һ��Ҫ��һ���ո�
         */
        int index_$ = sql.indexOf("$");
        int index_blank = sql.indexOf(" ", index_$);
        String var = sql.substring(index_$ + 1, index_blank);
        if (varValueMap.containsKey(var))
        {
            sql = sql.replace('$' + var, varValueMap.get(var).toString());
        }

        WpLog.recordLog(LogLevelConstant.INFO,"statement名称是："+statementName);
        //从varValueMap中取出测试Case对应statement变量对应的数据库执行Statement引用
        Statement statement = (Statement) varValueMap.get(statementName);//向下转型
        int rows = -1;
        switch (operator)
        {
            case READ:
                result = BasicSQLOperation.stmtExecuteQuery(statement, sql, false);
                break;
            case WRITE:
                rows = BasicSQLOperation.stmtExecuteUpdate(statement, sql, false);
                break;
            case READ_ERROR:
                // executeQuery and will throw exception
                BasicSQLOperation.stmtExecuteQuery(statement, sql, true);
                break;
            case WRITE_ERROR:
                // executeUpdate and will throw exception
                BasicSQLOperation.stmtExecuteUpdate(statement, sql, true);
                break;
            case EXPLAIN:
                // The goal is row 0 column 0 of result
                result = BasicSQLOperation.stmtExecuteQuery(statement, sql, false);
                break;
            case EXPLAIN_ERROR:
                BasicSQLOperation.stmtExecuteQuery(statement, sql, true);
                break;
            default:
                throw new Exception(
                        String.format("Wrong type in %s line %d", caseFileName, lineNumber));
        }
        return rows;
    }


    public static void dealDiffType(String varName, Result result) throws Exception
    {
        SortedMap[] sortMap=result.getRows();

        // for(int i=0;i<result.getColumnNames().length;i++)
        //     System.out.println(result.getColumnNames()[i]);

        //System.out.println("result="+result.getRowCount());
        String columnAndValue=sortMap[0].toString().substring(1, sortMap[0].toString().length()-1);//第一行的值
        //System.out.println(columnAndValue);//c1=3, c2=1, c3=-3, c4=-1, pk=1
        String[] value=columnAndValue.split("=");
        //System.out.println(value[1]);
        switch (varTypeMap.get(varName))
        {
            case DataTypeConstant.INT_SHORT:
                varValueMap.put(varName, Integer.valueOf(value[1]));
                result=null;
                break;
            case DataTypeConstant.BOOLEAN_SHORT:
                varValueMap.put(varName, Boolean.valueOf(value[1]));
                result=null;
                break;
            case DataTypeConstant.LONG_SHORT:
                varValueMap.put(varName, Long.valueOf(value[1]));
                result=null;
                break;
            case DataTypeConstant.FLOAT_SHORT:
                varValueMap.put(varName, Float.valueOf(value[1]));
                result=null;
                break;
            case DataTypeConstant.DOUBLE_SHORT:
                varValueMap.put(varName, Double.valueOf(value[1]));
                result=null;
                break;
            case DataTypeConstant.STRING_SHORT:
                varValueMap.put(varName, value[1]);
                result=null;
                break;
            case DataTypeConstant.DECIMAL_SHORT:
                varValueMap.put(varName, new BigDecimal(value[1]));
                result=null;
                break;
            case DataTypeConstant.RESULT_SET:
                //如果是resultSet类型的变量，把result放入varValueMap
                WpLog.recordLog(LogLevelConstant.INFO,"变量"+varName+"的类型:"+"ResultSet");
                varValueMap.put(varName, result);
                result=null;
                break;
            case DataTypeConstant.LIST_INTGER:
                List<Integer> listInteger=new ArrayList<Integer>();
                sortMap=result.getRows();
                for(int i=0;i<result.getRowCount();i++)
                {
                    columnAndValue=sortMap[i].toString().substring(1, sortMap[0].toString().length()-1);
                    value=columnAndValue.split("=");
                    if(value.length>2)
                    {
                        throw new Exception("Resultset is more than one column!!!");
                    }
                    listInteger.add(Integer.valueOf(value[1]));
                }
                varValueMap.put(varName, listInteger);
                result=null;
                break;
            case DataTypeConstant.LIST_FLOAT:
                List<Float> listFloat=new ArrayList<Float>();
                sortMap=result.getRows();
                for(int i=0;i<result.getRowCount();i++)
                {
                    columnAndValue=sortMap[i].toString().substring(1, sortMap[0].toString().length()-1);
                    value=columnAndValue.split("=");
                    if(value.length>2)
                    {
                        throw new Exception("Resultset is more than one column!!!");
                    }
                    listFloat.add(Float.valueOf(value[1]));
                }
                varValueMap.put(varName, listFloat);
                result=null;
                break;
            case DataTypeConstant.LIST_DOUBLE:
                List<Double> listDouble=new ArrayList<Double>();
                sortMap=result.getRows();
                for(int i=0;i<result.getRowCount();i++)
                {
                    columnAndValue=sortMap[i].toString().substring(1, sortMap[0].toString().length()-1);
                    value=columnAndValue.split("=");
                    if(value.length>2)
                    {
                        throw new Exception("Resultset is more than one column!!!");
                    }
                    listDouble.add(Double.valueOf(value[1]));
                }
                varValueMap.put(varName, listDouble);
                result=null;
                break;
            case DataTypeConstant.LIST_BOOLEAN:
                List<Boolean> listBoolean=new ArrayList<Boolean>();
                sortMap=result.getRows();
                for(int i=0;i<result.getRowCount();i++)
                {
                    columnAndValue=sortMap[i].toString().substring(1, sortMap[0].toString().length()-1);
                    value=columnAndValue.split("=");
                    if(value.length>2)
                    {
                        throw new Exception("Resultset is more than one column!!!");
                    }
                    listBoolean.add(Boolean.valueOf(value[1]));
                }
                varValueMap.put(varName, listBoolean);
                result=null;
                break;
            case DataTypeConstant.LIST_BIGDECIMAL:
                List<BigDecimal> listBigDecimal=new ArrayList<BigDecimal>();
                sortMap=result.getRows();
                for(int i=0;i<result.getRowCount();i++)
                {
                    columnAndValue=sortMap[i].toString().substring(1, sortMap[0].toString().length()-1);
                    value=columnAndValue.split("=");
                    if(value.length>2)
                    {
                        throw new Exception("Resultset is more than one column!!!");
                    }
                    listBigDecimal.add(new BigDecimal(value[1].toString()));
                }
                varValueMap.put(varName, listBigDecimal);
                result=null;
                break;
            case DataTypeConstant.LIST_LONG:
                List<Long> listLong=new ArrayList<Long>();
                sortMap=result.getRows();
                for(int i=0;i<result.getRowCount();i++)
                {
                    columnAndValue=sortMap[i].toString().substring(1, sortMap[0].toString().length()-1);
                    value=columnAndValue.split("=");
                    if(value.length>2)
                    {
                        throw new Exception("Resultset is more than one column!!!");
                    }
                    listLong.add(Long.valueOf(value[1]));
                }
                varValueMap.put(varName, listLong);
                result=null;
                break;
            case DataTypeConstant.LIST_STRING:
                List<String> listStirng=new ArrayList<String>();
                sortMap=result.getRows();
                for(int i=0;i<result.getRowCount();i++)
                {
                    columnAndValue=sortMap[i].toString().substring(1, sortMap[0].toString().length()-1);
                    value=columnAndValue.split("=");
                    if(value.length>2)
                    {
                        throw new Exception("Resultset is more than one column!!!");
                    }
                    listStirng.add(value[1]);
                }
                varValueMap.put(varName, listStirng);
                result=null;
                break;
            default:
                result=null;
                throw new Exception ("Unsupported Datatype!");
        }
    }
}
