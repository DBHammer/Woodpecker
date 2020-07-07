package edu.ecnu.woodpecker.performancetest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import edu.ecnu.woodpecker.constant.ConfigConstant;
import edu.ecnu.woodpecker.constant.FileConstant;

public class DBConnection {
	private String IP = null;
	private int port;
    private String dbInstance = null;
    private String dbUser = null;
    private String dbPassword = null;
    private String DBMS = null;
    
    public DBConnection(String IP, int port, String dbInstance, 
    		String dbUser, String dbPassword, String DBMS)
    {
    	this.IP = IP;
    	this.port = port;
    	this.dbInstance = dbInstance;
    	this.dbUser = dbUser;
    	this.dbPassword = dbPassword;
    	this.DBMS = DBMS;
    }

    public Connection getDBConnection()
    {
        for (int i = 1; i < 99999; i++)
        {
            try
            {
                Connection connection = null;
                switch (DBMS.toLowerCase().trim())
                {
                case ConfigConstant.MYSQL:
                    connection = getMySQLConnection();
                    break;
                case ConfigConstant.POSTGRESQL:
                    connection = getPostgreSQLConnection();
                    break;
                case ConfigConstant.SQL_SERVER:
                    connection = getSQLServeConnection();
                    break;
                case ConfigConstant.CEDAR:
                    connection = getJDBCConnection();
                    break;
                case ConfigConstant.VOLTDB:
                    connection = getVoltDBConnection();
                    break;
                default:
                    throw new Exception("unsupport DBMS");
                }
                return connection;
            }
            catch (Exception e)
            {
            	e.printStackTrace();
//        		WpLog.recordLog(LogLevelConstant.DEBUG, "Can't connect to DB...Trying " + i + "-th connection", i);
                try
                {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e1)
                {}
            }
        }
        return null;
    }

    private Connection getMySQLConnection() throws Exception
    {
        Connection connection = null;
        String config = "?useServerPrepStmts=true&cachePrepStmts=true";
        String URL = "jdbc:mysql://" + IP + ":" + port + FileConstant.FILE_SEPARATOR + dbInstance + config;
        try
        {
            connection = DriverManager.getConnection(URL, dbUser, dbPassword);
        }
        catch (SQLException e)
        {
            if (e.getMessage().matches("Unknown database.*"))
            {
                connection = DriverManager.getConnection("jdbc:mysql://" + IP + ":" + port, dbUser, dbPassword);
                Statement statement = connection.createStatement();
                statement.executeUpdate("create database " + dbInstance);
                statement.executeQuery("use " + dbInstance);
            }
            else
            {
                throw e;
            }
        }
        return connection;
    }

    private Connection getVoltDBConnection() throws Exception
    {
        String URL = "jdbc:voltdb://" + IP + ":" + port + "?autoreconnect=true";
        Class.forName("org.voltdb.jdbc.Driver");
        return DriverManager.getConnection(URL, dbUser, dbPassword);
    }

    private Connection getJDBCConnection() throws Exception
    {
        String URL = "jdbc:mysql://" + IP + ":" + port + "/mysql?useServerPrepStmts=true";
        Class.forName("com.mysql.jdbc.Driver");
        Connection connection = DriverManager.getConnection(URL, dbUser, dbPassword);
        connection.createStatement().executeQuery("set @@session.ob_query_timeout=9000000000;");
        return connection;
    }

    private Connection getPostgreSQLConnection() throws Exception
    {
        Connection connection = null;
        String URL = "jdbc:postgresql://" + IP + ":" + port + "/" + dbInstance;
        try
        {
            connection = DriverManager.getConnection(URL, dbUser, dbPassword);
        }
        catch (Exception e)
        {
            if (e.getMessage().matches("Unknown database.*"))
            {
                connection = DriverManager.getConnection("jdbc:postgresql://" + IP + ":" + port, dbUser, dbPassword);
                Statement statement = connection.createStatement();
                statement.executeUpdate("create database " + dbInstance);
                statement.executeQuery("\\c " + dbInstance);
            }
            else
            {
                throw e;
            }
        }
        return connection;
    }

    private Connection getSQLServeConnection() throws Exception
    {
        String URL = "jdbc:microsoft:sqlserve://" + IP + ":" + port + ":1433;DatabaseName=" + dbInstance;
        Connection connection = DriverManager.getConnection(URL, dbUser, dbPassword);
        return connection;
    }
}
