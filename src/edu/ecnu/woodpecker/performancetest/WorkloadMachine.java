package edu.ecnu.woodpecker.performancetest;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

import edu.ecnu.woodpecker.constant.FileConstant;
import edu.ecnu.woodpecker.constant.LogLevelConstant;
import edu.ecnu.woodpecker.constant.SignConstant;
import edu.ecnu.woodpecker.log.WpLog;

public class WorkloadMachine implements Runnable, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static BlockingQueue<Workload> workloadQueue = null;
	private static Workload workload = null;
	public static int currentWorkloadID = 0;
	public static ThreadRunResults threadRunResults = null;
	//用于计算中间过程的TPS时使用(算的结果并不精确，是个近似值)，在每次接收到workload时进行赋值
	public static long acceptWorkloadTime; 
	private List<Long> QTPS = null;
	private List<Double> QTRS = null;
	private CountDownLatch countDownLatchForThread = null;
	private CountDownLatch countDownLatchForConnection = null;
	public static List<String> dispatcherIPList = Arrays.asList(
			WorkloadConfigInfo.dispatcher.split(SignConstant.COLON_STR)[0]);
	public static List<Integer> dispatcherPortList = Arrays.asList(Integer.parseInt(
			WorkloadConfigInfo.dispatcher.split(SignConstant.COLON_STR)[1]));
	// 开启负载机端的Client
	public static NetworkClient client = new NetworkClient(dispatcherIPList, dispatcherPortList, 
			new WorkloadMachineClientHandler());
	
	public WorkloadMachine() {}
	
	public WorkloadMachine(Workload workload) {
		WorkloadMachine.workload = new Workload(workload);
	}

	public void execute() {
		int workloadMachineIndex = 0;
		try {
			String workloadMachineIP = InetAddress.getLocalHost().getHostAddress();
			for (int j = 0; j < WorkloadConfigInfo.dbMachines.size(); ++j) {
				String dbEntry = WorkloadConfigInfo.dbMachines.get(j);
				if (dbEntry.split(SignConstant.COLON_STR)[0].equals(workloadMachineIP)) {
					workloadMachineIndex = j;
					break;
				}
			}
		} catch (UnknownHostException e) {
			WpLog.recordLog(LogLevelConstant.DEBUG, "Getting workload machine IP went wrong!");
			e.printStackTrace();
		}
		
		List<String> txnNameList = new ArrayList<>();
		for (Transaction txn : workload.getTransactionList())
			txnNameList.add(txn.getTxnName());
		
		try {
			threadRunResults = new ThreadRunResults(InetAddress.getLocalHost().getHostAddress());
		} catch (UnknownHostException e2) {
			e2.printStackTrace();
		}
		
		QTPS = new CopyOnWriteArrayList<>();
		QTRS = new CopyOnWriteArrayList<>();
		countDownLatchForThread = new CountDownLatch(workload.getThreadNumber());
		countDownLatchForConnection = new CountDownLatch(workload.getThreadNumber());
		
		for (int i = 1; i <= workload.getThreadNumber(); ++i) {
			ThreadRunResult temp = new ThreadRunResult(i, txnNameList);
			threadRunResults.getThreadRunResults().add(temp);
			WpLog.recordLog(LogLevelConstant.DEBUG, "Thread-%d was created!", 
					(i + workloadMachineIndex * workload.getThreadNumber() * workload.getThreadRunTimes()));
			
			// 线程ID为：i + 平均每台负载机执行的次数 * workloadMachineIndex
			new Thread(new WorkloadExecutor(countDownLatchForThread, countDownLatchForConnection, QTPS, QTRS, 
					threadRunResults.getThreadRunResults().get(i - 1), workload, 
					(i + workloadMachineIndex * workload.getThreadNumber() * workload.getThreadRunTimes()),
					workload.getThreadNumber())).start();
		}
		
		try {
			countDownLatchForThread.await();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		//该段代码负责汇总每个txnb的平均延迟和每个TXN中BRANCH的每个分支的平均延迟
		for (int i = 0; i < threadRunResults.getThreadRunResults().size(); ++i) {
			ThreadRunResult threadRunResult = threadRunResults.getThreadRunResults().get(i);
			for (int j = 0; j < threadRunResult.getTxnResults().size(); ++j) {
				TXNResult txnResult = threadRunResult.getTxnResults().get(j);
				//汇总每个txnb的平均延迟
				for (int k = 0; k < txnResult.getTxnbNumber(); ++k) {
					txnResult.getTxnbLatency().set(k, txnResult.getTxnbLatency().get(k) 
							/ txnResult.getSuccessedTimes());
				}
				//汇总每个TXN中每个BRANCH的每个分支的平均延迟
				for (int l = 0; l < txnResult.getBranchesLatency().size(); ++l) {
					BranchResult branchResult = txnResult.getBranchesLatency().get(l);
					for (int m = 0; m < branchResult.getBranchNumber(); ++m) {
						branchResult.getBranchLatency().set(m, 
								branchResult.getBranchLatency().get(m) 
								/ branchResult.getBranchRunTimes().get(m));
					}
				}
			}
		}
		
		System.out.println("Print results:");
		//下面这段代码是关于统计BRANCH每个分支的平均延迟
//		for (int i = 100; i < 150; ++i) {
//			System.out.println("Thread-" + i + "branch number:");
//			for (TXNResult txnResult : threadRunResults.getThreadRunResults().get(i).getTxnResults()) {
//				double sumLatency = 0;
//				for (int l = 0; l < txnResult.getLatency().size(); ++l) {
//					sumLatency += txnResult.getLatency().get(l);
//				}
//				System.out.println(txnResult.getLatency().size() + ":" + txnResult.getSuccessedTimes());
//				System.out.println(sumLatency / txnResult.getSuccessedTimes());
//				System.out.println("--------------------");
//				System.out.println(txnResult.getTxnbLatency());
//				System.out.println("TXN successed times:" + txnResult.getSuccessedTimes());
//				for (BranchResult branchResult : txnResult.getBranchesLatency()) {
//					System.out.println(branchResult.getBranchRunTimes());
//					System.out.println(branchResult.getBranchLatency());
//					for (int j = 0; j <  branchResult.getBranchLatency().size(); ++j) {
//						System.out.println(j + ":" + branchResult.getBranchLatency().get(j));
//					}
////					System.out.println(branchResult.getBranchLatency().get(0));
////					System.out.println(branchResult.getBranchRunTimes());
//				}
//			}
////			System.out.println("Thread-" + i + 1 + "branch results:");
////			threadRunResults.get(i).getTxnResults().get(0).getBranchesLatency().get(0).getBranchLatency().get(0);
////			threadRunResults.get(i).getTxnResults().get(0).getBranchesLatency().get(0).getBranchLatency().get(1);
//		}
		
		long tps = 0;
		List<Double> txnAvgLatency = new ArrayList<>();
		for (int i = 0; i < threadRunResults.getThreadRunResults().get(0).getTxnResults().size(); ++i)
			txnAvgLatency.add(0.0);
		
		for (int t = 0; t < threadRunResults.getThreadRunResults().size(); ++t) {
			ThreadRunResult threadRunResult = threadRunResults.getThreadRunResults().get(t);
			tps = tps + (long)(threadRunResult.getSuccessedTimes() / (threadRunResult.getRunTime() * 1.0 
					/ FileConstant.NANOSECOND));
			
			for (int i = 0; i < threadRunResult.getTxnResults().size(); ++i) {
				double sumLatency = 0;
				for (int j = 0; j < threadRunResult.getTxnResults().get(i).getLatency().size(); ++j) {
					sumLatency += threadRunResult.getTxnResults().get(i).getLatency().get(j);
				}
				double avgLatency = sumLatency / threadRunResult.getTxnResults().get(i).getLatency().size();
				txnAvgLatency.set(i, txnAvgLatency.get(i) + avgLatency);
			}
		}
		
		System.out.println(threadRunResults.getWorkloadMachineIP() + "'s TPS:" + tps);
		for (int i = 0; i < threadRunResults.getThreadRunResults().get(0).getTxnResults().size(); ++i)
			System.out.println("TXN" + (i + 1) + "'s average Latency:" + txnAvgLatency.get(i) / 
					threadRunResults.getThreadRunResults().size());
		
		int sumQTPS = 0;
		double sumQTRS = 0;
		for (int i = 0; i < QTPS.size(); i++) {
			sumQTPS += QTPS.get(i);
			sumQTRS += QTRS.get(i);
	}
	sumQTRS = sumQTRS * 1.0 / QTRS.size();
	WpLog.recordLog(LogLevelConstant.INFO, "QTPS: %d, QTRS: %f", sumQTPS, sumQTRS);
	threadRunResults.setFinished(true); //标志着该负载机上的所有线程均执行完毕
}

	public static void main(String[] args) {
		WorkloadMachine.workloadQueue = new ArrayBlockingQueue<>(1);
		new Thread(new WorkloadMachine()).start();
		WpLog.recordLog(LogLevelConstant.DEBUG, "WorkloadMachine thread has initialized and started...");

		
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (threadRunResults != null) {
					try {
						AbstractResultForClient arfc = new AbstractResultForClient(
								WorkloadMachine.currentWorkloadID, InetAddress.getLocalHost().getHostAddress(),
								threadRunResults.getThreadRunResults().get(0).getTxnResults().size());
						processAbstractResultForClient(threadRunResults, arfc);
						WorkloadMachine.client.send(arfc, 0);
						System.out.println(arfc.toString());
					} catch (Exception e) {
						e.printStackTrace();
					}
//					
					if (threadRunResults.isFinished() == true) {
						threadRunResults = null; //释放该workload的结果对象
//						timer.cancel(); // 由于timer要一直保持活跃，所以这里不能取消timer
					}
				}				
			}
		}, 0, FileConstant.DEF_SEND_TIME * 1000);
	}

	/**
	 * 此函数用来处理ThreadRunResults，将它在该时刻的关键信息保存到AbstractResultForClient对象中，该对象占用
	 * 更小的内存空间，因此，使网络传输更快。
	 * @param threadRunResults
	 * @param arfc
	 */
	public static void processAbstractResultForClient(ThreadRunResults threadRunResults,
			AbstractResultForClient arfc) {
		double progress = 0;
		for (int i = 0; i < threadRunResults.getThreadRunResults().size(); ++i) {
			ThreadRunResult threadRunResult = threadRunResults.getThreadRunResults().get(i);
			progress += threadRunResult.getSuccessedTimes() * 1.0 / WorkloadMachine.workload.getThreadRunTimes();
		}
		
		progress /= threadRunResults.getThreadRunResults().size();
		arfc.progress = progress; //将负载机执行进度赋值给arfc
		
		//该段代码负责汇总每个txnb的平均延迟和每个TXN中BRANCH的每个分支的平均延迟
		for (int i = 0; i < threadRunResults.getThreadRunResults().size(); ++i) {
			ThreadRunResult threadRunResult = threadRunResults.getThreadRunResults().get(i);
			for (int j = 0; j < threadRunResult.getTxnResults().size(); ++j) {
				TXNResult txnResult = threadRunResult.getTxnResults().get(j);
				//汇总每个txnb的平均延迟
				for (int k = 0; k < txnResult.getTxnbNumber(); ++k) {
					txnResult.getTxnbLatency().set(k, txnResult.getTxnbLatency().get(k) 
							/ txnResult.getSuccessedTimes());
				}
				//汇总每个TXN中每个BRANCH的每个分支的平均延迟
				for (int l = 0; l < txnResult.getBranchesLatency().size(); ++l) {
					BranchResult branchResult = txnResult.getBranchesLatency().get(l);
					for (int m = 0; m < branchResult.getBranchNumber(); ++m) {
						branchResult.getBranchLatency().set(m, 
								branchResult.getBranchLatency().get(m) 
								/ branchResult.getBranchRunTimes().get(m));
					}
				}
			}
		}//END
		
		//设置当前负载已经执行了的时间，以秒为单位
		arfc.runTime = (System.nanoTime() - WorkloadMachine.acceptWorkloadTime) / FileConstant.NANOSECOND;
		
		long workloadMachineTPS = 0;
		for (ThreadRunResult threadRunResult : threadRunResults.getThreadRunResults()) {
			if (threadRunResults.isFinished()) //这里计算的是精确的结果
				workloadMachineTPS = workloadMachineTPS + (long)(threadRunResult.getSuccessedTimes() 
						/ (threadRunResult.getRunTime() * 1.0 / FileConstant.NANOSECOND));
			else //这里只是返回中间结果的近似结果(为了更好的可视化)，但有误差
				workloadMachineTPS = workloadMachineTPS + (long)(threadRunResult.getSuccessedTimes() 
						/ ((System.nanoTime() - WorkloadMachine.acceptWorkloadTime) * 1.0 
						/ FileConstant.NANOSECOND));
			
			for (int i = 0; i < threadRunResult.getTxnResults().size(); ++i) {
				double txnSumLatency = 0;// 某个txn的总延迟
				for (int j = 0; j < threadRunResult.getTxnResults().get(i).getLatency().size(); ++j) {
					txnSumLatency += threadRunResult.getTxnResults().get(i).getLatency().get(j);
				}
				
				arfc.txnAvgLatency.set(i, arfc.txnAvgLatency.get(i) + (txnSumLatency / 
						threadRunResult.getTxnResults().get(i).getLatency().size()));
				
				Collections.sort(threadRunResult.getTxnResults().get(i).getLatency());
				double temp50, temp90, temp95, temp99;
				temp50 = threadRunResult.getTxnResults().get(i).getLatency().get(
						(int)(threadRunResult.getTxnResults().get(i).getLatency().size() * 0.5));
				temp90 = threadRunResult.getTxnResults().get(i).getLatency().get(
						(int)(threadRunResult.getTxnResults().get(i).getLatency().size() * 0.9));
				temp95 = threadRunResult.getTxnResults().get(i).getLatency().get(
						(int)(threadRunResult.getTxnResults().get(i).getLatency().size() * 0.95));
				temp99 = threadRunResult.getTxnResults().get(i).getLatency().get(
						(int)(threadRunResult.getTxnResults().get(i).getLatency().size() * 0.99));
				
				arfc.txn50Latency.set(i, arfc.txn50Latency.get(i) + temp50);
				arfc.txn90Latency.set(i, arfc.txn90Latency.get(i) + temp90);
				arfc.txn95Latency.set(i, arfc.txn95Latency.get(i) + temp95);
				arfc.txn99Latency.set(i, arfc.txn99Latency.get(i) + temp99);
			}
		}
		//求之前累加的若干个延迟的平均值
		for (int i = 0; i < threadRunResults.getThreadRunResults().get(0).getTxnResults().size(); ++i) {
			arfc.txnAvgLatency.set(i, arfc.txnAvgLatency.get(i) / threadRunResults.getThreadRunResults().size());
			arfc.txn50Latency.set(i, arfc.txn50Latency.get(i) / threadRunResults.getThreadRunResults().size());
			arfc.txn90Latency.set(i, arfc.txn90Latency.get(i) / threadRunResults.getThreadRunResults().size());
			arfc.txn95Latency.set(i, arfc.txn95Latency.get(i) / threadRunResults.getThreadRunResults().size());
			arfc.txn99Latency.set(i, arfc.txn99Latency.get(i) / threadRunResults.getThreadRunResults().size());
		}
		arfc.workloadMachineTPS = workloadMachineTPS; //将该时刻的TPS赋值给arfc
		arfc.time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
		if (threadRunResults.isFinished()) {//若所有线程执行完毕，设置arfc的isFinished为true
			arfc.isFinished = true;
			//设置完成该负载所用的时间
			arfc.runTime = (System.nanoTime() - WorkloadMachine.acceptWorkloadTime) / FileConstant.NANOSECOND;
		}
	}
	
	@Override
	public void run() {
		while (true) {
			try {
				WorkloadMachine.workload = workloadQueue.take();
				WpLog.recordLog(LogLevelConstant.DEBUG, "Began to run workload...");
				WorkloadMachine.acceptWorkloadTime = System.nanoTime();
				//累计目前接共收到了几个workload，反馈给dispatcher时，该id用来判断是否为最后一个workload
				//若为最后一个workload，则关闭负载机端的Server进程
				WorkloadMachine.currentWorkloadID++; 
				execute();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
 	}
}
