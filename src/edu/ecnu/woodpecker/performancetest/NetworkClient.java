package edu.ecnu.woodpecker.performancetest;

import java.util.ArrayList;
import java.util.List;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class NetworkClient {

	private int threadNum = 1;
	private List<Channel> channels = new ArrayList<Channel>();

	public NetworkClient(List<String> hosts, List<Integer> serverPorts, 
			ChannelInboundHandlerAdapter clientHandler) {
		connect(hosts, serverPorts, clientHandler);
	}

	private void connect(List<String> hosts, List<Integer> serverPorts, 
			ChannelInboundHandlerAdapter clientHandler) {
		EventLoopGroup group = new NioEventLoopGroup(threadNum);
		Bootstrap b = new Bootstrap();
		b.group(group).channel(NioSocketChannel.class)
		.option(ChannelOption.TCP_NODELAY, true)
		.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.
						weakCachingConcurrentResolver(this.getClass().getClassLoader())));
				ch.pipeline().addLast(new ObjectEncoder());
				ch.pipeline().addLast(clientHandler);
			}
		});
		for (int i = 0; i < hosts.size(); i++) {
			Channel channel = null;
			try {
				channel = b.connect(hosts.get(i), serverPorts.get(i)).sync().channel();
			} catch (Exception e) {
				e.printStackTrace();
			}
			while (channel == null || !channel.isWritable()) {
				try {
					Thread.sleep(1000);
					channel = b.connect(hosts.get(i), serverPorts.get(i)).sync().channel();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			channels.add(channel);
		}
	}

	public void send(Object object, int hostID) {
		// append ...
		channels.get(hostID).writeAndFlush(object);
	}
}
