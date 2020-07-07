package edu.ecnu.woodpecker.performancetest;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import edu.ecnu.woodpecker.constant.FileConstant;
import edu.ecnu.woodpecker.constant.LogLevelConstant;
import edu.ecnu.woodpecker.constant.SignConstant;
import edu.ecnu.woodpecker.log.WpLog;

/**
 * 这个类负责开启一个workload的一个线程，一个线程维护一个数据库连接
 * @author 59173
 *
 */
public class WorkloadExecutor extends WorkloadConfigInfo implements Runnable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Connection conn;
	private Workload workload;
	private int threadID;
	private int threadNum;
	private CountDownLatch countDownLatchForThread = null;
	private CountDownLatch countDownLatchForConnection = null;
	private List<Long> QTPS = null;
	private List<Double> QTRS = null;
	private ThreadRunResult threadRunResult = null;
	
	public WorkloadExecutor(CountDownLatch countDownLatchForThread,
			CountDownLatch countDownLatchForConnection,
			List<Long> QTPS, List<Double> QTRS,
			ThreadRunResult threadRunResult, 
			Workload workload, int threadID, int threadNum) {
		this.countDownLatchForThread = countDownLatchForThread;
		this.countDownLatchForConnection = countDownLatchForConnection;
		this.threadRunResult = threadRunResult;
		this.QTPS = QTPS;
		this.QTRS = QTRS;
		
		//这里必须使用深复制，这里是每个线程使用不同的workload
		this.workload = new Workload(workload);
		this.threadID = threadID;
		this.threadNum = threadNum;
//		this.workload = workload;
	}
	
	//这个操作用来做一些事务执行前的准备，如初始化数据库connection和每个操作的statement
	public void preprocess(long threadId, long threadNum) {
		//每个线程建立一个数据库连接，接下来的若干次执行都复用这一个连接 
		int entryIndex = new Random().nextInt(dbEntries.size());
		String[] entries = dbEntries.get(entryIndex).split(SignConstant.COLON_STR);
		conn = new DBConnection(entries[0], Integer.valueOf(entries[1]), dbInstance, 
				dbUser, dbPassword, DBMS).getDBConnection();
		try {
			conn.setAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		workload.preproccess(conn, threadId, threadNum);

		countDownLatchForConnection.countDown();
		
		WpLog.recordLog(LogLevelConstant.DEBUG, "Thread-%d connects to %s", threadID, entries[0]);
		WpLog.recordLog(LogLevelConstant.DEBUG, threadID + " is wating...");
	}
	
	@Override
	public void run() {
		//预处理
		preprocess(threadID, threadNum);
		// 等所有DB连接都建立成功后开始跑负载
		try {
			countDownLatchForConnection.await();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		WpLog.recordLog(LogLevelConstant.DEBUG, threadID + " began to run...");

		long startTime = System.nanoTime();
		for (int i = 1; i <= workload.getThreadRunTimes(); ++i) {
			//统计该线程若干次执行所消耗的所有时间，成功，失败次数。
			try {
				workload.execute(conn, threadRunResult);
				threadRunResult.setSuccessedTimes(threadRunResult.getSuccessedTimes() + 1);
			} catch (SQLException e) {
				WpLog.recordLog(LogLevelConstant.DEBUG, "Txn rollback, global adding!");
				threadRunResult.setFailedTimes(threadRunResult.getFailedTimes() + 1);
			}
			//TODO 传给WorkloadMachine
		}
		long time = System.nanoTime() - startTime;
		threadRunResult.setRunTime(time);
		
		QTPS.add((long)(workload.getThreadRunTimes() / (time * 1.0 / FileConstant.NANOSECOND)));
		QTRS.add((time * 1.0 / FileConstant.MICROSECOND / workload.getThreadRunTimes()));
		
		try  {
			workload.close();
			if (conn != null) {
				conn.close();
				conn = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
//		WpLog.recordLog(LogLevelConstant.DEBUG, "Thread-%d finished! time:%d---QTPS:%d---QTRS:%f", 
//		threadID, time, (int)(workload.getThreadRunTimes() / (time * 1.0 / 1000000000)), 
//		(double)(time * 1.0 / 1000000 / workload.getThreadRunTimes()));
		countDownLatchForThread.countDown();
	}

}
