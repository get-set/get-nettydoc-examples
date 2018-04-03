package com.getset.nettyex.uptime;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * 周期性的与 server 进行连接，并打印时长。演示了如何使用 netty 开发可靠的重连机制。
 */
public class UptimeClient {
    static final UptimeClientHandler handler = new UptimeClientHandler();
    static final Bootstrap bootstrap = new Bootstrap();
    static final String HOST = System.getProperty("host", "localhost");
    static final int PORT = Integer.parseInt(System.getProperty("port", "8007"));
    // 每过5秒钟重新尝试连接
    static final int RECONNECT_DELAY = Integer.parseInt(System.getProperty("reconnectDelay", "5"));
    // 如果10秒钟收不到服务器的消息则重新连接
    static final int READ_TIMEOUT = Integer.parseInt(System.getProperty("readTimeout", "10"));

    public static void main(String[] args) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();

        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .remoteAddress(HOST, PORT)
                .handler(new ChannelInitializer<SocketChannel>() {
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new IdleStateHandler(READ_TIMEOUT, 0, 0))
                                .addLast(handler);
                    }
                });

        bootstrap.connect();
    }

    static void connect() {
        bootstrap.connect().addListener((ChannelFutureListener) future -> {
            if (future.cause() != null) {
                handler.startTime = -1;
                handler.println("Failed to connect: " + future.cause());
            }
        });
    }
}
