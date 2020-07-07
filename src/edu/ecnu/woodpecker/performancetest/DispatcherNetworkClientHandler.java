package edu.ecnu.woodpecker.performancetest;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

@Sharable
public class DispatcherNetworkClientHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		// append ...
		System.out.println(msg.toString());
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}

	// for testing
//	public static void main(String[] args) {
//		List<String> hosts = Arrays.asList("172.30.237.7", "172.30.237.7");
//		List<Integer> serverPorts = Arrays.asList(11111, 22222);
//		NetworkClient client = new NetworkClient(hosts, serverPorts, 
//				new DispatcherNetworkClientHandler());
//		
//		String configPath = System.getProperty("user.dir") + "/s_t/";
////		String configName = "default";
//		String configName = "mysql";
//		WorkloadConfigInfo.initConfigInfo(configPath, configName);
//		
//    	List<Column> columns = new ArrayList<>();
//    	Column colSid = new Column("sid", "int");
//		Column colSage = new Column("sage",	"int");
//		Column colScore = new Column("score", "double");
//		Column colShome = new Column("shome", "varchar(70)");
//		Column colSdecimal = new Column("sdecimal", "char");
//		
//		columns.add(colSid);
//		columns.add(colSage);
//		columns.add(colScore);
//		columns.add(colShome);
//		columns.add(colSdecimal);
//		
//		List<String> pkColumnNames = new ArrayList<String>();
//		pkColumnNames.add(colSid.getColumnName());
//		boolean autoIncrement = true;
//		
//		PrimaryKey pKey =  new PrimaryKey(pkColumnNames, autoIncrement);
//		
//		Table table = new Table("student", 5000, columns, pKey,
//				null, null);
//		
//		List<Table> tbList = new ArrayList<>();
//		tbList.add(table);
//		
//		Dispatcher dispatcher = new Dispatcher();
//		dispatcher.setTableList(tbList);
//
//		dispatcher.preprocess();
//		Workload workload = dispatcher.getTableWorkloadList().get(0);
//		
//		client.send(workload, 0);
//		client.send(workload, 1);
//		client.send(workload, 0);
//		System.out.println("Client sent over!");  
//	}
}
