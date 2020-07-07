package edu.ecnu.woodpecker.performancetest;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class Update extends SQL {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<Column> updatedColumnList;
	private List<String> operatorList;
	private List<String> updatedValue;
	private Filter filter;

	public Update(Table table, boolean isPrepared, Distribution distribution,
			List<Column> updatedColumnList, List<String> operatorList, List<String> updatedValue, Filter filter) {
		this.table = table;
		this.isPrepared = isPrepared;
		this.distribution = distribution;
		this.updatedColumnList = updatedColumnList;
		this.operatorList = operatorList;
		this.updatedValue = updatedValue;
		this.filter = filter;
	}
	
	public Update(Update update) {
		this.table = update.table;
		this.isPrepared = update.isPrepared;
		this.distribution = update.distribution;
		this.updatedColumnList = update.updatedColumnList;
		this.operatorList = update.operatorList;
		this.updatedValue = update.updatedValue;
		this.filter = update.filter;
	}
	
	@Override
	protected String geneStmtSQL() {
		StringBuilder sb = new StringBuilder("UPDATE ")
				.append(table.getTableName()).append(" SET ");
		for (int i = 0; i < updatedColumnList.size(); ++i) {
			sb.append(updatedColumnList.get(i).getColumnName()).append(" = ");
			String upValue = (!updatedValue.get(i).equals("") ? updatedValue.get(i) 
					: updatedColumnList.get(i).geneData().toString());
			
			switch (operatorList.get(i)) {
			case "=":
				if (updatedColumnList.get(i).getDataType().indexOf("CHAR", 0) != -1)
					sb.append("('" + upValue + "')");//这里有可能是用户指定的字符串，要用单引号括住
				else
					sb.append("(" + upValue + ")");
				break;
			case "++":
				sb.append(updatedColumnList.get(i).getColumnName()).append(" + 1");
				break;
			case "--":
				sb.append(updatedColumnList.get(i).getColumnName()).append(" - 1");
				break;
			case "+=":
			case "-=":
			case "*=":
			case "/=":
			case "%=":
				sb.append(updatedColumnList.get(i).getColumnName()).append(" " + operatorList.get(i).charAt(0) + " ")
				.append("(" + upValue + ")");
				break;
			default:
				break;
			}
			if (i != updatedColumnList.size() - 1)
				sb.append(", ");
		}
		sb.append(filter.geneWhereClause(table, distribution));
		return sb.toString();
	}

	/**
	 * 该函数设置预编译SQL语句
	 * 如UPDATE T1 SET c1 = ?, c2 = ? WHERE c1 = ?;
	 * 即使c1和c2指定了值，我们仍然将它们设置为问号。具体值的填充在fillPstmt函数中实现。
	 */
	@Override
	protected String genePstmtSQL() {
		StringBuilder sb = new StringBuilder("UPDATE ")
				.append(table.getTableName()).append(" SET ");
		for (int i = 0; i < updatedColumnList.size(); ++i) {
			sb.append(updatedColumnList.get(i).getColumnName()).append(" = ");
//			String upValue = (!updatedValue.get(i).equals("") ? updatedValue.get(i) 
//					: "?");//除了用户指定的值，其他的值均用问号代替
			String upValue = "?";
			
			switch (operatorList.get(i)) {
			case "=":
				sb.append("(" + upValue + ")");
				break;
			case "++":
				sb.append(updatedColumnList.get(i).getColumnName()).append(" + 1");
				break;
			case "--":
				sb.append(updatedColumnList.get(i).getColumnName()).append(" - 1");
				break;
			case "+=":
			case "-=":
			case "*=":
			case "/=":
			case "%=":
				sb.append(updatedColumnList.get(i).getColumnName()).append(" " + operatorList.get(i).charAt(0) + " ")
				.append("(" + upValue + ")");
				break;
			default:
				break;
			}
			if (i != updatedColumnList.size() - 1)
				sb.append(", ");
		}
		sb.append(filter.genePwhereClause());
		return sb.toString();
	}
	
	@Override
	protected void fillPstmt() {
		int count = 0;
		for (int i = 0; i < updatedColumnList.size(); ++i) {
			System.out.println("UpdatedValue:" + updatedValue.get(i));
			if (updatedValue.get(i).equals("")) { //该列为问号
				setPstmtForUpdatedColumns(++count, updatedColumnList.get(i));
			} else { //该列指定了预设的值
				setPstmtForUpdatedColumns(++count, updatedColumnList.get(i), updatedValue.get(i));
			}
		}
		
		for (int i = 0; i < filter.getfilterColumnList().size(); ++i) {
				setPstmtForFilter(++count, filter.getfilterColumnList().get(i));
		}
	}

	//设定updatedColumns中的预编译列，其中的列使用geneData(random)生成随机值即可，给问号的列设置值
	public void setPstmtForUpdatedColumns(int index, Column column) {
		String dataType = column.getDataType();
		try {
			switch (dataType) {
			case "INT":
			case "INTEGER":
				if (!column.getColumnName().equals(table.getPrimaryKey().getPkColumnNames().get(0)))
					pstmt.setInt(index, (int)column.geneData());
					//TODO 暂不支持对主键的UPDATE
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
				pstmt.setBigDecimal(index, new BigDecimal(column.geneData().toString()));
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

	//设定updatedColumns中的预编译列，列的值为value，需要进行相应转换，给非问号列设置值
	public void setPstmtForUpdatedColumns(int index, Column column, Object value) {
		String dataType = column.getDataType();
		try {
			switch (dataType) {
			case "INT":
			case "INTEGER":
				pstmt.setInt(index, Integer.parseInt(value.toString()));
				//TODO 暂不支持对主键的UPDATE
				break;
			case "LONG":
				pstmt.setLong(index, Long.parseLong(value.toString()));
				break;
			case "FLOAT":
				pstmt.setFloat(index, Float.parseFloat(value.toString()));
				break;
			case "DOUBLE":
				pstmt.setDouble(index, Double.parseDouble(value.toString()));
				break;
			case "DECIMAL":
				pstmt.setBigDecimal(index, new BigDecimal(value.toString()));
				break;
			case "VARCHAR":
			case "CHAR":
				pstmt.setString(index, value.toString());
				break;
			case "BOOL":
				pstmt.setBoolean(index, Boolean.parseBoolean(value.toString()));
				break;
			case "TIMESTAMP":
				pstmt.setTimestamp(index, (Timestamp)column.geneData());
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//设定Filter中的预编译列，其中的列使用geneData(random)生成随机值，这里考虑数据分布
	public void setPstmtForFilter(int index, Column column) {
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
				String temp = geneStmtSQL();
				stmt.executeUpdate(temp);
			}
			return System.nanoTime() - startTime;
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		}
	}
}
