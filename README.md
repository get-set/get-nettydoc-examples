本项目例子来自[Netty官方文档](http://netty.io/wiki/index.html)。

版本v4.1

## 基础通讯
1. [Echo](./fundamental-echo) ‐ 一个非常基本的 server & client，与官方例子有点不同：client将控制台的输入内容发送给server。
2. [Discard](./fundamental-discard) - client 会持续发消息到 server， server 会丢弃所有的消息。学习 ChannelFutureListener 的使用。
3. [Uptime](./fundamental-uptime) - 实现了 client 的自动重连。

## 文本协议
1. [Telnet](./text-telnet) - 一个基本的基于字符行的应用。
2. [Quote](./text-udp-quote) - 一个基于UDP协议的广播应用。
3. [SecureChat](./text-securechat) - 一个基于TLS的聊天服务器。

## 二进制协议
1. [ObjectEcho](./binary-echo) - 一个传输序列化对象的 Echo server。
2. [Factorial](./binary-factorial) - 一对有状态的 server 和 client， 另外自定义了传输 BigInteger 对象的编解码器。
