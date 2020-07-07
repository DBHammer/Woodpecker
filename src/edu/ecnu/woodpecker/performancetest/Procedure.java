package edu.ecnu.woodpecker.performancetest;

import java.sql.Connection;
import java.sql.SQLException;

public class Procedure extends TransactionBlock {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	@Override
	protected void preproccess(Connection conn, long threadID, long threadNum) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void execute(int index, TXNResult txnResult) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public long execute() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
