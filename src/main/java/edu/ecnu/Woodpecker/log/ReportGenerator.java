package edu.ecnu.Woodpecker.log;


import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.pdf.converter.PdfConverterExtension;
import com.vladsch.flexmark.profile.pegdown.Extensions;
import com.vladsch.flexmark.profile.pegdown.PegdownOptionsAdapter;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;


import java.io.File;
import java.io.IOException;
import java.util.Calendar;

/**
 * @Author: Chen Lixiang
 * @Email: lixiang3608@outlook.com
 * @Date: 2021/12/5
 */
public class ReportGenerator {

    private StringBuilder benchmarkInfoBuilder;

    private StringBuilder generalInfoBuilder;

    private String reportPath;

    final private static DataHolder OPTIONS = PegdownOptionsAdapter.flexmarkOptions(
            Extensions.NONE
    );

    final private MutableDataSet myOptions = new MutableDataSet();

    public ReportGenerator(String reportPath) {
        this.reportPath = reportPath;
        benchmarkInfoBuilder = new StringBuilder();
        generalInfoBuilder = new StringBuilder();
    }

    public ReportGenerator appendNewBenchmark(String benchName, String info) {
         benchmarkInfoBuilder.append("### ").append(benchName).append("\n");
         appendBenchmarkDetail(info);
         return this;
    }

    public ReportGenerator appendBenchmarkDetail(String info) {
        benchmarkInfoBuilder.append(info).append("\n");
        return this;
    }

    public ReportGenerator appendGeneralInfo(String info) {
        generalInfoBuilder.append(info).append("\n");
        return this;
    }

    public void generatePDF(String case_group_name) {
        final Parser PARSER = Parser.builder(myOptions).build();
        final HtmlRenderer RENDERER = HtmlRenderer.builder(myOptions).build();

        Node document = PARSER.parse("# Woodpecker 测试报告\n"
                + "## 整体测试信息概览\n"
                + generalInfoBuilder.toString()
                + (benchmarkInfoBuilder.length() == 0 ? "" : ("## 第三方工具测试报告\n" + benchmarkInfoBuilder.toString())));
        String html = RENDERER.render(document);

        // 获取当前路径
        String curPath = "";
        try {
            curPath = new File("").getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }

        html = "<meta charset=\"UTF-8\">\n" +
                "\n" +
                "<style>" +
                "@font-face {\n" +
                "  font-family: 'FZHei-B01S';\n" +
                "  src: url('file://" + curPath + "/fonts/fzht.ttf');\n" +
                "  font-weight: normal;\n" +
                "  font-style: normal;\n" +
                "}\n" +
                "\n" +
                "body {\n" +
                "    font-family: 'FZHei-B01S';\n" +
                "    overflow: hidden;\n" +
                "    word-wrap: break-word;\n" +
                "}" +
                "pre, code {\n" +
                "white-space: pre-wrap;\n" +
                "word-wrap: break-word;\n" +
                "}" +
                "</style></head><body>" + html + "\n" +
                "</body></html>";
        Calendar rightNow    =    Calendar.getInstance();
        Integer year = rightNow.get(Calendar.YEAR);
        Integer month = rightNow.get(Calendar.MONTH)+1; //第一个月从0开始，所以得到月份＋1
        Integer day = rightNow.get(rightNow.DAY_OF_MONTH);
        Integer hour24 = rightNow.get(rightNow.HOUR_OF_DAY);
        Integer minute = rightNow.get(rightNow.MINUTE);
        Integer second = rightNow.get(rightNow.SECOND);

        String fileName = reportPath + (reportPath.endsWith("/") ? "" : "/")
                +case_group_name+"("+year+"-"+month+"-"+day+"-"+hour24+"-"+minute+"-"+second+")"+"_report.pdf";

        PdfConverterExtension.exportToPdf(fileName, html, "", OPTIONS);
    }

}
