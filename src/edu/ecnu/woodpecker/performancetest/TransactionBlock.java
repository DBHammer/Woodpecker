package edu.ecnu.woodpecker.performancetest;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class TransactionBlock implements Serializable {
	private static final long serialVersionUID = 1L;
	//该形式的execute由Branch和Multiple使用
	public abstract void execute(int index, TXNResult txnResult) throws SQLException;
	//该形式的execute为执行SQL中的sql语句
	public abstract long execute() throws SQLException;
	protected abstract void preproccess(Connection conn, long threadID, long threadNum);
	public abstract void close();
}
