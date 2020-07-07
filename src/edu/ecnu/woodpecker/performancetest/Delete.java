package edu.ecnu.woodpecker.performancetest;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;

public class Delete extends SQL {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Filter filter;
	
	public Delete(Table table, boolean isPrepared, Distribution distribution, Filter filter) {
		this.table = table;
		this.isPrepared = isPrepared;
		this.distribution = distribution;
		this.filter = filter;
	}
	
	public Delete(Delete delete) {
		this.table = delete.table;
		this.isPrepared = delete.isPrepared;
		this.distribution = delete.distribution;
		this.filter = delete.filter;
	}

	@Override
	protected String geneStmtSQL() {
		StringBuilder sb = new StringBuilder("DELETE FROM ");
		sb.append(table.getTableName());
		sb.append(filter.geneWhereClause(table, distribution));
		return sb.toString();
	}

	@Override
	protected String genePstmtSQL() {
		StringBuilder sb = new StringBuilder("DELETE FROM ");
		sb.append(table.getTableName());
		sb.append(filter.genePwhereClause());
		return sb.toString();
	}

	@Override
	protected void fillPstmt() {
		for (int i = 0; i < filter.getfilterColumnList().size(); ++i)
			setPstmt(i + 1, filter.getfilterColumnList().get(i));		
	}
	
	private void setPstmt(int index, Column column) {
		String dataType = column.getDataType();
		
		long random = distribution.geneRandomValue();
		try {
			switch (dataType) {
			case "INT":
			case "INTEGER":
				if (column.getColumnName().equals(table.getPrimaryKey().getPkColumnNames().get(0)))
					pstmt.setInt(index, (int)random);
				else 
					pstmt.setInt(index, (int)column.geneData(random));
				break;
			case "LONG":
				pstmt.setLong(index, (long)column.geneData(random));
				break;
			case "FLOAT":
				pstmt.setFloat(index, (float)column.geneData(random));
				break;
			case "DOUBLE":
				pstmt.setDouble(index, (double)column.geneData(random));
				break;
			case "DECIMAL":
				pstmt.setBigDecimal(index, new BigDecimal(column.geneData(random).toString()));
				break;
			case "VARCHAR":
			case "CHAR":
				pstmt.setString(index, column.geneData(random).toString());
				break;
			case "BOOL":
				pstmt.setBoolean(index, (boolean)column.geneData(random));
				break;
			case "TIMESTAMP":
				pstmt.setTimestamp(index, (Timestamp)column.geneData(random));
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public long execute() throws SQLException {
		try {
			long startTime = System.nanoTime();
			if (isPrepared) {
				fillPstmt();
				pstmt.executeUpdate();
			} else {
				stmt.executeUpdate(geneStmtSQL());
			}

			return System.nanoTime() - startTime;
		} catch (SQLException e) {
			throw e;
		}
	}
}
