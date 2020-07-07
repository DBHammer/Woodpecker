package edu.ecnu.woodpecker.performancetest;

import java.io.Serializable;
import java.util.List;

public class PrimaryKey implements Serializable{
	private static final long serialVersionUID = 1L;
	
	// TODO 复合主键的情况
	private List<String> pkColumnNames = null;
	private boolean autoIncrement;

	public PrimaryKey(List<String> pkColumnNames, boolean autoIncrement) {
		super();
		this.pkColumnNames = pkColumnNames;
		this.autoIncrement = autoIncrement;
	}

	public List<String> getPkColumnNames() {
		return pkColumnNames;
	}

	public boolean isAutoIncrement() {
		return autoIncrement;
	}
}
