package edu.ecnu.woodpecker.performancetest_test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class AutoAntPackage {

    /**
     * 该类调用ant工具来自动生成jar包
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        Process process;
        String buildPath = System.getProperty("user.dir") + "\\conf\\build.xml";
        String antPath = "C:\\apache-ant-1.10.5\\bin\\ant.bat";
        InputStream fInputStream;
        BufferedReader bReader;
        String line = null;

        process = Runtime.getRuntime().exec("cmd /c " + antPath + " -file " + buildPath);
        fInputStream = process.getInputStream();
        bReader = new BufferedReader(new InputStreamReader(fInputStream));
        while ((line = bReader.readLine()) != null) {
            System.out.println(line);
        }
    }
}
