package com.getset.nettyex.telnet;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class TelnetClient {
    private final static boolean SSL = System.getProperty("ssl") != null;
    private final static String HOST = System.getProperty("host", "localhost");
    private final static int PORT = Integer.parseInt(System.getProperty("port", SSL ? "8992" : "8023"));

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
                        socketChannel.pipeline()
                                .addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()))
                                .addLast(new StringDecoder())
                                .addLast(new StringEncoder())
                                .addLast(new TelnetClientHandler());
                    }
                });

        try {
            // 启动 Client
            ChannelFuture future = bootstrap.connect(HOST, PORT).sync();

            // 将控制台的输入发送给 server
            Channel channel = future.channel();
            ChannelFuture lastWriteFuture = null;
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
            String line;
            while (true) {
                line = input.readLine();
                if (line == null) {
                    break;
                }
                lastWriteFuture = channel.writeAndFlush(line + "\r\n");
                if ("bye".equals(line.toLowerCase())) {
                    // 等待关闭
                    channel.closeFuture().sync();
                    break;
                }
            }
            if (lastWriteFuture != null) {
                lastWriteFuture.sync();
            }

        } finally {
            group.shutdownGracefully();
        }
    }
}
