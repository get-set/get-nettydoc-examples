package com.getset.nettyex.quote;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.unix.DatagramSocketAddress;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.SocketUtils;

public class QuoteClient {
    private final static int PORT = Integer.parseInt(System.getProperty("port", "7686"));

    public static void main(String[] args) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            // 注意，这里并非是 ServerBootstrap
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new ChannelInitializer<DatagramChannel>() {
                        protected void initChannel(DatagramChannel ch) throws Exception {
                            ch.pipeline().addLast(new QuoteClientHandler());
                        }
                    });
            // 注意，这里是 bind 端口 0
            Channel ch = b.bind(0).sync().channel();

            // 首先发出一个数据报
            ch.writeAndFlush(new DatagramPacket(
                    Unpooled.copiedBuffer("QOTM?", CharsetUtil.UTF_8),
                    SocketUtils.socketAddress("255.255.255.255", PORT))).sync();

            // 若超过5秒没有完成发送则打印超时信息
            if (!ch.closeFuture().await(5000)) {
               System.err.println("QOTM request timed out.");
            }
        } finally {
            group.shutdownGracefully();
        }
    }
}
