package com.getset.nettyex.binaryfactorial;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;

import java.math.BigInteger;

public class FactorialServerHandler extends SimpleChannelInboundHandler<BigInteger> {

      private BigInteger lastMultiplier = new BigInteger("1");
      private BigInteger factorial = new BigInteger("1");

      @Override
      public void channelRead0(ChannelHandlerContext ctx, BigInteger msg) throws Exception {
          // 对每一个收到的数字计算累积然后发送给 client
          lastMultiplier = msg;
          factorial = factorial.multiply(msg);
          ctx.writeAndFlush(factorial);
      }

      @Override
      public void channelInactive(ChannelHandlerContext ctx) throws Exception {
          System.err.printf("Factorial of %,d is: %,d%n", lastMultiplier, factorial);
      }

      @Override
      public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
          cause.printStackTrace();
          ctx.close();
      }
}
