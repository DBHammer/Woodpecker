package edu.ecnu.woodpecker.performancetest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;



public class AbstractResultForClient implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public int workloadID = 0;
	public long runTime = 0;
	public String workloadMachineIP;
	public int txnNumber;
	public String time;
	public double progress;
	public long workloadMachineTPS;

	public List<Double> txnAvgLatency;
	public List<Double> txn50Latency;
	public List<Double> txn90Latency;
	public List<Double> txn95Latency;
	public List<Double> txn99Latency;
	public boolean isFinished;
	public AbstractResultForClient(int workloadID, String workloadMachineIP, int txnNumber) {

		this.workloadID = workloadID;
		this.workloadMachineIP = workloadMachineIP;
		this.txnNumber = txnNumber;
		txnAvgLatency = new ArrayList<>();
		txn50Latency = new ArrayList<>();
		txn90Latency = new ArrayList<>();
		txn95Latency = new ArrayList<>();
		txn99Latency = new ArrayList<>();
		for (int l = 0; l < txnNumber; ++l) {
			txnAvgLatency.add(0.0);
			txn50Latency.add(0.0);
			txn90Latency.add(0.0);
			txn95Latency.add(0.0);
			txn99Latency.add(0.0);
		}
	}
	
	@Override
	public String toString() {



		StringBuilder sb = new StringBuilder
				("\n-----------" + time + ": " + 
				workloadMachineIP + "'s result----------\n" +
				"WorkloadID:" + workloadID + "\n" +
				"Progress:" + ((double)Math.round(progress * 100 * 100) / 100) + "%\n" +
				"Spent Time:" + runTime + "(s)\n" +
				"Finished:" + isFinished + "\n" +
				"WorkloadMachineTPS:" + workloadMachineTPS + "\n");
		for (int i = 0; i < txnNumber; ++i) {
			sb.append("----------SPLIT LINE----------\n");
			sb.append("TXN" + (i + 1) + "'s average Latency:" + txnAvgLatency.get(i) + "\n");
			sb.append("TXN" + (i + 1) + "'s 50% Latency:" + txn50Latency.get(i) + "\n");
			sb.append("TXN" + (i + 1) + "'s 90% Latency:" + txn90Latency.get(i) + "\n");
			sb.append("TXN" + (i + 1) + "'s 95% Latency:" + txn95Latency.get(i) + "\n");
			sb.append("TXN" + (i + 1) + "'s 99% Latency:" + txn99Latency.get(i) + "\n");


		}





		sb.append("--------------------END--------------------");
		
		return sb.toString();
	}
}
