package edu.ecnu.woodpecker.tools;

import edu.ecnu.woodpecker.constant.FileConstant;
import edu.ecnu.woodpecker.constant.SignConstant;
import edu.ecnu.woodpecker.controller.TestController;

/**
 * The tool to kill CEDAR servers
 * 
 */
public class CedarKiller implements Initializable
{
    /**
     * servers' IP and SSH port
     */
    private String IP = null;
    private int connectionPort = 22;

    private String serverUserName = null;
    private String serverPassword = null;

    /**
     * Kill servers
     */
    private void killCEDAR(String IP)
    {
        String cmd = "kill -9 `pgrep 'rootserver|chunkserver|mergeserver|updateserver' -u " + serverUserName + "`";
        edu.ecnu.woodpecker.util.Util.exec(IP, serverUserName, serverPassword, connectionPort, cmd);
    }

    public void start() throws Exception
    {
        TestController.initializeParameter();
        initialize(this, FileConstant.CEDAR_KILLER_CONFIG_PATH);
        String[] IPs = IP.split(SignConstant.COMMA_STR);
        for (String ele : IPs)
            killCEDAR(ele.trim());
        System.out.println("Off line all CEDAR servers belong to " + serverUserName + " in IP: " + IP);
    }

    public void setIP(String IP)
    {
        this.IP = IP;
    }

    public void setServerUserName(String serverUserName)
    {
        this.serverUserName = serverUserName;
    }

    public void setServerPassword(String serverPassword)
    {
        this.serverPassword = serverPassword;
    }

    /**
     * Tool's entry
     * 
     * @param args
     */
    public static void main(String[] args)
    {
        try
        {
            new CedarKiller().start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
