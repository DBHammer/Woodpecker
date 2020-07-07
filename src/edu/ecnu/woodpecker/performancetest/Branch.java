package edu.ecnu.woodpecker.performancetest;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import edu.ecnu.woodpecker.constant.FileConstant;

public class Branch extends TransactionBlock implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<Float> ratioList = null;
	private List<ArrayList<SQL>> branchBlockList = null;

	public Branch(Float[] ratios, List<ArrayList<SQL>> branchBlockList) {
		super();
		this.ratioList = new ArrayList<Float>();
		this.ratioList.add(ratios[0]);
		for (int i = 1; i < ratios.length; i++)
			this.ratioList.add(this.ratioList.get(i - 1) + ratios[i]);
		this.branchBlockList = branchBlockList;
	}

	public Branch(Branch branch) {
		super();
		this.ratioList = new ArrayList<Float>();
		this.ratioList.addAll(branch.ratioList);
		this.branchBlockList = new ArrayList<ArrayList<SQL>>();
		try {
			for (int i = 0; i < branch.branchBlockList.size(); i++) {
				this.branchBlockList.add(new ArrayList<SQL>());
				for (int j = 0; j < branch.branchBlockList.get(i).size(); j++) {
					SQL sql = branch.branchBlockList.get(i).get(j);
					Class<? extends SQL> sqlClass = sql.getClass();
					Constructor<? extends SQL> constructor = sqlClass.getConstructor(sqlClass);
					this.branchBlockList.get(i).add(constructor.newInstance(sql));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void preproccess(Connection conn, long threadID, long threadNum) {
		for (ArrayList<SQL> sqlList : branchBlockList)
			for (SQL sql : sqlList)
				sql.preproccess(conn, threadID, threadNum);
	}
	
	/**
	 * 此处的index为该BRANCH是它所属TXN中的第几个BRANCH，从0开始，0，1，2...
	 */
	@Override
	public void execute(int index, TXNResult txnResult) throws SQLException {
		double ratio = Math.random();
		long startTime;
		int branchIndex = 0;//记录当前分支中的第几个分支被执行了
		double branchLatencyTemp = 0;
		for (int i = 0; i < ratioList.size(); i++) {
			if (ratio < ratioList.get(i)) {
				startTime = System.nanoTime();
				for (int j = 0; j < branchBlockList.get(i).size(); j++)
					branchBlockList.get(i).get(j).execute();
				branchLatencyTemp = System.nanoTime() - startTime;
				branchIndex = i;
				break;
			}
		}
		
		if (txnResult.getBranchesLatency().get(index) == null) {
			BranchResult temp = new BranchResult(ratioList.size());
			temp.getBranchLatency().set(branchIndex, branchLatencyTemp * 1.0 / FileConstant.MICROSECOND);
			temp.getBranchRunTimes().set(branchIndex, 1);
			txnResult.getBranchesLatency().set(index, temp);
		} else {
			txnResult.getBranchesLatency().get(index).getBranchLatency().set(branchIndex,
					txnResult.getBranchesLatency().get(index).getBranchLatency().get(branchIndex)
					+ branchLatencyTemp * 1.0 / FileConstant.MICROSECOND);
			txnResult.getBranchesLatency().get(index).getBranchRunTimes().set(branchIndex, 
					(txnResult.getBranchesLatency().get(index).getBranchRunTimes().get(branchIndex) + 1));
		}
	}
	
	@Override
	public void close() {
		for (ArrayList<SQL> sqlList : branchBlockList)
			for (SQL sql : sqlList)
				sql.close();
	}
	
	@Override
	public String toString() {
		return "Branch [ratioList=" + ratioList + ", branchBlockList=" + branchBlockList + "]";
	}

	@Override
	public long execute() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}
}
