# bitactor-core

[![JDK](https://img.shields.io/badge/JDK-1.8%2B-green.svg)](https://www.oracle.com/technetwork/java/javase/downloads/index.html)
  [![license](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0)

###介绍
  Bitactor Core 是一款Java 网络服务框架
### bitactor协议

#### 支持的协议

* TCP   :   常规的长连接协议
* KCP   :   [可靠的UDP协议](https://github.com/szhnet/kcp-netty)
* WS    :   Websocket

#### Bitactor协议格式

通用

| head     | packetType |
|:---------|:-----------|
| 2(short) | 1(byte)    |

MessageData 专有

| head     | packetType | protoType | msgId  | commandId | data    |
|:---------|:-----------|:----------|:-------|:----------|:--------|
| 2(short) | 1(byte)    | 1(byte)   | 4(int) | 4(int)    | n(byte) |

#### 协议名词解释

|  参数名   | 	名词 	|	解释                                                         |
|:---------|:---------|:----------------------------------------------------------------|
|head      | 包头      |指定包体长度，不包括head的长度。                                    |
|protoType | 包类型    | 包类型 协议包类型。                                               |
|protoType | 协议类型  | 指定序列化类型，支持Protobuf/Json。                                |
|msgId     | 消息号    | 单次请求响应消息号相同，每增加一次请求消息号+1,推送消息的msg固定为0。   |
|commandId | 协议解析id| 序列化类的简单类名的hashCode值,用于序列化&反序列化（也可自定义）       |
|data      | 数据内容  | Protobuf/Json的 序列化类的byte[]                                  |

#### bitactor协议包类型

基于packetType协议包体分为5种

| 包类名            | packetType值 | 描述    |
|:-----------------|:-------------|:--------|
| MessageHandShake | 0x01         | 握手消息 |
| MessageAck       | 0x02         | 确认消息 |
| MessageHeartBeat | 0x03         | 心跳消息 |
| MessageData      | 0x04         | 数据消息 |
| MessageClose     | 0x10         | 关闭消息 |
