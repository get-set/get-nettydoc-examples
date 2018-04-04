package com.getset.nettyex.ws;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.ClientCookieEncoder;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.internal.SystemPropertyUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;

public class WebSocketClient {
    private final static boolean SSL = true;
    private final static String HOST = "localhost";
    private final static int PORT = SSL ? 8443 : 8088;
    private static final String URL = (SSL ? "wss" : "ws") + "://" + HOST + ":" + PORT + "/websocket";

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

        WebSocketClientHandler handler = new WebSocketClientHandler(WebSocketClientHandshakerFactory.newHandshaker(
                new URI(URL), WebSocketVersion.V13, null, true, new DefaultHttpHeaders()));

        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        if (SSL) {
                            socketChannel.pipeline().addLast(sslContext.newHandler(socketChannel.alloc(), HOST, PORT));
                        }
                        socketChannel.pipeline().addLast(
                                new HttpClientCodec(),
                                new HttpObjectAggregator(65536),
                                WebSocketClientCompressionHandler.INSTANCE,
                                handler);
                    }
                });

        try {
            // 启动 Client
            Channel ch = bootstrap.connect(HOST, PORT).sync().channel();
            // 等待 hand shake 完成
            handler.getHandshakeFuture().sync();

            // 通过控制台与服务端通讯
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
            String line;
            while ((line = input.readLine()) != null) {
                if ("bye".equals(line.toLowerCase())) {
                    ch.writeAndFlush(new CloseWebSocketFrame());
                    ch.closeFuture().sync();
                    break;
                } else if ("ping".equals(line.toLowerCase())) {
                    ch.writeAndFlush(new PingWebSocketFrame());
                } else {
                    ch.writeAndFlush(new TextWebSocketFrame(line));
                }
            }

            // 等待关闭
            ch.closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}
