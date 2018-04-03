package com.getset.nettyex.binaryfactorial;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;

import java.math.BigInteger;
import java.util.List;

public class BigIntegerDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        // 等F和表示长度的前缀都接收到
        if(in.readableBytes() < 5) {
            return;
        }

        in.markReaderIndex();

        // 检查 magic number
        int magicNumber = in.readUnsignedByte();
        if(magicNumber != 'F') {
            in.resetReaderIndex();
            throw new CorruptedFrameException("Invalid magic number: " + magicNumber);
        }

        // 等指定长度的字节都接收到
        int dataLength = in.readInt();
        if(in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }

        // 将收到的数据转换为 BigInteger
        byte[] decoded = new byte[dataLength];
        in.readBytes(decoded);

        out.add(new BigInteger(decoded));
    }
}
