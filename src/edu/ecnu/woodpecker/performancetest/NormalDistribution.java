package edu.ecnu.woodpecker.performancetest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import edu.ecnu.woodpecker.constant.DistributionType;
import edu.ecnu.woodpecker.constant.FileConstant;

public class NormalDistribution extends Distribution {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private double mu;
	private double sigma;
	private double range;//正态分布限定在哪个范围内，取几倍的sigma

	private long minValue;
	private long maxValue;
	List<Double> normalDistributionList = null;

	public NormalDistribution(long minValue, long maxValue, double sigma) {
		this.mu = FileConstant.NORMAL_DIST_MU;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.sigma = sigma;
		this.distributionType = DistributionType.NORMAL;
	}

	public NormalDistribution(Distribution distribution) {
		this.minValue = ((NormalDistribution)distribution).minValue;
		this.maxValue = ((NormalDistribution)distribution).maxValue;
		this.mu = FileConstant.NORMAL_DIST_MU;
		this.sigma = ((NormalDistribution)distribution).sigma;
		this.range = FileConstant.NORMAL_DIST_RANGE;
		this.distributionType = DistributionType.NORMAL;
		initNormalDistributionList();
	}

	public void initNormalDistributionList() {
		normalDistributionList = new ArrayList<>();
		double step = 2 * range * sigma / FileConstant.NORMAL_DIST_LIST_SIZE;
		for (double x = -range * sigma; x <= range * sigma; x += step) {
			normalDistributionList.add(Gaussian.cdf(x, mu, sigma));
		}

		List<Double> temp = new ArrayList<>();
		for (int i = 0; i < normalDistributionList.size(); ++i) {
			if (i == 0)
				temp.add(normalDistributionList.get(0));
			else if (i ==  normalDistributionList.size() - 1)
				temp.add(1 - normalDistributionList.get(i - 1));
			else
				temp.add(normalDistributionList.get(i) - normalDistributionList.get(i - 1));
		}
		normalDistributionList.clear();
		normalDistributionList = temp;

		Collections.shuffle(normalDistributionList);
		for (int i = 0; i < normalDistributionList.size(); ++i) {
			if (i == 0)
				normalDistributionList.set(i, normalDistributionList.get(i));
			else if (i == normalDistributionList.size() - 1)
				normalDistributionList.set(i, 1.0);
			else
				normalDistributionList.set(i, normalDistributionList.get(i - 1) + normalDistributionList.get(i));
		}
	}

	@Override
	public long geneRandomValue() {
		double ratio = Math.random();
		for (int i = 0; i < normalDistributionList.size(); ++i) {
			if (ratio <= normalDistributionList.get(i)) {
				long index = (long)(Math.random() * ((maxValue - minValue) / FileConstant.NORMAL_DIST_LIST_SIZE + 1));
				return minValue + index * 10 + i;
			}
		}
		return -1;
	}

	//TEST 
	public static void main(String[] args) {
		Distribution u1 = new NormalDistribution(1, 300000, 1.0);
		Distribution u2 = new NormalDistribution(u1);
		double[] tList = new double[10];
		for (int i = 0; i < tList.length; ++i)
			tList[i] = 0;
		for (int i = 1; i < 10000; ++i) {
			int remainder = (int) (u2.geneRandomValue() % 10);
			tList[remainder]++;
		}
		for (int i = 0; i < tList.length; ++i)
			System.out.println(tList[i]);
	}
}
