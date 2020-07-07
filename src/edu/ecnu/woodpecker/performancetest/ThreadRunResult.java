package edu.ecnu.woodpecker.performancetest;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 一台负载机一个ThreadRunResults类，表示该负载机所有线程的执行结果集合
 * 包括该负载机上所有线程是否执行完成，负载机IP，每个线程执行结果集。
 */
class ThreadRunResults implements Serializable {
	private static final long serialVersionUID = 1L;
	// 为true时表示所有线程执行完成，初始化为false。
	private boolean isFinished;
	private String workloadMachineIP;
	private CopyOnWriteArrayList<ThreadRunResult> threadRunResults = null;
	public ThreadRunResults(String workloadMachineIP) {
		isFinished = false;
		this.workloadMachineIP = workloadMachineIP;
		threadRunResults = new CopyOnWriteArrayList<>();
	}
	
	public boolean isFinished() {
		return isFinished;
	}
	public void setFinished(boolean isFinished) {
		this.isFinished = isFinished;
	}
	public String getWorkloadMachineIP() {
		return workloadMachineIP;
	}
	public void setWorkloadMachineIP(String workloadMachineIP) {
		this.workloadMachineIP = workloadMachineIP;
	}
	public CopyOnWriteArrayList<ThreadRunResult> getThreadRunResults() {
		return threadRunResults;
	}
	public void setThreadRunResults(CopyOnWriteArrayList<ThreadRunResult> threadRunResults) {
		this.threadRunResults = threadRunResults;
	}
}

/**
 * 记录一个线程执行结果，包括线程ID，执行成功次数，执行失败次数，
 * 总运行时间，所有事务的名称集合，事务执行结果集的集合。
 */
public class ThreadRunResult implements Serializable {
	private static final long serialVersionUID = 1L;
	private long threadID;
	private long successedTimes = 0;
	private long runTime = 0;
	private long failedTimes = 0;
	private List<String> txnNameList;
	CopyOnWriteArrayList<TXNResult> txnResults;
	public ThreadRunResult(long threadID, List<String> txnNameList) {
		this.threadID = threadID;
		this.txnNameList = txnNameList;
		txnResults = new CopyOnWriteArrayList<>();
		for (String txnName : txnNameList)
			txnResults.add(new TXNResult(txnName));
	}
	public long getThreadID() {
		return threadID;
	}
	public void setThreadID(long threadID) {
		this.threadID = threadID;
	}
	public long getSuccessedTimes() {
		return successedTimes;
	}
	public void setSuccessedTimes(long successedTimes) {
		this.successedTimes = successedTimes;
	}
	public long getRunTime() {
		return runTime;
	}
	public void setRunTime(long runTime) {
		this.runTime = runTime;
	}
	public long getFailedTimes() {
		return failedTimes;
	}
	public void setFailedTimes(long failedTimes) {
		this.failedTimes = failedTimes;
	}
	public List<String> getTxnNameList() {
		return txnNameList;
	}
	public void setTxnNameList(List<String> txnNameList) {
		this.txnNameList = txnNameList;
	}
	public CopyOnWriteArrayList<TXNResult> getTxnResults() {
		return txnResults;
	}
	public void setTxnResults(CopyOnWriteArrayList<TXNResult> txnResults) {
		this.txnResults = txnResults;
	}
}

/**
 * 一个事务的执行结果类，记录事务名，txnb数量，BRANCH数量，
 * 执行成功、失败次数。该TXN每次的执行延迟，每条txnb的平均延迟(除BRNACH外，先累加求和最后一次求平均)，
 * BRNACH的结果放在branchesLatency。
 * 注：Multiple和Branch中均不支持嵌套操作
 */
class TXNResult implements Serializable {
	private static final long serialVersionUID = 1L;
	private String txnName;
	private long branchNumber;
	private long txnbNumber;
	private long successedTimes = 0;
	private long failedTimes = 0;
	// 统计该事务执行完成，每次的延迟，集合大小为successedTimes
	private CopyOnWriteArrayList<Double> latency;
	// 统计事务中每个SQL的平均延迟，实际上是累计每一次的延迟，最后再求平均，集合大小为txnb的个数
	// 我们将Branch和Multiple均看作单独的一个txnb，Multiple在txnbLatency中的值为平均latency，
	// Branch由于结构特殊，在txnbLatency中置为0，并新建BranchResult对象来记录Branch中的每个txnb的平均延迟
	private CopyOnWriteArrayList<Double> txnbLatency;
	// 该集合的大小为事务中Branch的个数
	private CopyOnWriteArrayList<BranchResult> branchesLatency;
	
	public TXNResult(String txnName) {
		this.txnName = txnName;
		latency = new CopyOnWriteArrayList<>();
		txnbLatency = new CopyOnWriteArrayList<>();
		branchesLatency = new CopyOnWriteArrayList<>();
	}

	public String getTxnName() {
		return txnName;
	}
	public void setTxnName(String txnName) {
		this.txnName = txnName;
	}
	public long getBranchNumber() {
		return branchNumber;
	}
	public void setBranchNumber(long branchNumber) {
		this.branchNumber = branchNumber;
	}
	public long getTxnbNumber() {
		return txnbNumber;
	}
	public void setTxnbNumber(long txnbNumber) {
		this.txnbNumber = txnbNumber;
	}
	public long getSuccessedTimes() {
		return successedTimes;
	}
	public void setSuccessedTimes(long successedTimes) {
		this.successedTimes = successedTimes;
	}
	public long getFailedTimes() {
		return failedTimes;
	}
	public void setFailedTimes(long failedTimes) {
		this.failedTimes = failedTimes;
	}
	public CopyOnWriteArrayList<Double> getLatency() {
		return latency;
	}
	public void setLatency(CopyOnWriteArrayList<Double> latency) {
		this.latency = latency;
	}
	public CopyOnWriteArrayList<Double> getTxnbLatency() {
		return txnbLatency;
	}
	public void setTxnbLatency(CopyOnWriteArrayList<Double> txnbLatency) {
		this.txnbLatency = txnbLatency;
	}
	public CopyOnWriteArrayList<BranchResult> getBranchesLatency() {
		return branchesLatency;
	}
	public void setBranchesLatency(CopyOnWriteArrayList<BranchResult> branchesLatency) {
		this.branchesLatency = branchesLatency;
	}

	@Override
	public String toString() {
		return "TXNResult [txnName=" + txnName + ", branchNumber=" + branchNumber + ", txnbNumber=" + txnbNumber
				+ ", successedTimes=" + successedTimes + ", failedTimes=" + failedTimes + ", latency=" + latency
				+ ", txnbLatency=" + txnbLatency + ", branchesLatency=" + branchesLatency + "]";
	}
}

/**
 * 记录一个BRANCH的每个分支执行了多少次，每条分支的平均延迟(先累加求和，最后一次求平均)
 */
class BranchResult implements Serializable {
	private static final long serialVersionUID = 1L;
	// 分支数量
	private long branchNumber;
	// 每个分支运行次数
	private CopyOnWriteArrayList<Integer> branchRunTimes;
	// 每个分支的平均延迟(先累加求和，最后一次求平均)
	private CopyOnWriteArrayList<Double> branchLatency;
	
	public BranchResult(long branchNumber) {
		this.branchNumber = branchNumber;
		branchRunTimes = new CopyOnWriteArrayList<>();
		branchLatency = new CopyOnWriteArrayList<>();
		for (int i = 0; i < branchNumber; ++i) {
			branchRunTimes.add(0);
			branchLatency.add(0.0);
		}
	}
	
	public long getBranchNumber() {
		return branchNumber;
	}
	public void setBranchNumber(long branchNumber) {
		this.branchNumber = branchNumber;
	}
	public CopyOnWriteArrayList<Integer> getBranchRunTimes() {
		return branchRunTimes;
	}
	public void setBranchRunTimes(CopyOnWriteArrayList<Integer> branchRunTimes) {
		this.branchRunTimes = branchRunTimes;
	}
	public CopyOnWriteArrayList<Double> getBranchLatency() {
		return branchLatency;
	}
	public void setBranchLatency(CopyOnWriteArrayList<Double> branchLatency) {
		this.branchLatency = branchLatency;
	}
}