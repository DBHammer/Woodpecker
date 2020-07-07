package edu.ecnu.woodpecker.performancetest;

import java.io.Serializable;
import java.util.List;

public class Index implements Serializable{

	private static final long serialVersionUID = 1L;
	private String indexName = null;
	private String tableName = null;
	private List<String> columnList = null;
	
	public Index(String indexName, String tableName, List<String> columnList) {
		super();
		this.indexName = indexName;
		this.tableName = tableName;
		this.columnList = columnList;
	}
	
	public String geneCreateIndexSQL() {
		StringBuilder sBuilder = new StringBuilder("CREATE INDEX ");
		sBuilder.append(indexName).append(" ON ").append(tableName).append("(");
		for (int i = 0; i < columnList.size(); ++i) {
			sBuilder.append(columnList.get(i));
			if (i != columnList.size() - 1)
				sBuilder.append(", ");
		}
		sBuilder.append(")");
		return sBuilder.toString();
	}
	
	public String geneDropIndexSQL() {
		return "DROP INDEX " + indexName + " ON " + tableName;
	}
}
