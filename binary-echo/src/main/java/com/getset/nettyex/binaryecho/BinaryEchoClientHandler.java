package com.getset.nettyex.binaryecho;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.ArrayList;
import java.util.List;

public class BinaryEchoClientHandler extends ChannelInboundHandlerAdapter {
    private final List<Integer> integers;

    public BinaryEchoClientHandler() {
        integers = new ArrayList<>(BinaryEchoClient.SIZE);
        for (int i = 0; i < BinaryEchoClient.SIZE; i++) {
            integers.add(Integer.valueOf(i));
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(integers);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
