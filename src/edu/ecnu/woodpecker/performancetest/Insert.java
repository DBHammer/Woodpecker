package edu.ecnu.woodpecker.performancetest;

import java.sql.SQLException;
import java.sql.Timestamp;

public class Insert extends SQL {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Insert(Table table, boolean isPrepared, Distribution distribution) {
		this.table = table;
		this.isPrepared = isPrepared;
		this.distribution = distribution;
	}
	
	public Insert(Insert insert) {
		this.table = insert.table;
		this.isPrepared = insert.isPrepared;
		this.distribution = insert.distribution;
	}
	
	@Override
	protected String geneStmtSQL() {
		StringBuilder sb = new StringBuilder("INSERT INTO ");
		sb.append(table.getTableName()).append("(");
		Column column;
		
		for (int i = 0; i < table.getColumnList().size(); ++i)
		{
			column = table.getColumnList().get(i);
			if (column.getColumnName() == table.getPrimaryKey().getPkColumnNames().get(0)
					&& table.getPrimaryKey().isAutoIncrement())
				continue;
			sb.append(column.getColumnName());
			if (i != table.getColumnList().size() - 1)
				sb.append(", ");
		}
		sb.append(")");
		sb.append(" VALUES (");
		for (int i = 0; i < table.getColumnList().size(); ++i)
		{
			column = table.getColumnList().get(i);
			if (column.getColumnName() == table.getPrimaryKey().getPkColumnNames().get(0)
					&& table.getPrimaryKey().isAutoIncrement())
				continue;
			
			if (column.getColumnName() == table.getPrimaryKey().getPkColumnNames().get(0)
					&& !table.getPrimaryKey().isAutoIncrement()) {
				sb.append("'");
				sb.append(distribution.geneRandomValue());
				sb.append("'");
			}
			
			if (!column.getColumnName().equals(table.getPrimaryKey().getPkColumnNames().get(0))) {
				sb.append("'");
				sb.append(column.geneData());
				sb.append("'");
			}
			
			if (i != table.getColumnList().size() - 1)
				sb.append(", ");
		}
			
		sb.append(")");
		return sb.toString();
	}

	@Override
	protected String genePstmtSQL() {
		StringBuilder sb = new StringBuilder("INSERT INTO ");
		sb.append(table.getTableName()).append("(");
		Column column;
		
		for (int i = 0; i < table.getColumnList().size(); ++i)
		{
			column = table.getColumnList().get(i);
			if (column.getColumnName() == table.getPrimaryKey().getPkColumnNames().get(0)
					&& table.getPrimaryKey().isAutoIncrement())
				continue;
			sb.append(column.getColumnName());
			if (i != table.getColumnList().size() - 1)
				sb.append(", ");
		}
		
		sb.append(")");
		sb.append(" VALUES (");
		for (int i = 0; i < table.getColumnList().size(); ++i)
		{
			column = table.getColumnList().get(i);
			if (column.getColumnName() == table.getPrimaryKey().getPkColumnNames().get(0)
					&& table.getPrimaryKey().isAutoIncrement())
				continue;
			sb.append("?");
			if (i != table.getColumnList().size() - 1)
				sb.append(", ");
		}
			
		sb.append(")");
		return sb.toString();
	}

	@Override
	protected void fillPstmt() {
		Column column;
		int pkIndex = 0;
		
		//??????????????????table????????????
		for (int i = 0; i < table.getColumnList().size(); ++i) {
			column = table.getColumnList().get(i);
			if (column.getColumnName() == 
					table.getPrimaryKey().getPkColumnNames().get(0)) {
				pkIndex = i;
				break;
			}
		}
		
		//?????????????????????????????????????????????????????????????????????????????????
		for (int i = 0, j = 0; i < table.getColumnList().size(); ++i) {
			column = table.getColumnList().get(i);
			//????????????j = i + 1
			if (!table.getPrimaryKey().isAutoIncrement())
				j = i + 1;
			else {//????????????
				if (pkIndex == i)//????????????????????????
					continue;
				else if (pkIndex > i)//?????????????????????i + 1
					j = i + 1;
				else
					j = i;//????????????????????????????????????i
			}
			setPstmt(j, column);
		}
	}
	
	private void setPstmt(int index, Column column) {
		//??????if??????????????????????????????????????????????????????????????????????????????
		if (column.getColumnName().equals(table.getPrimaryKey().getPkColumnNames().get(0))) {
			try {
				// TODO ?????????????????????
				pstmt.setLong(index, distribution.geneRandomValue());
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return;//????????????????????????
		}
		
		String dataType = column.getDataType();
		if (column.getNullRatio() == 1)
			try {
				pstmt.setNull(index, (int)column.geneData()); //????????????java.sql.Types????????????????????????INT???
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		else //???????????????
			try {
				switch (dataType) {
				case "INT":
				case "INTEGER":
					pstmt.setInt(index, (int)column.geneData());
					break;
				case "LONG":
					pstmt.setLong(index, (long)column.geneData());
					break;
				case "FLOAT":
					pstmt.setFloat(index, (float)column.geneData());
					break;
				case "DOUBLE":
					pstmt.setDouble(index, (double)column.geneData());
					break;
				case "DECIMAL":
					pstmt.setDouble(index, (double)column.geneData());
					break;
				case "VARCHAR":
				case "CHAR":
					pstmt.setString(index, column.geneData().toString());
					break;
				case "BOOL":
					pstmt.setBoolean(index, (boolean)column.geneData());
					break;
				case "TIMESTAMP":
					pstmt.setTimestamp(index, (Timestamp)column.geneData());
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
			e.printStackTrace();
			throw e;
		}
	}
}
