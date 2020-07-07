package edu.ecnu.woodpecker.performancetest;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import edu.ecnu.woodpecker.constant.FileConstant;
import edu.ecnu.woodpecker.constant.LogLevelConstant;
import edu.ecnu.woodpecker.log.WpLog;

public class Workload implements Serializable{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private List<Transaction> transactionList = null;
	// ratioList为累计概率值，也即该集合的最后一个值为1.
	private List<Float> ratioList = null;

	private int threadNumber;
	private int threadRunTimes;
	private int loadMachineNumber;

	public Workload(List<Transaction> transactionList, int threadNumber, int threadRunTimes,
					int loadMachineNumber) {
		super();
		this.transactionList = transactionList;
		this.threadNumber = threadNumber;
		this.threadRunTimes = threadRunTimes;
		this.loadMachineNumber = loadMachineNumber;
		initRatioList();
	}

	public Workload(Workload workload) {
		super();
		this.transactionList = new ArrayList<Transaction>();
		for (Transaction txn : workload.transactionList)
			this.transactionList.add(new Transaction(txn));

		this.ratioList = new ArrayList<Float>();
		this.ratioList.addAll(workload.ratioList);
		this.threadNumber = workload.threadNumber;
		this.threadRunTimes = workload.threadRunTimes;
		this.loadMachineNumber = workload.loadMachineNumber;
	}


	private void initRatioList() {
		ratioList = new ArrayList<Float>();
		for (int i = 0; i < transactionList.size(); i++) {
			if (i == 0)
				ratioList.add(transactionList.get(i).getRatio());
			else
				ratioList.add(ratioList.get(i - 1) + transactionList.get(i).getRatio());
		}
	}

	public void preproccess(Connection conn, long threadID, long threadNum) {
		for (Transaction txn : transactionList)
			for (TransactionBlock txnb : txn.getTransactionBlockList())
				txnb.preproccess(conn, threadID, threadNum);
	}

	public void execute(Connection conn, ThreadRunResult threadRunResult) throws SQLException {
		double ratio = Math.random();
		for (int i = 0; i < ratioList.size(); i++) {
			if (ratio < ratioList.get(i)) {
				// 统计每一个具体事务的延迟和成功、失败次数
				long startTime = System.nanoTime();
				try {
					transactionList.get(i).execute(threadRunResult.getTxnResults().get(i));
					conn.commit();
					// 累计该事务本次执行的延迟
					threadRunResult.getTxnResults().get(i).getLatency().add((double)
							(System.nanoTime() - startTime) * 1.0 / FileConstant.MICROSECOND);
					// 更新该事务执行成功的次数
					threadRunResult.getTxnResults().get(i).setSuccessedTimes(
							threadRunResult.getTxnResults().get(i).getSuccessedTimes() + 1);
				} catch (SQLException e) {
					try {
						conn.rollback();
						// 统计回滚次数，计算回滚率
						WpLog.recordLog(LogLevelConstant.DEBUG, "Txn %d rollback! part adding!", i + 1);
						threadRunResult.getTxnResults().get(i).setFailedTimes(
								threadRunResult.getTxnResults().get(i).getFailedTimes() + 1);
						throw e;
					} catch (SQLException e1) {
						throw e1;
					}
				}
				break;
			}
		}
	}

	public void close() {
		for (Transaction txn : transactionList)
			for (TransactionBlock txnb : txn.getTransactionBlockList())
				txnb.close();
	}

	@Override
	public String toString() {
		return "Workload [transactionList=" + transactionList + ", ratioList=" + ratioList
				+ ", threadNumber=" + threadNumber + ", threadRunTimes=" + threadRunTimes
				+ ", loadMachineNumber=" + loadMachineNumber + "]";
	}

	public List<Transaction> getTransactionList() {
		return transactionList;
	}

	public int getThreadNumber() {
		return threadNumber;
	}

	public int getThreadRunTimes() {
		return threadRunTimes;
	}

}
