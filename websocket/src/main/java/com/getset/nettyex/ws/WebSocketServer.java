package com.getset.nettyex.ws;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

/**
 * 浏览器的话可以访问 https://localhost:8443，会生成一个 demo 页面；
 * 直接进行 websocket 通讯地址为 wss://localhost:8443/websocket。
 */
public class WebSocketServer {

    private final static boolean SSL = true;
    private final static int PORT = SSL ? 8443 : 8088;
    private static final String WEBSOCKET_PATH = "/websocket";

    public static void main(String[] args) throws Exception {

        // 配置SSL
        final SslContext sslContext;
        if (SSL) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslContext = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } else {
            sslContext = null;
        }

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap b = new ServerBootstrap();

        try {
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            if (SSL) {
                                socketChannel.pipeline().addLast(sslContext.newHandler(socketChannel.alloc()));
                            }
                            socketChannel.pipeline().addLast(
                                    new HttpServerCodec(),
                                    new HttpObjectAggregator(65536),
                                    new WebSocketServerCompressionHandler(),
                                    new WebSocketServerProtocolHandler(WEBSOCKET_PATH, null, true),
                                    // WebSocketIndexPageHandler 用于为浏览器生成一个 demo 页面，纯 websocket 通讯不需要
                                    new WebSocketIndexPageHandler(WEBSOCKET_PATH),
                                    new WebSocketFrameHandler());
                        }
                    });

            // 启动 server
            b.bind(PORT).sync().channel().closeFuture().sync();
        } finally {
            // 停掉 EventLoopGroup 的所有线程
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
