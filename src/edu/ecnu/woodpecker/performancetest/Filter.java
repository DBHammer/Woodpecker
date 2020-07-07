package edu.ecnu.woodpecker.performancetest;

import java.io.Serializable;
import java.util.List;

/**
 * SELECT、SELECT_FOR_UPDATE、UPDATE、DELETE条件中的
 * WHERE过滤条件类，主要有列名集合、关系运算符、逻辑运算符。
 * TODO 目前仅支持逻辑运算符的单层嵌套
 * @author 59173
 *
 */
public class Filter implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<Column> filterColumnList;//filterColumnList和relationOperator个数相同
	private List<String> relationalOperator;
	private List<String> logicalOperator;//个数相比于前两个少1
	
	public Filter(List<Column> filterColumnList, List<String> relationalOperator, 
			List<String> logicalOperator) {
		this.filterColumnList = filterColumnList;
		this.relationalOperator = relationalOperator;
		this.logicalOperator = logicalOperator;
	}
	//生成非预编译SQL中的WHERE子句
	public String geneWhereClause(Table table, Distribution distribution) {
		StringBuilder sb = new StringBuilder(" WHERE ");
		for (int i = 0; i < filterColumnList.size(); ++i) {
			sb.append(filterColumnList.get(i).getColumnName()).append(" ")
			.append(relationalOperator.size() != 0 ? relationalOperator.get(i) : "").append(" ");
			long random = distribution.geneRandomValue();
			if (filterColumnList.get(i).getColumnName().equals(table.getPrimaryKey().getPkColumnNames().get(0)))
				sb.append(random);
			else
				sb.append(filterColumnList.get(i).geneData(random));
			
			if (i != filterColumnList.size() -1)
				sb.append(" ").append(logicalOperator.get(i)).append(" ");
		}
		return sb.toString();
	}
	
	//生成预编译SQL语中的WHERE子句
	public String genePwhereClause() {
		StringBuilder sb = new StringBuilder(" WHERE ");
		for (int i = 0; i < filterColumnList.size(); ++i) {
			sb.append(filterColumnList.get(i).getColumnName()).append(" ")
			.append(relationalOperator.size() != 0 ? relationalOperator.get(i) : "").append(" ");
			sb.append("?");
			
			if (i != filterColumnList.size() -1)
				sb.append(" ").append(logicalOperator.get(i)).append(" ");
		}
		return sb.toString();
	}

	public List<Column> getfilterColumnList() {
		return filterColumnList;
	}

	public void setfilterColumnList(List<Column> filterColumnList) {
		this.filterColumnList = filterColumnList;
	}
	
}
