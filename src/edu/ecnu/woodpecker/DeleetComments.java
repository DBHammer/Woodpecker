package edu.ecnu.woodpecker;
import java.io.*;

/**
 * @author Insunny
 *
 */
public class DeleetComments{

    private static final char MARK = '"';

    private static final char SLASH = '/';

    private static final char BACKSLASH = '\\';

    private static final char STAR = '*';

    private static final char NEWLINE = '\n';

    //引号
    private static final int TYPE_MARK = 1;

    //斜杠
    private static final int TYPE_SLASH = 2;

    //反斜杠
    private static final int TYPE_BACKSLASH = 3;

    //星号
    private static final int TYPE_STAR = 4;

    // 双斜杠类型的注释
    private static final int TYPE_DSLASH = 5;

    /**
     * 删除char[]数组中_start位置到_end位置的元素
     *
     * @param _target
     * @param _start
     * @param _end
     * @return
     */
    public static char[] del(char[] _target, int _start, int _end) {
        char[] tmp = new char[_target.length - (_end - _start + 1)];
        System.arraycopy(_target, 0, tmp, 0, _start);
        System.arraycopy(_target, _end + 1, tmp, _start, _target.length - _end
                - 1);
        return tmp;
    }

    /**
     * 删除代码中的注释
     *
     * @param _target
     * @return
     */
    public static String delComments(String _target) {
        int preType = 0;
        int mark = -1, cur = -1, token = -1;
        // 输入字符串
        char[] input =  _target.toCharArray();
        for (cur = 0; cur < input.length; cur++) {
            if (input[cur] == MARK) {
                // 首先判断是否为转义引号
                if (preType == TYPE_BACKSLASH)
                    continue;
                // 已经进入引号之内
                if (mark > 0) {
                    // 引号结束
                    mark = -1;
                } else {
                    mark = cur;
                }
                preType = TYPE_MARK;
            } else if (input[cur] == SLASH) {
                // 当前位置处于引号之中
                if (mark > 0)
                    continue;
                // 如果前一位是*，则进行删除操作
                if (preType == TYPE_STAR) {
                    input = del(input, token, cur);
                    // 退回一个位置进行处理
                    cur = token - 1;
                    preType = 0;
                } else if (preType == TYPE_SLASH) {
                    token = cur - 1;
                    preType = TYPE_DSLASH;
                } else {
                    preType = TYPE_SLASH;
                }
            } else if (input[cur] == BACKSLASH) {
                preType = TYPE_BACKSLASH;
            } else if (input[cur] == STAR) {
                // 当前位置处于引号之中
                if (mark > 0)
                    continue;
                // 如果前一个位置是/,则记录注释开始的位置
                if (preType == TYPE_SLASH) {
                    token = cur - 1;
                }
                preType = TYPE_STAR;
            } else if(input[cur] == NEWLINE)
            {
                if(preType == TYPE_DSLASH)
                {
                    input = del(input, token, cur);
                    // 退回一个位置进行处理
                    cur = token - 1;
                    preType = 0;
                }
            }

        }
        return new String(input);
    }

    private static int count = 0;

    /**
     * 删除文件中的各种注释，包含//、/* * /等
     * @param charset 文件编码
     * @param file 文件
     */
    public static void clearComment(File file, String charset) {
        try {
//递归处理文件夹
            if (!file.exists()) {
                return;
            }

            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File f : files) {
                    clearComment(f, charset); //递归调用
                }
                return;
            } else if (!file.getName().endsWith(".java")) {
//非java文件直接返回
                return;
            }
            System.out.println("-----开始处理文件：" + file.getAbsolutePath());

//根据对应的编码格式读取
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));
            StringBuffer content = new StringBuffer();
            String tmp = null;
            while ((tmp = reader.readLine()) != null) {
                content.append(tmp);
                content.append("\n");
            }
            String target = content.toString();
//String s = target.replaceAll("\\/\\/[^\\n]*|\\/\\*([^\\*^\\/]*|[\\*^\\/*]*|[^\\**\\/]*)*\\*\\/", ""); //本段正则摘自网上，有一种情况无法满足（/* ...**/），略作修改
            String s = target.replaceAll("\\/\\/[^\\n]*|\\/\\*([^\\*^\\/]*|[\\*^\\/*]*|[^\\**\\/]*)*\\*+\\/", "");
//System.out.println(s);
//使用对应的编码格式输出
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), charset));
            out.write(s);
            out.flush();
            out.close();
            count++;
            System.out.println("-----文件处理完成---" + count);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void clearComment(String filePath, String charset) {
        clearComment(new File(filePath), charset);
    }

    public static void clearComment(String filePath) {
        clearComment(new File(filePath), "UTF-8");
    }

    public static void clearComment(File file) {
        clearComment(file, "UTF-8");
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            clearComment("/Users/zt-mbp/Desktop/stable_version/performancetest");
//            System.out.println("Hello");
//            File file = new File("./src/edu/ecnu/woodpecker/performancetest/AbstractResultForClient.java");
//            BufferedReader reader = new BufferedReader(new FileReader(file));
//            StringBuilder content = new StringBuilder();
//            String tmp = null;
//            while ((tmp = reader.readLine()) != null) {
//                content.append(tmp);
//                content.append("\n");
//            }
//            String target = content.toString();
//            System.out.println(delComments(target));
        } catch (Exception e) {
            System.out.println(e.getMessage());

        }
    }

}