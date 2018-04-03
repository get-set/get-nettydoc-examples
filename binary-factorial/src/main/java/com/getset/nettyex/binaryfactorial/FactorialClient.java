package com.getset.nettyex.binaryfactorial;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.compression.ZlibCodecFactory;
import io.netty.handler.codec.compression.ZlibWrapper;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

public class FactorialClient {
    final static int COUNT = Integer.parseInt(System.getProperty("count", "100"));
    private final static boolean SSL = System.getProperty("ssl") != null;
    private final static String HOST = System.getProperty("host", "localhost");
    private final static int PORT = Integer.parseInt(System.getProperty("port", "8322"));

    public static void main(String[] args) throws Exception {
        // 配置SSL
        final SslContext sslContext;
        if (SSL) {
            sslContext = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        } else {
            sslContext = null;
        }
        EventLoopGroup group = new NioEventLoopGroup();

        Bootstrap b = new Bootstrap();

        b.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        if (SSL) {
                            socketChannel.pipeline().addLast(sslContext.newHandler(socketChannel.alloc(), HOST, PORT));
                        }
                        socketChannel.pipeline().addLast(
                                // 用于进行数据压缩
                                ZlibCodecFactory.newZlibEncoder(ZlibWrapper.GZIP),
                                ZlibCodecFactory.newZlibDecoder(ZlibWrapper.GZIP),
                                // 用于数据编解码
                                new BigIntegerDecoder(),
                                new NumberEncoder(),
                                // 业务Handler
                                new FactorialClientHandler());
                    }
                });

        try {
            b.connect(HOST, PORT).sync().channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}
