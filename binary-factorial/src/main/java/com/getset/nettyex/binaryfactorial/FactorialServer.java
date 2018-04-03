package com.getset.nettyex.binaryfactorial;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.compression.ZlibCodecFactory;
import io.netty.handler.codec.compression.ZlibWrapper;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

public class FactorialServer {

    private final static boolean SSL = System.getProperty("ssl") != null;
    private final static int PORT = Integer.parseInt(System.getProperty("port", "8322"));

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
                                    // 用于进行数据压缩
                                    ZlibCodecFactory.newZlibEncoder(ZlibWrapper.GZIP),
                                    ZlibCodecFactory.newZlibDecoder(ZlibWrapper.GZIP),
                                    // 用于数据编解码
                                    new BigIntegerDecoder(),
                                    new NumberEncoder(),
                                    // 业务Handler
                                    new FactorialServerHandler());
                        }
                    });

            b.bind(PORT).sync().channel().closeFuture().sync();
        } finally {
            // 停掉 EventLoopGroup 的所有线程
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
