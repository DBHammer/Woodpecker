package edu.ecnu.woodpecker.performancetest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KillPortApp {
    public static Set<Integer> ports;
    
    /**
     * 验证此行是否为指定的端口，因为 findstr命令会是把包含的找出来，例如查找80端口，但是会把8099查找出来
     * @param str
     * @return
     */
    public static boolean validPort(String str){
        Pattern pattern = Pattern.compile("^ *[a-zA-Z]+ +\\S+");
        Matcher matcher = pattern.matcher(str);

        matcher.find();
        String find = matcher.group();
        int spstart = find.lastIndexOf(":");
        find = find.substring(spstart + 1);
        
        int port = 0;
        try {
            port = Integer.parseInt(find);
        } catch (NumberFormatException e) {
            System.out.println("Find wrong port:" + find);
            return false;
        }
        if(ports.contains(port)){
            return true;
        }else{
            return false;
        }
    }
    
    /**
     * 更换为一个Set，去掉重复的pid值
     * @param data
     */
    public static void kill(List<String> data){
        Set<Integer> pids = new HashSet<>();
        for (String line : data) {
            int offset = line.lastIndexOf(" ");
            String spid = line.substring(offset);
            spid = spid.replaceAll(" ", "");
            int pid = 0;
            try {
                pid = Integer.parseInt(spid);
            } catch (NumberFormatException e) {
                System.out.println("获取的进程号错误:" + spid);
            }
            pids.add(pid);
        }
    }
    
    /**
     * 一次性杀除所有的端口
     * @param pids
     */
    public static boolean killWithPort(int port){
        try {
            Runtime.getRuntime().exec("taskkill /F /pid " + port + "");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static List<String> read(InputStream in,String charset) throws IOException{
        List<String> data = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, charset));
        String line;
        while((line = reader.readLine()) != null){
            boolean validPort = validPort(line);
            if(validPort){
                data.add(line);
            }
        }
        reader.close();
        return data;
    }
}
