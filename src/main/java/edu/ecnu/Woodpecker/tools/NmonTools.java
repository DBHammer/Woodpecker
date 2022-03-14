package edu.ecnu.Woodpecker.tools;

import edu.ecnu.Woodpecker.constant.LogLevelConstant;
import edu.ecnu.Woodpecker.controller.TestController;
import edu.ecnu.Woodpecker.log.WpLog;
import edu.ecnu.Woodpecker.util.Util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @Author: Chen Lixiang
 * @Email: lixiang3608@outlook.com
 * @Date: 2022/1/12
 */
public class NmonTools {

    private static final int connectionPort = 22;

    private static final String remoteFileName = "/home/ysh/nmon";

    private static List<String> hosts;

    public static void execRemoteNmon(String host, String user, String interval, String times)
    {
        if (hosts == null) {
            hosts = new ArrayList<>();
        }
        String cmd = "nmon -s " + interval + " -c " + times + " -F " + remoteFileName;
        WpLog.recordLog(LogLevelConstant.INFO, "开始执行nmon命令：" + cmd);
        String result = Util.exec(host, user, connectionPort, cmd);
        hosts.add(host + "&" + user);
        WpLog.recordLog(LogLevelConstant.INFO, "result：" + result);
    }

    public static void generateFiles()
    {
        if (hosts == null || hosts.isEmpty()) {
            return;
        }
        Calendar rightNow = Calendar.getInstance();
        Integer year = rightNow.get(Calendar.YEAR);
        Integer month = rightNow.get(Calendar.MONTH) + 1; //第一个月从0开始，所以得到月份＋1
        Integer day = rightNow.get(rightNow.DAY_OF_MONTH);
        Integer hour24 = rightNow.get(rightNow.HOUR_OF_DAY);
        Integer minute = rightNow.get(rightNow.MINUTE);
        Integer second = rightNow.get(rightNow.SECOND);
        String fileName = year+"-"+month+"-"+day+"-"+hour24+"-"+minute+"-"+second+".txt";
        for (String next : hosts) {
            String[] args = next.split("&");
            String host = args[0];
            String user = args[1];
            String path = TestController.getMonitorInfoPath() +
                    (TestController.getMonitorInfoPath().endsWith("/") ? "" : "/") +
                    host + "-" + user + "-" + fileName;
            Util.get(host, user, connectionPort, remoteFileName, path);
            Util.exec(host, user, connectionPort, "rm " + remoteFileName);
        }
        hosts = null;
    }
}
