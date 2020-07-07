package edu.ecnu.woodpecker.performancetest;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class Select extends SQL {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<String> selectedColumnList;
	private Filter filter;
	private String append;
	
	public Select(Table table, boolean isPrepared, Distribution distribution,
			List<String> selectedColumnList, Filter filter, String append) {
		this.table = table;
		this.isPrepared = isPrepared;
		this.distribution = distribution;
		this.selectedColumnList = selectedColumnList;
		this.filter = filter;
		this.append = append;
	}
	
	public Select(Select select) {
		this.table = select.table;
		this.isPrepared = select.isPrepared;
		this.distribution = select.distribution;
		this.selectedColumnList = select.selectedColumnList;
		this.filter = select.filter;
		this.append = select.append;
	}

	@Override
	protected String geneStmtSQL() {
		StringBuilder sb = new StringBuilder("SELECT ");
		for (int i = 0; i < selectedColumnList.size(); ++i) {
			sb.append(selectedColumnList.get(i));
			if (i != selectedColumnList.size() - 1)
				sb.append(", ");
		}
		sb.append(" FROM ").append(table.getTableName());
		sb.append(filter.geneWhereClause(table, distribution));
		sb.append(append != null ? " " + append : "");
		return sb.toString();
	}

	@Override
	protected String genePstmtSQL() {
		StringBuilder sb = new StringBuilder("SELECT ");
		for (int i = 0; i < selectedColumnList.size(); ++i) {
			sb.append(selectedColumnList.get(i));
			if (i != selectedColumnList.size() - 1)
				sb.append(", ");
		}
		sb.append(" FROM ").append(table.getTableName());
		sb.append(filter.genePwhereClause());
		sb.append(append != null ? " " + append : "");
		return sb.toString();
	}

	@Override
	protected void fillPstmt() {
		for (int i = 0; i < filter.getfilterColumnList().size(); ++i)
			setPstmt(i + 1, filter.getfilterColumnList().get(i));
	}
	
	private void setPstmt(int index, Column column) {
		String dataType = column.getDataType();
		if (dataType.indexOf("(") != -1)
			dataType = dataType.substring(0, dataType.indexOf("("));
		
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
				pstmt.executeQuery();
			} else {
				String temp = geneStmtSQL();
				System.out.println(temp);
				stmt.executeQuery(temp);
			}
			return System.nanoTime() - startTime;
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		}
	}
}