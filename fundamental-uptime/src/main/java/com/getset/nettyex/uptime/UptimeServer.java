package com.getset.nettyex.uptime;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * 丢弃来自客户端的信息，收不到回应的 client 会重连。
 */
public class UptimeServer {

    private final static int PORT = Integer.parseInt(System.getProperty("port", "8007"));

    public static void main(String[] args) throws Exception {

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap serverBootstrap = new ServerBootstrap();

        try {
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new UptimeServerHandler());
                        }
                    });

            // 启动EchoServer
            ChannelFuture future = serverBootstrap.bind(PORT).sync();
            // 等待server socket 关闭
            future.channel().closeFuture().sync();
        } finally {
            // 停掉 EventLoopGroup 的所有线程
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
