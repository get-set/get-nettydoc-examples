package com.getset.nettyex.uptime;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import static io.netty.channel.ChannelHandler.Sharable;

@Sharable
public class UptimeServerHandler extends SimpleChannelInboundHandler {

    long counter;

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        // discard
        System.out.println("[SERVER] discarded " + counter++ + " messages.");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
