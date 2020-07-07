package edu.ecnu.woodpecker.executor.keyword;

import java.util.Iterator;

import edu.ecnu.woodpecker.constant.LogLevelConstant;
import edu.ecnu.woodpecker.executor.Executor;
import edu.ecnu.woodpecker.executor.GrammarType;
import edu.ecnu.woodpecker.log.WpLog;
import edu.ecnu.woodpecker.stresstest.Dispatcher;

public class Latch extends Executor implements Keyword
{
    public Latch(){}

    @Override
    public void handle(String keyword, GrammarType type /* unused */ ) throws Exception
    {
        WpLog.recordLog(LogLevelConstant.INFO, "LATCH keyword");
        // wait asynchronous keyword done
        Iterator<Integer> it = performTestList.iterator();
        while (it.hasNext())
        {
            Dispatcher.awaitTaskDone(it.next());
            it.remove();
        }
        // TODO system test
        
    }

}
