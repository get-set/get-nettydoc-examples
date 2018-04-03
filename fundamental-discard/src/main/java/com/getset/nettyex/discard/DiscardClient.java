package com.getset.nettyex.discard;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

/**
 * 持续不断地向服务端发送消息。
 */
public class DiscardClient {
    private final static boolean SSL = System.getProperty("ssl") != null;
    private final static String HOST = System.getProperty("host", "localhost");
    private final static int PORT = Integer.parseInt(System.getProperty("port", "8007"));
    final static int SIZE = Integer.parseInt(System.getProperty("size", "256"));

    public static void main(String[] args) throws Exception {
        // 配置SSL
        final SslContext sslContext;
        if (SSL) {
            sslContext = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        } else {
            sslContext = null;
        }
        EventLoopGroup group = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();

        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        if (SSL) {
                            socketChannel.pipeline().addLast(sslContext.newHandler(socketChannel.alloc()));
                        }
                        socketChannel.pipeline().addLast(new DiscardClientHandler());
                    }
                });

        try {
            // 启动 Client
            ChannelFuture future = bootstrap.connect(HOST, PORT).sync();

            // 等待关闭
            future.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}
