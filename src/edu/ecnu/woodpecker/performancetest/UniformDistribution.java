package edu.ecnu.woodpecker.performancetest;

import edu.ecnu.woodpecker.constant.DistributionType;

public class UniformDistribution extends Distribution {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private long minValue;
	private long maxValue;
	
	public UniformDistribution(long minValue, long maxValue) {
		super();
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.distributionType = DistributionType.UNIFORM;
	}
	
	public UniformDistribution(Distribution distribution) {
		this.minValue = ((UniformDistribution)distribution).minValue;
		this.maxValue = ((UniformDistribution)distribution).maxValue;
		this.distributionType = DistributionType.UNIFORM;
	}
	
	@Override
	public long geneRandomValue() {
		return (long)(Math.random() * (maxValue - minValue + 1)) + minValue;
	}
}
