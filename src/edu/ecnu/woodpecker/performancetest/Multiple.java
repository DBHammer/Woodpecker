package edu.ecnu.woodpecker.performancetest;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import edu.ecnu.woodpecker.constant.FileConstant;

public class Multiple extends TransactionBlock implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private int min;
	private int max;
	private List<SQL> sqlList = null;

	public Multiple(int min, int max, List<SQL> sqlList) {
		super();
		this.min = min;
		this.max = max;
		this.sqlList = sqlList;
	}

	public Multiple(Multiple multiple) {
		super();
		this.min = multiple.min;
		this.max = multiple.max;
		this.sqlList = new ArrayList<SQL>();
		try {
			for (SQL sql : multiple.sqlList) {
				Class<? extends SQL> sqlClass = sql.getClass();
				Constructor<? extends SQL> constructor = sqlClass.getConstructor(sqlClass);
				this.sqlList.add(constructor.newInstance(sql));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void preproccess(Connection conn, long threadID, long threadNum) {
		for (SQL sql : sqlList) {
			sql.preproccess(conn, threadID, threadNum);
		}
	}

	@Override
	public void execute(int index, TXNResult txnResult) throws SQLException {
		int times = (int)(Math.random() * (max - min + 1) + min);
		long startTime = System.nanoTime();
		for (int i = 0; i < times; i++) {
			for (int j = 0; j < sqlList.size(); j++)
				sqlList.get(j).execute();
		}
		// 计算Multiple每次执行的平均延迟
		long txnbLatencyTemp = (System.nanoTime() - startTime) / times;
		// 如果Multiple是第一次执行，则直接在index位置填入延迟即可，否则刷新index位置的平均延迟
		if (txnResult.getSuccessedTimes() == 0) {
			txnResult.getTxnbLatency().set(index, txnbLatencyTemp * 1.0
					/ FileConstant.MICROSECOND);
		} else {
			// Multiple的平均延迟是实时计算的，不同于其他txnb是在负载结束后统一计算
			double multTime = (txnResult.getTxnbLatency().get(index) * txnResult.getSuccessedTimes()
					+ txnbLatencyTemp * 1.0 / FileConstant.MICROSECOND)
					/ (txnResult.getSuccessedTimes() + 1);
			txnResult.getTxnbLatency().set(index, multTime);
		}
	}

	@Override
	public void close() {
		for (SQL sql : sqlList) {
			sql.close();
		}
	}

	@Override
	public String toString() {
		return "Multiple [min=" + min + ", max=" + max + ", sqlList=" + sqlList + "]";
	}

	@Override
	public long execute() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}
}
