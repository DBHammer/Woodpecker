
package edu.ecnu.Woodpecker.executor.keyword;

import edu.ecnu.Woodpecker.constant.LogLevelConstant;
import edu.ecnu.Woodpecker.constant.SignConstant;
import edu.ecnu.Woodpecker.executor.Executor;
import edu.ecnu.Woodpecker.executor.GrammarType;
import edu.ecnu.Woodpecker.log.WpLog;
import edu.ecnu.Woodpecker.tools.FaultInjection;
import edu.ecnu.Woodpecker.util.Util;

public class DiskExceptionProcessor extends Executor implements Keyword
{
    public DiskExceptionProcessor()
    {
        
    }

    @Override
    public void handle(String keyword, GrammarType type) throws Exception
    {
        // TODO Auto-generated method stub
        WpLog.recordLog(LogLevelConstant.INFO, "DISK: %s", keyword);
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
     * @param keyword Starts with "disk["
     */
    private void handleFirstGrammar(String keyword)
    {
        String[] parts = Util.removeBlankElement(keyword.split("\\[|;|]"));
        FaultInjection faultInjection=new FaultInjection();
        faultInjection.seizeDISK(keyword);
    }
    
    /**
     * 
     * @param variableName
     * @param keyword Starts with "disk["
     * @throws Exception
     */
    private void handleSecondGrammar(String variableName, String keyword) throws Exception
    {
        
    }
    
    /**
     * 
     * @param dataType The data type of variable
     * @param variableName
     * @param keyword Starts with "disk["
     * @throws Exception
     */
    private void handleThirdGrammar(String dataType, String variableName, String keyword) throws Exception
    {
        
    }
}
