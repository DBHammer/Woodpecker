package edu.ecnu.woodpecker.performancetest;

import edu.ecnu.woodpecker.constant.FileConstant;
import edu.ecnu.woodpecker.constant.LogLevelConstant;
import edu.ecnu.woodpecker.constant.SignConstant;
import edu.ecnu.woodpecker.log.WpLog;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class WorkloadMachineServerHandler extends ChannelInboundHandlerAdapter {
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		Workload workload = (Workload) msg;
		WpLog.recordLog(LogLevelConstant.DEBUG, "Accepted worklaod!"); 
		WpLog.recordLog(LogLevelConstant.DEBUG, "Put workload into workloadQueue...");
		WorkloadMachine.workloadQueue.put(workload);
				
		ctx.writeAndFlush("Thanks for client!");
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}

	// for testing
	public static void main(String[] args) {
		String configPath = System.getProperty("user.dir") + "/"; 
		String configName = FileConstant.DEF_STRESS_TEST_CONFIG_FILE;
		WorkloadConfigInfo.initConfigInfo(configPath, configName);
		
		new Thread(new NetworkServer(Integer.parseInt(WorkloadConfigInfo.dbMachines.get(0).split(SignConstant.COLON_STR)[1]), 
				new WorkloadMachineServerHandler())).start();
		WorkloadMachine.main(args);
//		new Thread(new NetworkServer(22222, new DispatcherNetworkServerHandler())).start();
	}
}
