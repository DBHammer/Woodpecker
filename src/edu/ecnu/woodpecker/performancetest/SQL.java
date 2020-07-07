package edu.ecnu.woodpecker.performancetest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class SQL extends TransactionBlock {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected Table table = null;
	protected String stmtSQL = null;
	protected String pstmtSQL = null;
	protected boolean isPrepared;
	
	protected Statement stmt = null;
	protected PreparedStatement pstmt = null;
	
	//操作分布
	protected Distribution distribution = null;
	
	//生成非预编译的SQL语句
	protected abstract String geneStmtSQL();
	//生成预编译的SQL语句
	protected abstract String genePstmtSQL();
	//设置预编译的SQL中第index个参数的值
//	protected void setPstmt(int index, Column column) {
//		
//	}
	
	//填充预编译的SQL语句ֵ
	protected abstract void fillPstmt();
	protected void preproccess(Connection conn, long threadID, long threadNum) {
		switch (distribution.distributionType) {
			case UNIFORM:
				distribution = new UniformDistribution(distribution);
				break;
			case UNIQUE:
				distribution = new UniqueDistribution(threadID, threadNum);
				break;
			case NORMAL:
				distribution = new NormalDistribution(distribution);
				break;
			case ZIPF:
				distribution = new ZIPFDistribution(distribution);
			default :
				break;
				// ......
			}
		
		try {
			if (isPrepared) {
				pstmtSQL = genePstmtSQL();
				pstmt = conn.prepareStatement(pstmtSQL);
			} else {
				stmt = conn.createStatement();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	//空函数
	public void execute(int index, TXNResult txnResult) throws SQLException {}
	//该形式的execute为执行SQL中的sql语句
	public abstract long execute() throws SQLException;
	//关闭数据库执行器
	public void close() {
		try {
			if (stmt != null)
				stmt.close();
			if (pstmt != null)
				pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public Table getTable() {
		return table;
	}
	public void setPrepared(boolean isPrepared) {
		this.isPrepared = isPrepared;
	}
}
