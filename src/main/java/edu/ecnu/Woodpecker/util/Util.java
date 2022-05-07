package edu.ecnu.Woodpecker.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import com.jcraft.jsch.*;

import edu.ecnu.Woodpecker.constant.FileConstant;
import edu.ecnu.Woodpecker.constant.LogLevelConstant;
import edu.ecnu.Woodpecker.log.WpLog;

public class Util
{
    /**
     * Remove all blank element in array, include 0 length and null
     * 
     * @param arrays
     * @return
     */
    public static String[] removeBlankElement(String[] arrays)
    {
        Stream<String> stream = Stream.of(arrays);
        return stream.filter(ele -> ele != null).map(ele -> ele.trim()).filter(ele -> ele.length() != 0)
                .collect(() -> new ArrayList<>(), (list, item) -> list.add(item), (list1, list2) -> list1.addAll(list2))
                .toArray(new String[0]);
    }

    /**
     * Remote execute shell command
     * 
     * @param host The IP of specified host
     * @param user 账户名

     * @param port 端口，SSH连接默认22
     * @param command 命令
     * @return 命令的返回结果字符串
     */
    public static String exec(String host, String user, int port, String command)
    {
        StringBuilder result = new StringBuilder();
        Session session = null;
        ChannelExec openChannel = null;
        try
        {
            JSch jsch = new JSch();
            jsch.addIdentity("~/.ssh/id_rsa");
            session = jsch.getSession(user, host, port);
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            int timeout = 60000000;
            session.setTimeout(timeout);
            session.connect();

            openChannel = (ChannelExec) session.openChannel("exec");
            openChannel.setCommand(command);
            openChannel.connect();
            InputStream in = openChannel.getInputStream();
            WpLog.recordLog(LogLevelConstant.INFO, "运行命令: "+command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String buf = null;
            while ((buf = reader.readLine()) != null)
            {
                result.append(new String(buf.getBytes("gbk"), FileConstant.UTF_8)).append(FileConstant.LINUX_LINE_FEED);
            }
            reader.close();
        }
        catch (JSchException | IOException e)
        {
            result.append(WpLog.getExceptionInfo(e));
        }
        finally
        {
            if (openChannel != null && !openChannel.isClosed())
                openChannel.disconnect();
            if (session != null && session.isConnected())
                session.disconnect();
        }
        return result.toString();
    }

    public static String execInHosts(String host1, String user1, String host2, String user2, int port, String command1,String command2 ){
        StringBuilder result = new StringBuilder();
        Session session1 = null;
        Session session2 = null;
        ChannelExec openChannel1 = null;
        ChannelExec openChannel2 = null;
        try
        {
            JSch jsch = new JSch();
            jsch.addIdentity("~/.ssh/id_rsa");
            session1 = jsch.getSession(user1, host1, port);
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session1.setConfig(config);
            int timeout = 60000000;
            session1.setTimeout(timeout);
            session1.connect();

            jsch.addIdentity("~/.ssh/id_rsa");
            session2 = jsch.getSession(user2, host2, port);
            Properties config2 = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session2.setConfig(config);
            int timeout2 = 60000000;
            session2.setTimeout(timeout);
            session2.connect();

            openChannel1 = (ChannelExec) session1.openChannel("exec");
            openChannel1.setCommand(command1);
            openChannel1.connect();
            InputStream in1 = openChannel1.getInputStream();
            WpLog.recordLog(LogLevelConstant.INFO, "运行命令: "+command1);

            openChannel2 = (ChannelExec) session2.openChannel("exec");
            openChannel2.setCommand(command2);
            openChannel2.connect();
            InputStream in2 = openChannel2.getInputStream();
            WpLog.recordLog(LogLevelConstant.INFO, "运行命令: "+command2);

            BufferedReader reader = new BufferedReader(new InputStreamReader(in1));
            String buf = null;
            while ((buf = reader.readLine()) != null)
            {
                result.append(new String(buf.getBytes("gbk"), FileConstant.UTF_8)).append(FileConstant.LINUX_LINE_FEED);
            }
            reader.close();
        }
        catch (JSchException | IOException e)
        {
            result.append(WpLog.getExceptionInfo(e));
        }
        finally
        {
            if (openChannel1 != null && !openChannel1.isClosed() && openChannel2 != null && !openChannel2.isClosed())
                openChannel1.disconnect();
                openChannel2.disconnect();
            if (session1 != null && session1.isConnected() && session2 != null && session2.isConnected())
                session1.disconnect();
                session2.disconnect();
        }
        return result.toString();
    }

    public static String execCmd(Session session, String command) {
        StringBuilder result = new StringBuilder();
        BufferedReader reader = null;
        Channel channel = null;
        String path_cmd = "bash --login -c '" ;//使用命令行执行
        path_cmd = path_cmd + command + "'";
        try {
            if (command != null) {
                channel = session.openChannel("exec");

                ((ChannelExec) channel).setCommand(path_cmd);
                // ((ChannelExec) channel).setErrStream(System.err);
                channel.connect();
                WpLog.recordLog(LogLevelConstant.INFO, "command executing");

                InputStream in = channel.getInputStream();
                reader = new BufferedReader(new InputStreamReader(in));
                String buf = null;
                while ((buf = reader.readLine()) != null) {
                    System.out.println(buf);
                    result.append(new String(buf.getBytes("gbk"), FileConstant.UTF_8)).append(FileConstant.LINUX_LINE_FEED);
                }
            }
        } catch (JSchException | IOException e)
        {
            result.append(WpLog.getExceptionInfo(e));
        }finally {
            try {
                reader.close();
            } catch (IOException e) {
                result.append(WpLog.getExceptionInfo(e));
            }
            if (channel != null && !channel.isClosed())
                channel.disconnect();
        }
        return result.toString();
    }

    /**
     * 用于获取服务器上的文件
     *
     * @param host The IP of specified host
     * @param user 账户
     * @param port 端口，SSH连接默认22
     */
    public static void get(String host, String user, int port, String src, String dst)
    {
        Session session = null;
        ChannelSftp openChannel = null;
        try
        {
            JSch jsch = new JSch();
            jsch.addIdentity("~/.ssh/id_rsa","");
            session = jsch.getSession(user, host, port);
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            int timeout = 60000000;
            session.setTimeout(timeout);
            session.connect();

            openChannel = (ChannelSftp) session.openChannel("sftp");
            openChannel.connect();
            openChannel.get(src, dst);
        }
        catch (JSchException | SftpException e)
        {
            WpLog.recordLog(LogLevelConstant.ERROR, WpLog.getExceptionInfo(e));
            e.printStackTrace();
        }
        finally
        {
            if (openChannel != null && !openChannel.isClosed())
                openChannel.disconnect();
            if (session != null && session.isConnected())
                session.disconnect();
        }
    }

    /**
     * 用于服务器间上传文件
     * 
     * @param host The IP of specified host
     * @param user 账户
     * @param port 端口，SSH连接默认22
     */
    public static void put(String host, String user, int port, String src, String dst)
    {
        Session session = null;
        ChannelSftp openChannel = null;
        try
        {
            JSch jsch = new JSch();
            jsch.addIdentity("~/.ssh/id_rsa","");
            session = jsch.getSession(user, host, port);
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            int timeout = 60000000;
            session.setTimeout(timeout);
            session.connect();

            openChannel = (ChannelSftp) session.openChannel("sftp");
            openChannel.connect();
			openChannel.put(src, dst, ChannelSftp.OVERWRITE);
        }
        catch (JSchException | SftpException e)
        {
            WpLog.recordLog(LogLevelConstant.ERROR, WpLog.getExceptionInfo(e));
            e.printStackTrace();
        }
        finally
        {
            if (openChannel != null && !openChannel.isClosed())
                openChannel.disconnect();
            if (session != null && session.isConnected())
                session.disconnect();
        }
        return;
    }


    /**
     * Return local host IP address, which is not one of
     * site local, link local, virtual, loopback
     * 
     * @return
     * @throws SocketException
     */
    public static Optional<String> getLocalHostAddress() throws SocketException
    {
        for (Enumeration<NetworkInterface> localNetworkInterfaces = NetworkInterface.getNetworkInterfaces(); localNetworkInterfaces
                .hasMoreElements();)
        {
            NetworkInterface networkInterface = localNetworkInterfaces.nextElement();
            if (networkInterface.isLoopback() || networkInterface.isVirtual() || !networkInterface.isUp())
                continue;
            for (Enumeration<InetAddress> inetAddrEnum = networkInterface.getInetAddresses(); inetAddrEnum.hasMoreElements();)
            {
                try
                {
                    Inet4Address inetAddress = (Inet4Address) inetAddrEnum.nextElement();
                    if (!inetAddress.isSiteLocalAddress() && !inetAddress.isLinkLocalAddress())
                        return Optional.of(inetAddress.getHostAddress());
                }
                catch (Exception e)
                {}
            }
        }
        return Optional.empty();
    }

    /**
     *
     */
    public static ProcessExecInfo execCommand(String cmd) {
        try {
            String[] cmdList = { "/bin/sh", "-c", cmd };
            Process process = Runtime.getRuntime().exec(cmdList);

            BufferedReader stdInput = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );
            BufferedReader errInput = new BufferedReader(
                    new InputStreamReader(process.getErrorStream())
            );
            StringBuilder stdBuilder = new StringBuilder();
            StringBuilder errBuilder = new StringBuilder();
            String s = null;
            while ((s = stdInput.readLine()) != null) {
                stdBuilder.append(s).append('\n');
            }
            while ((s = errInput.readLine()) != null) {
                errBuilder.append(s).append('\n');
            }

            // 等待进程完全执行结束
            // TODO: 超时设置
            process.waitFor();

            return new ProcessExecInfo(process.exitValue(), errBuilder.toString(), stdBuilder.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void createOrDropDatabase(String ip, String port, String dbName, String user, String password, Boolean flag){
        String url = "jdbc:mysql://" + ip + ":" + port + "/?useSSL=false" ;
        Connection conn = null;
        String driver = "com.mysql.jdbc.Driver";
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(url, user, password);
            if (conn != null) {
                WpLog.recordLog(LogLevelConstant.INFO, "数据库连接成功");
            } else {
                WpLog.recordLog(LogLevelConstant.INFO, "数据库连接失败");
            }
            Statement statement = conn.createStatement();
            String dropDB = "drop database if exists " + dbName;
            statement.execute(dropDB);

            if(flag) {
                String createDB = "create database " + dbName;
                statement.execute(createDB);
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
