package com.getset.nettyex.quote;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.ThreadLocalRandom;


public class QuoteServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    // Quotes from Mohandas K. Gandhi:
    private static final String[] quotes = {
            "Where there is love there is life.",
            "First they ignore you, then they laugh at you, then they fight you, then you win.",
            "Be the change you want to see in the world.",
            "The weak can never forgive. Forgiveness is the attribute of the strong.",
    };

    private static String nextQuote() {
        int index = ThreadLocalRandom.current().nextInt(quotes.length);
        return quotes[index];
    }

    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        System.out.println(msg);
        if ("QOTM?".equals(msg.content().toString(CharsetUtil.UTF_8))) {
            ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer("QOTM: " + nextQuote(), CharsetUtil.UTF_8), msg.sender()));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        // 并不关闭channel，可以继续进行响应
    }
}
