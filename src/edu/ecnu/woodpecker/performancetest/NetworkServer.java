package edu.ecnu.woodpecker.performancetest;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class NetworkServer implements Runnable {

	private int threadNum = 1;
	private int serverPort;
	private ChannelInboundHandlerAdapter serverHandler = null;

	public NetworkServer(int serverPort, ChannelInboundHandlerAdapter serverHandler) {
		this.serverPort = serverPort;
		this.serverHandler = serverHandler;
	}

	private void bind() {
		EventLoopGroup bossGroup = new NioEventLoopGroup(threadNum);
		EventLoopGroup workerGroup = new NioEventLoopGroup(threadNum);
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
			.channel(NioServerSocketChannel.class)
			.option(ChannelOption.SO_BACKLOG, 1024)
			.handler(new LoggingHandler(LogLevel.INFO))
			.childHandler(new ChannelInitializer<SocketChannel>() {
				@Override
				public void initChannel(SocketChannel ch) {
					ch.pipeline().addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.
							weakCachingConcurrentResolver(this.getClass().getClassLoader())));
					ch.pipeline().addLast(new ObjectEncoder());
					ch.pipeline().addLast(serverHandler);
				}
			});
			ChannelFuture cf = b.bind(serverPort).sync();
			cf.channel().closeFuture().sync();
		} catch(Exception e) { 
			e.printStackTrace();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

	@Override
	public void run() {
		bind();
	}
}
