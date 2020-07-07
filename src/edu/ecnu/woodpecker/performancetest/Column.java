package edu.ecnu.woodpecker.performancetest;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;

import edu.ecnu.woodpecker.constant.FileConstant;

public class Column implements Serializable{
	private static final long serialVersionUID = 1L;

	private char[] chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

	private String columnName = null;
	private String dataType = null;//记录INT、DOUBLE、CHAR、VARCHAR等
	private String fullDataType = null;//记录VARCHAR(70)、DECIMAL(5,2)等，创建表时用

	//每一列的数据分布参数
	private float nullRatio = FileConstant.NULL_RATIO;
	private int cardinality = FileConstant.CARDINALITY;
	private double minValue = FileConstant.MIN_VALUE;
	private double maxValue = FileConstant.MAX_VALUE;

	private int minLength = FileConstant.MIN_LENGTH;
	private int maxLength = FileConstant.MAX_LENGTH;
	private int seedNumber = FileConstant.SEEDS_NUMBER;
	
	private int decimalA = 0;
	private int decimalB = 0;
	private String[] seeds = null;

	public Column(String columnName, String dataType) {
		super();
		this.columnName = columnName;
		this.dataType = dataType.trim().toUpperCase();
		this.fullDataType = this.dataType;
		
		if (dataType.indexOf("(") != -1) {
			this.dataType = this.dataType.substring(0, this.dataType.indexOf("("));
			if (this.dataType.equals("VARCHAR")) {
				this.maxLength = Integer.parseInt(this.fullDataType.substring(this.fullDataType.indexOf("(") + 1,
						this.fullDataType.indexOf(")")));
				initSeeds();
			} else if (this.dataType.equals("DECIMAL")) {
				this.decimalA = Integer.parseInt(this.fullDataType.substring(this.fullDataType.indexOf("(") + 1,
						this.fullDataType.indexOf(",")));
				this.decimalB = Integer.parseInt(this.fullDataType.substring(this.fullDataType.indexOf(",") + 1,
						this.fullDataType.indexOf(")")));
			}
		}
		//TODO 初始化各个列的范围
	}

	public Object geneData(long random) {
		//我们默认只有在INSERT插入数据的时候，才有可能将某个属性置为空，对于SELECT（SelectForUpdate）、UPDATE、DELETE等操作中
		//不存在将where条件子句中的某个属性置为空
		if (Math.random() < nullRatio) {
			switch(dataType) {
			case "INT":case "INTEGER":case "LONG":
				return Types.INTEGER;
			case "FLOAT":
				return Types.FLOAT;
			case "DOUBLE":
				return Types.DOUBLE;
			case "DECIMAL":
				return Types.DECIMAL;
			case "VARCHAR":
				return Types.VARCHAR;
			case "CHAR":
				return Types.CHAR;
			case "BOOL":
				return Types.BOOLEAN;
			case "TIMESTAMP":
				return Types.TIMESTAMP;
			}
		}
		
		switch (dataType) {
		case "INT":
		case "INTEGER":
			int idx = (int)(Math.ceil(Math.random() * cardinality));
			double c = (maxValue - minValue) / cardinality;
			int temp = (int)(minValue + idx * c);
			return temp;
		case "LONG":
			return (long)((maxValue - minValue) / random) * (int)(Math.random() * random);
		case "FLOAT":
			return (float)((maxValue - minValue) / random) * (int)(Math.random() * random);
		case "DOUBLE":
			return ((maxValue - minValue) / random) * (int)(Math.random() * random);
		case "DECIMAL":
			return new BigDecimal((double)(Math.random() * (Math.pow(10, this.decimalA))) 
					/ Math.pow(10, this.decimalB)).
					setScale(this.decimalB,  BigDecimal.ROUND_DOWN).doubleValue();
		case "VARCHAR":
			int randomIndex = (int)(Math.random() * cardinality);
//			return String.valueOf(randomIndex) + seeds[randomIndex % seedNumber];
			return seeds[randomIndex % seedNumber];
		case "CHAR":
			return chars[(int)(Math.random() * 62)];
		case "BOOL":
			return Math.random() < 0.5 ? true : false;
		case "TIMESTAMP":
			return new Timestamp(new Date().getTime());
		default:
			return null;
		}
	}
		
	public Object geneData() {
		if (Math.random() < nullRatio) {
			switch(dataType) {
			case "INT":case "INTEGER":case "LONG":
				return Types.INTEGER;
			case "FLOAT":
				return Types.FLOAT;
			case "DOUBLE":
				return Types.DOUBLE;
			case "DECIMAL":
				return Types.DECIMAL;
			case "VARCHAR":
				return Types.VARCHAR;
			case "CHAR":
				return Types.CHAR;
			case "BOOL":
				return Types.BOOLEAN;
			case "TIMESTAMP":
				return Types.TIMESTAMP;
			}
		}
			
		switch (dataType) {
		case "INT":
		case "INTEGER":
			int idx = (int)(Math.ceil(Math.random() * cardinality));
			double c = (maxValue - minValue) / cardinality;
			int temp = (int)(minValue + idx * c);
			return temp;
		case "LONG":
			return (long)((maxValue - minValue) / cardinality) * (int)(Math.random() * cardinality);
		case "FLOAT":
			return (float)((maxValue - minValue) / cardinality) * (int)(Math.random() * cardinality);
		case "DOUBLE":
			return ((maxValue - minValue) / cardinality) * (int)(Math.random() * cardinality);
		case "DECIMAL":
			return new BigDecimal((double)(Math.random() * (Math.pow(10, this.decimalA))) 
					/ Math.pow(10, this.decimalB)).
					setScale(this.decimalB,  BigDecimal.ROUND_DOWN).doubleValue();
		case "VARCHAR":
			int randomIndex = (int)(Math.random() * cardinality);
//			return String.valueOf(randomIndex) + seeds[randomIndex % seedNumber];
			return seeds[randomIndex % seedNumber];
		case "CHAR":
			return chars[(int)(Math.random() * 62)];
		case "BOOL":
			return Math.random() < 0.5 ? true : false;
		case "TIMESTAMP":
			return new Timestamp(new Date().getTime());
		default:
			return null;
		}
	}

	private String geneRandomString(int length) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++)
			sb.append(chars[(int)(Math.random() * 62)]);
		return sb.toString();
	}

	public String getColumnName() {
		return columnName;
	}

	public String getDataType() {
		return dataType;
	}

	public String getFullDataType() {
		return fullDataType;
	}
	public int getMaxLength() {
		return maxLength;
	}

	public float getNullRatio() {
		return nullRatio;
	}

	public int getCardinality() {
		return cardinality;
	}

	public double getMinValue() {
		return minValue;
	}

	public double getMaxValue() {
		return maxValue;
	}

	public void setNullRatio(float nullRatio) {
		this.nullRatio = nullRatio;
	}

	public void setCardinality(int cardinality) {
		this.cardinality = cardinality;
	}

	public void setMinValue(double minValue) {
		this.minValue = minValue;
	}

	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
	}

	public void setMinLength(int minLength) {
		this.minLength = minLength;
	}

	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}

	public void setSeedNumber(int seedNumber) {
		this.seedNumber = seedNumber;
	}

	public void initSeeds() {
		seeds = new String[seedNumber];
		for (int i = 0; i < seedNumber; i++)
			seeds[i] = geneRandomString((int)(Math.random() * (maxLength - minLength + 1)) + minLength);
	}
}
