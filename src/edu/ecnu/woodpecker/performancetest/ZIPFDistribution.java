package edu.ecnu.woodpecker.performancetest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.math3.distribution.ZipfDistribution;
import edu.ecnu.woodpecker.constant.DistributionType;
import edu.ecnu.woodpecker.constant.FileConstant;

public class ZIPFDistribution extends Distribution {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int size;
	private double skew;
	private ZipfDistribution zipfDistribution = null;
	
	private long minValue;
	private long maxValue;
	List<Double> zipfDistributionList = null;
	
	public ZIPFDistribution(long minValue, long maxValue, int size, double skew) {
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.size = size;
		this.skew = skew;
		this.distributionType = DistributionType.ZIPF;
	}
	
	public ZIPFDistribution(Distribution distribution) {
		this.minValue = ((ZIPFDistribution)distribution).minValue;
		this.maxValue = ((ZIPFDistribution)distribution).maxValue;
		this.size = ((ZIPFDistribution)distribution).size;
		this.skew = ((ZIPFDistribution)distribution).skew;
		this.distributionType = DistributionType.ZIPF;
		zipfDistribution = new ZipfDistribution(size, skew);
		initZipfDistributionList();
	}
	
	public void initZipfDistributionList() {
		zipfDistributionList = new ArrayList<>();
		int step = size / FileConstant.ZIPF_DIST_LIST_SIZE;
		for (int x = 1; x <= size; x += step) {
			zipfDistributionList.add(zipfDistribution.cumulativeProbability(x));
		}
		
		List<Double> temp = new ArrayList<>();
		for (int i = 0; i < zipfDistributionList.size(); ++i) {
	    		if (i == 0)
	    			temp.add(zipfDistributionList.get(0));
	    		else if (i ==  zipfDistributionList.size() - 1)
	    			temp.add(1 - zipfDistributionList.get(i - 1));
	    		else
	    		 	temp.add(zipfDistributionList.get(i) - zipfDistributionList.get(i - 1));
	    }
		zipfDistributionList.clear();
		zipfDistributionList = temp;
		
		Collections.shuffle(zipfDistributionList);
	    for (int i = 0; i < zipfDistributionList.size(); ++i) {
			if (i == 0)
				zipfDistributionList.set(i,  zipfDistributionList.get(i));
			else if (i == zipfDistributionList.size() - 1)
				zipfDistributionList.set(i, 1.0);
			else
				zipfDistributionList.set(i,  zipfDistributionList.get(i - 1) + zipfDistributionList.get(i));
	    }
	}

	@Override
	public long geneRandomValue() {
		double ratio = Math.random();
		for (int i = 0; i < zipfDistributionList.size(); ++i) {
			if (ratio <= zipfDistributionList.get(i)) {
				long index = (long)(Math.random() * ((maxValue - minValue) / 
						FileConstant.ZIPF_DIST_LIST_SIZE + 1));
				return minValue + index * 10 + i;
			}
		}
		return -1;
	}
	
	//TEST
	public static void main(String[] args) {
		ZIPFDistribution zipf1 = new ZIPFDistribution(1, 300000, 10, 1);
		ZIPFDistribution zipf2 = new ZIPFDistribution(zipf1);
		double[] tList = new double[10];
		for (int i = 0; i < tList.length; ++i)
			tList[i] = 0;
		for (int i = 1; i <= 1000; ++i) {
			int remainder = (int) (zipf2.geneRandomValue() % 10);
			tList[remainder]++;
		}
		for (int i = 0; i < tList.length; ++i)
			System.out.println(tList[i]);
	}
}
