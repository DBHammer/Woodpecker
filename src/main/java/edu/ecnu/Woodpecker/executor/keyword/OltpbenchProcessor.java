package edu.ecnu.Woodpecker.executor.keyword;

import edu.ecnu.Woodpecker.constant.LogLevelConstant;
import edu.ecnu.Woodpecker.constant.SignConstant;
import edu.ecnu.Woodpecker.executor.Executor;
import edu.ecnu.Woodpecker.executor.GrammarType;
import edu.ecnu.Woodpecker.log.WpLog;
import edu.ecnu.Woodpecker.tools.OltpbenchTools;
import edu.ecnu.Woodpecker.util.Util;

public class OltpbenchProcessor extends Executor implements Keyword
{

    public OltpbenchProcessor(){

    }

    @Override
    public void handle(String keyword, GrammarType type) throws Exception{
        WpLog.recordLog(LogLevelConstant.INFO, "OLTPBENCH: %s", keyword);
        switch (type)
        {
            case FIRST_GRAMMAR:
                handleFirstGrammar(keyword);
                break;
            case SECOND_GRAMMAR:
                // Index 0 is variable name, 1 is keyword
                String[] keywordParts = Util.removeBlankElement(keyword.split(SignConstant.ASSIGNMENT_STR, 2));
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
     * @param keyword Starts with "oltpbench["
     */
    private void handleFirstGrammar(String keyword) throws Exception {
        String[] parts = Util.removeBlankElement(keyword.split("\\[|;|]"));
        String bench = parts[1].trim();
        String isolation = parts[2].trim();
        String scalefactor = parts[3].trim();
        String loaderThreads = parts[4].trim();
        String terminal = parts[5].trim();
        String time = parts[6].trim();
        String rate = parts[7].trim();

        Boolean create = Boolean.parseBoolean(parts[8]);
        Integer round = Integer.parseInt(parts[13]);

     //   OltpbenchTools oltpbenchTools = new OltpbenchTools("./tools/BenchmarkTools/oltpbench/" + configFile);
        OltpbenchTools oltpbenchTools = new OltpbenchTools(bench, isolation,scalefactor,loaderThreads, terminal,time,rate);

        oltpbenchTools.create_database(bench,create);
        for(int i=0; i<round; i++) {
            WpLog.recordLog(LogLevelConstant.INFO, "自动执行第"+(i+1)+"遍");
            oltpbenchTools.SeizeOltpBench(keyword);
        }

    }

    /**
     *
     * @param variableName
     * @param keyword Starts with "oltpbench["
     * @throws Exception
     */
    private void handleSecondGrammar(String variableName, String keyword) throws Exception
    {

    }

    /**
     *
     * @param dataType The data type of variable
     * @param variableName
     * @param keyword Starts with "oltpbench["
     * @throws Exception
     */
    private void handleThirdGrammar(String dataType, String variableName, String keyword) throws Exception
    {

    }

}
