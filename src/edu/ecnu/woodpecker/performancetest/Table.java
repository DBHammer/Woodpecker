package edu.ecnu.woodpecker.performancetest;

import java.io.Serializable;
import java.util.List;

public class Table implements Serializable {
	private static final long serialVersionUID = 1L;
	// 表基本信息项
	private String tableName = null;
	private long tableSize;
	private List<Column> columnList = null;
	private PrimaryKey primaryKey = null;
	private List<ForeignKey> foreignKeyList = null;
	private List<Index> indexList = null;

	public Table(String tableName, long tableSize, List<Column> columnList, PrimaryKey primaryKey,
			List<ForeignKey> foreignKeyList, List<Index> indexList) {
		super();
		this.tableName = tableName;
		this.tableSize = tableSize;
		this.columnList = columnList;
		this.primaryKey = primaryKey;
		this.foreignKeyList = foreignKeyList;
		this.indexList = indexList;
	}

	// 生成创建表的SQL语句
	public String geneCreateTableSQL() {
		StringBuilder sb = new StringBuilder("CREATE TABLE ");
		sb.append(this.tableName).append(" ( ");

		for (Column column : this.columnList) {
			sb.append(column.getColumnName()).append(" ").append(column.getFullDataType());
			sb.append(", ");
		}
		sb.append(" PRIMARY KEY (").append(primaryKey.getPkColumnNames().get(0));
		sb.append(")").append(")");

		String pkType = null;
		for (Column column : columnList) {
			if (column.getColumnName().equals(primaryKey.getPkColumnNames().get(0)))
				pkType = column.getFullDataType();
		}

		// 若主键自增，将"sid INT"替换为"sid INT AUTO_INCREMENT"
		String temp = primaryKey.getPkColumnNames().get(0) + " " + pkType;
		if (primaryKey.isAutoIncrement())
			return sb.toString().replaceFirst(temp, temp + " AUTO_INCREMENT");
		return sb.toString();
	}
	
	// 生成删除表的SQL语句
	public String geneDropTableSQL() {
		return "DROP TABLE IF EXISTS " + tableName;
	}

	public String getTableName() {
		return tableName;
	}

	public long getTableSize() {
		return tableSize;
	}

	public List<Column> getColumnList() {
		return columnList;
	}

	public PrimaryKey getPrimaryKey() {
		return primaryKey;
	}

	public List<ForeignKey> getForeignKeyList() {
		return foreignKeyList;
	}

	public List<Index> getIndexList() {
		return indexList;
	}

	public void setColumnList(List<Column> columnList) {
		this.columnList = columnList;
	}

	@Override
	public String toString() {
		return "Table [tableName=" + tableName + ", tableSize=" + tableSize + ", columnList=" + columnList
				+ ", primaryKey=" + primaryKey + ", foreignKeyList=" + foreignKeyList + ", indexList=" + indexList
				+ "]";
	}
}

class ForeignKey implements Serializable {
	private static final long serialVersionUID = 1L;
	private String columnName = null;
	private String referencedTableName = null;
	private String referencedColumnName = null;

	public ForeignKey(String columnName, String referencedTableName, String referencedColumnName) {
		super();
		this.columnName = columnName;
		this.referencedTableName = referencedTableName;
		this.referencedColumnName = referencedColumnName;
	}

	public String getColumnName() {
		return columnName;
	}

	public String getReferencedTableName() {
		return referencedTableName;
	}

	public String getReferencedColumnName() {
		return referencedColumnName;
	}
}
