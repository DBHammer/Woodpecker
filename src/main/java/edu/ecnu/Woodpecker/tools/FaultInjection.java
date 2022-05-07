package edu.ecnu.Woodpecker.tools;

import edu.ecnu.Woodpecker.constant.LogLevelConstant;
import edu.ecnu.Woodpecker.log.WpLog;
import edu.ecnu.Woodpecker.util.Util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Youshuhong
 * @create 2021/11/16 下午5:13
 */
public class FaultInjection {
    /**
     * servers' IP and SSH port
     */
    private int connectionPort = 22;

    public void seizeCPU(String keyword)
    {
        String[] parts = Util.removeBlankElement(keyword.split("\\[|;|]"));
        String ip = parts[1];
        String user = parts[2];
        Integer cores = Integer.parseInt(parts[3]);
        Integer time = Integer.parseInt(parts[4]);


        String src = "tools/FaultInjection/seizeCPU.sh";
        String dst = "/tmp/seizeCPU.sh";
        Util.put(ip, user, connectionPort, src, dst);

        String cmd = "bash " + dst + " " + cores + " " + time;
        String result = Util.exec(ip, user, connectionPort, cmd);
        WpLog.recordLog(LogLevelConstant.INFO, "CPU RESULT: "+result);
    }
    public void seizeMEM(String keyword)
    {
        String[] parts = Util.removeBlankElement(keyword.split("\\[|;|]"));
        String ip = parts[1];
        String user = parts[2];
        Integer size = Integer.parseInt(parts[3]);
        Integer time = Integer.parseInt(parts[4]);

        String src = "tools/FaultInjection/seizeMEM";
        String dst = "/tmp/seizeMEM.c";
        String exe_file = "seizeMEM";
        Util.put(ip, user, connectionPort, src, dst);

        String cmd = "cd " + dst.substring(0,dst.lastIndexOf("/"))
                + " && ./" + exe_file
                + " " + time + " " + size;
        String result = Util.exec(ip, user, connectionPort, cmd);
        WpLog.recordLog(LogLevelConstant.INFO, "MEM RESULT: "+result);
    }
    public void seizeDISK(String keyword)
    {
        String[] parts = Util.removeBlankElement(keyword.split("\\[|;|]"));
        String ip = parts[1];
        String user = parts[2];
        Integer IOPS = Integer.parseInt(parts[3]);
        Integer size = Integer.parseInt(parts[4]);
        Integer time = Integer.parseInt(parts[5]);

        String src = "tools/FaultInjection/seizeDISK.sh";
        String dst = "/tmp/seizeDISK.sh";
        Util.put(ip, user, connectionPort, src, dst);

        String cmd = "bash " + dst + " " + IOPS +" " + size + " " + time;
        String result = Util.exec(ip, user, connectionPort, cmd);
        WpLog.recordLog(LogLevelConstant.INFO, "DISK RESULT: "+result);

    }
    public void seizeNET(String keyword)
    {
        String[] parts = Util.removeBlankElement(keyword.split("\\[|;|]"));
        String transmitIP = parts[1];
        String user1 = parts[2];
        Integer IOPS = Integer.parseInt(parts[3]);
        Integer size = Integer.parseInt(parts[4]);
        String receiveIP = parts[5];
        String user2 = parts[6];
        Integer time = Integer.parseInt(parts[7]);

        String transmitsrc = "tools/FaultInjection/netClient.jar";
        String transmitdst = "/tmp/netClient.jar";
        Util.put(transmitIP, user1, connectionPort, transmitsrc, transmitdst);

        String receivesrc = "tools/FaultInjection/netServer.jar";
        String receivedst = "/tmp/netServer.jar";
        Util.put(receiveIP, user2, connectionPort, receivesrc, receivedst);

        int port = 1234;
        String transmitcmd = "java -jar " + transmitdst + " " + receiveIP + " " + port + " " + IOPS + " " + size + " " + time;
        String receivecmd = "java -jar " + receivedst + " " + port + " " + (time + 5);

       // Util.exec(receiveIP, user2, connectionPort, transmitcmd);
        Util.execInHosts(transmitIP, user1, receiveIP, user2, connectionPort, transmitcmd, receivecmd);



    }

    public void seizeFILE(String keyword) throws IOException {
        String[] parts = Util.removeBlankElement(keyword.split("\\[|;|]"));
        String ip = parts[1];
        String user = parts[2];
        String path = parts[3];

        String src = "tools/FaultInjection/seizeFILE.sh";
        String dst = "/tmp/seizeFILE.sh";

        Util.put(ip, user, connectionPort, src, dst);
        String cmd = "bash " + dst + " " + path;

        String result = Util.exec(ip, user, connectionPort, cmd);
        WpLog.recordLog(LogLevelConstant.INFO, "FILE RESULT: "+result);

    }
}
