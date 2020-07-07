package edu.ecnu.woodpecker.performancetest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import edu.ecnu.woodpecker.constant.LogLevelConstant;
import edu.ecnu.woodpecker.constant.SignConstant;
import edu.ecnu.woodpecker.log.WpLog;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

@Sharable
public class DispatcherNetworkServerHandler extends ChannelInboundHandlerAdapter {
	public StringBuilder sBuilder = new StringBuilder();
	
	private CopyOnWriteArrayList<AbstractResultForClient> arfcList = null;
	// 综合所有负载机的结果集算出来的平均TPS，延迟，50%延迟，90%延迟，95%延迟，99%延迟
	private long totalTPS = 0;
//    private double TP50Latency = 0;
//    private double TP90Latency = 0;
//    private double TP95Latency = 0;
//    private double TP99Latency = 0;
    
	public DispatcherNetworkServerHandler() {
		arfcList = new CopyOnWriteArrayList<>();
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		AbstractResultForClient arfc = (AbstractResultForClient)msg;
		// 测试接收到的ThreadRunResults的近似大小
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		ObjectOutputStream oos = new ObjectOutputStream(baos);
//		oos.writeObject(arfc);
//		oos.close();
//		System.out.println("Object size:" + baos.size());
		System.out.println(arfc.toString());
//		sBuilder.append(arfc.toString());
		 
		String ip = arfc.workloadMachineIP;
		boolean isDuplicate = false; // 判断arfcList中是否已经有该IP的结果集，若没有则加入，有则跳过
		if (arfc.isFinished) {
			for (int i = 0; i < arfcList.size(); ++i) {
				if (ip.equals(arfcList.get(i).workloadMachineIP))
					isDuplicate = true;
			}
			//若该arfc没在arfcList中，则将它加入arfcList中
			if (!isDuplicate)
				arfcList.add(arfc);
			
			//若arfcList大小为负载机的个数，则说明所有负载机均已完成所有负载，可以进行结果汇总
			if (arfcList.size() == WorkloadConfigInfo.defaultLoadmachineNumber) {
				boolean isOneWorkload = true; //表示接收到的最终结果是否为同一个workload
				if (arfcList.size() > 1) 
					for (int i = 0; i < arfcList.size() - 1; ++i)
						if (arfcList.get(i).workloadID != arfcList.get(i).workloadID)
							isOneWorkload = false;
				
				if (isOneWorkload) { //来自同一个worklaod
//					System.out.println("-");
//					String[] temp = sBuilder.toString().split("[ \\n]+");
//					for (int i = 0; i < temp.length; i += 2)
//						System.out.println(temp[i]);
//					System.out.println();
//					for (int i = 1; i < temp.length; i += 2)
//						System.out.println(temp[i]);
					// 统计指标并输出
					outputResult(arfcList);
					Dispatcher.isOneWorkloadFinished = true;
				} else { //出错，接收到不同的workload结果,则跳过当前workload的结果汇总
					WpLog.recordLog(LogLevelConstant.ERROR, "Get wrong workload result!");
					Dispatcher.isOneWorkloadFinished = true;
				}
				
				//若该worklaod为最后一个负载，关闭所有负载机上的Server进程，并结束该程序
				if (arfcList.get(0).workloadID == Dispatcher.allTypeWorkloadNumber) {
					for (int i = 0; i < arfcList.size(); ++i) {
						String IP = (arfcList.get(i).workloadMachineIP);
				        WpLog.recordLog(LogLevelConstant.INFO, "Closing process in %s...", IP);
						Dispatcher.closeProcess(IP);
				        WpLog.recordLog(LogLevelConstant.INFO, "Closing process in %s successfully!", IP);
					}
//					System.exit(0);
				} else  //否则清空arfcList
					arfcList.clear();
			}
		}
	}

	public void outputResult(CopyOnWriteArrayList<AbstractResultForClient> arfcList) {
		AbstractResultForClient arfc;
		// 计算指标并输出
		totalTPS = 0;
		for (int t = 0; t < arfcList.size(); ++t) {
			arfc = arfcList.get(t);
			totalTPS += arfc.workloadMachineTPS;
		}
		
		System.out.println("-----WorkloadID:" + arfcList.get(0).workloadID + "'s TOTAL RESULT----------");
		System.out.println("Total TPS:" + (totalTPS));
//		System.out.println("Total TP50Latency:" + (TP50Latency / WorkloadConfigInfo.defaultLoadmachineNumber));
//		System.out.println("Total TP90Latency:" + (TP90Latency / WorkloadConfigInfo.defaultLoadmachineNumber));
//		System.out.println("Total TP95Latency:" + (TP95Latency / WorkloadConfigInfo.defaultLoadmachineNumber));
//		System.out.println("Total TP99Latency:" + (TP99Latency / WorkloadConfigInfo.defaultLoadmachineNumber));
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}

	public static void main(String[] args) {
		int listenPort = Integer.parseInt(WorkloadConfigInfo.dispatcher.split(SignConstant.COLON_STR)[1]);
		KillPortApp.ports = new HashSet<>();
		KillPortApp.ports.add(listenPort);
		Runtime runtime = Runtime.getRuntime();
        try {
            //查找进程号
            Process p = runtime.exec("cmd /c netstat -ano | findstr \"" + listenPort +"\"");
            InputStream inputStream = p.getInputStream();
            List<String> read = KillPortApp.read(inputStream, "UTF-8");
            if(read.size() == 0) {
            	WpLog.recordLog(LogLevelConstant.INFO, "Port：" + listenPort + " is not occupied!");
        		WpLog.recordLog(LogLevelConstant.INFO, "A new dispatcher server started!");
        		new Thread(new NetworkServer(Integer.parseInt(WorkloadConfigInfo.dispatcher.split(SignConstant.COLON_STR)[1]), 
        				new DispatcherNetworkServerHandler())).start();
            } else {
            	WpLog.recordLog(LogLevelConstant.INFO, "Find " + read.size( )+ " process on port, Ready to clear...");
                if (KillPortApp.killWithPort(listenPort) == true)
                	WpLog.recordLog(LogLevelConstant.INFO, "Clear successfully!");
                else
                	WpLog.recordLog(LogLevelConstant.ERROR, "Clear error!");
            }	
        } catch (IOException e) {
            e.printStackTrace();
        } 
	}
}