package edu.ecnu.woodpecker.performancetest;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import edu.ecnu.woodpecker.constant.FileConstant;

public class Transaction implements Serializable {
	private static final long serialVersionUID = 1L;
	private String txnName;
	private float ratio;
	private List<TransactionBlock> transactionBlockList = null;

	public Transaction(String txnName, float ratio, List<TransactionBlock> transactionBlockList) {
		super();
		this.txnName = txnName;
		this.ratio = ratio;
		this.transactionBlockList = transactionBlockList;
	}

	public Transaction(Transaction txn) {
		super();
		this.txnName = txn.txnName;
		this.ratio = txn.ratio;
		this.transactionBlockList = new ArrayList<TransactionBlock>();
		try {
			for (TransactionBlock txnb : txn.transactionBlockList) {
				Class<? extends TransactionBlock>  txnbClass = txnb.getClass();
				Constructor<? extends TransactionBlock> constructor = txnbClass.getConstructor(txnbClass);
				this.transactionBlockList.add(constructor.newInstance(txnb));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void execute(TXNResult txnResult) throws SQLException {
		// 初始化txnb数量
		if (txnResult.getTxnbNumber() == 0)
			txnResult.setTxnbNumber(transactionBlockList.size());
		// 初始化branch数量并初始化BranchesLatency集合的数量
		if (txnResult.getBranchNumber() == 0) {
			for (int i = 0; i < transactionBlockList.size(); i++) 
				if (transactionBlockList.get(i).getClass().getName().indexOf("Branch")!= -1) {
					txnResult.setBranchNumber(txnResult.getBranchNumber() + 1);
					txnResult.getBranchesLatency().add(null);
				}
		}
		long txnbLatencyTemp = 0;
		int branchCount = 0; // 记录Branch的当前数量，为branchesLatency中设置值做准备
		for (int i = 0; i < transactionBlockList.size(); i++) {
			try {
				if (transactionBlockList.get(i) instanceof SQL) {
					txnbLatencyTemp = transactionBlockList.get(i).execute();
					// 若第一次执行该事务，初始化txnbLatency集合，将该事务中的txnbLatency集合添加0
					if (txnResult.getSuccessedTimes() == 0) {
						for (int j = 0; j < txnResult.getTxnbNumber(); ++j)
							txnResult.getTxnbLatency().add(0.0);
						txnResult.getTxnbLatency().set(i, txnbLatencyTemp * 1.0 
								/ FileConstant.MICROSECOND);
					} else // 不是第一次执行该事务，更新该txnb的平均latency，这里只是累加
						txnResult.getTxnbLatency().set(i, txnResult.getTxnbLatency().get(i) + txnbLatencyTemp
								* 1.0 / FileConstant.MICROSECOND);
				} else { // 执行BRANCH和MULTIPLE
					// 如果Branch和Multiple是第一个txnb，则初始化txnbLatency集合
					if (txnResult.getSuccessedTimes() == 0 && i == 0) {
						for (int j = 0; j < txnResult.getTxnbNumber(); ++j)
							txnResult.getTxnbLatency().add(0.0);
					}
					
					if (transactionBlockList.get(i).getClass().getName().indexOf("Branch") != -1) {
						System.out.println("Ready to run branch");
						txnResult.getTxnbLatency().set(i, 0.0); // 将txnbLatency中Branch下标对应位置的值设为0
						transactionBlockList.get(i).execute(branchCount, txnResult);
						branchCount++;
					} else { // 执行Multiple
						transactionBlockList.get(i).execute(i, txnResult);
					}
				}
			} catch (SQLException e) {
				throw e;
			}
		}
	}

	public String getTxnName() {
		return this.txnName;
	}

	public float getRatio() {
		return ratio;
	}

	public List<TransactionBlock> getTransactionBlockList() {
		return transactionBlockList;
	}

	@Override
	public String toString() {
		return "Transaction [ratio=" + ratio + ", transactionBlockList=" + transactionBlockList + "]";
	}
}
