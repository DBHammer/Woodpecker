package edu.ecnu.woodpecker.performancetest;

import java.io.Serializable;

import edu.ecnu.woodpecker.constant.DistributionType;

public abstract class Distribution implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	DistributionType distributionType = null;
	public abstract long geneRandomValue();
}
