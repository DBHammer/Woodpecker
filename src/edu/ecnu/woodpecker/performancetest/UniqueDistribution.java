package edu.ecnu.woodpecker.performancetest;

import edu.ecnu.woodpecker.constant.DistributionType;

public class UniqueDistribution extends Distribution {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private long startValue;
	private long interval;
	private int minValue;
//	private int maxValue;
	
	public UniqueDistribution(int minValue, int maxValue) {
		this.minValue = minValue;
//		this.maxValue = maxValue;
		this.distributionType = DistributionType.UNIQUE;
	}

	public UniqueDistribution(long startValue, long interval) {
		super();
		this.startValue = startValue + minValue;
		this.interval = interval;
		this.distributionType = DistributionType.UNIQUE;
	}

	@Override
	public long geneRandomValue() {
		long temp = startValue;
		startValue += interval;
		return temp;
	}
}
