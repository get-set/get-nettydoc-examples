package com.getset.nettyex.binaryfactorial;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.math.BigInteger;

/**
 * 将一个 Number 对象转换为二进制（格式为`F`后跟32bit表示长度的后缀），
 * 比如 42 会被转换为 { 'F', 0, 0, 0, 1, 42 }
 */
public class NumberEncoder extends MessageToByteEncoder<Number> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Number msg, ByteBuf out) throws Exception {
        // 方便起见，先转换为 BigInteger
        BigInteger v;
        if (msg instanceof BigInteger) {
            v = (BigInteger) msg;
        } else {
            v = new BigInteger(String.valueOf(msg));
        }

        // 将 BigInteger 转换为 byte 数组
        byte[] data = v.toByteArray();
        int dataLength = data.length;

        // 写入 ByteBuf
        out.writeByte((byte) 'F'); // magic number
        out.writeInt(dataLength);  // 长度
        out.writeBytes(data);      // byte 数组
    }
}
