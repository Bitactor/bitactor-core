/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bitactor.framework.core.constant;

/**
 * @author WXH
 */
public class NetConstants {
    public static final String DEFAULT_PROTOCOL = "bitactor";
    // byte length
    public static final int BYTES_1_LENGTH = 1;
    public static final int BYTES_2_LENGTH = 2;
    public static final int BYTES_4_LENGTH = 4;
    public static final int BYTES_8_LENGTH = 8;

    // 网络协议
    public static final String DEFAULT_TCP = "TCP";
    public static final String DEFAULT_UDP = "UDP";
    public static final String DEFAULT_WS = "WS";
    public static final String DEFAULT_KCP = "KCP";

    // 编解码
    public static final String CODEC_KEY = "codec";
    public static final String DEFAULT_REMOTING_CODEC = "com.bitactor.framework.core.net.netty.codec.NettyDefaultCodec";

    // 超时时间
    public static final String TIMEOUT_KEY = "timeout";
    public static final int DEFAULT_TIMEOUT = 10000;

    // 打印协议
    public static final String PRINT_PROTO_KEY = "print.proto";
    public static final boolean DEFAULT_PRINT_PROTO = false;

    // 心跳
    public static final String HEARTBEAT_KEY = "heartbeat";
    public static final String HEARTBEAT_OPEN_KEY = "heartbeat.open";
    public static final Boolean DEFAULT_HEARTBEAT_OPEN = Boolean.TRUE;
    public static final String HEARTBEAT_PERIOD_KEY = "heartbeat.period";
    public static final long DEFAULT_HEARTBEAT_PERIOD = 10000L;
    public static final String HEARTBEAT_TIMEOUT_KEY = "heartbeat.timeout";
    public static final int DEFAULT_HEARTBEAT_TIMEOUT = 60000;

    // ACK
    public static final String ACK_PERIOD_KEY = "ack.period";
    public static final long DEFAULT_ACK_PERIOD = 5000L;
    public static final String ACK_KEY = "ack";
    public static final String CONNECT_TIME = "connect_time";
    public static final String ACK_TIMEOUT_KEY = "ack.timeout";
    public static final long DEFAULT_ACK_TIMEOUT = 10000L;

    //byte 大小端模式
    public static final String BYTE_ODER_BIG_ENDIAN_KEY = "byte.oder";
    public static final boolean DEFAULT_BYTE_ODER_BIG = true;

    // ip 限制
    public static final String IP_LIMIT_NUM = "ip.limit.num";

    // 线程相关
    public static final String IO_THREADS_KEY = "io.threads";
    public static final int DEFAULT_IO_THREADS = Math.min(2 * Runtime.getRuntime().availableProcessors() + 1, 32);
    public static final int DEFAULT_THREADS = Math.min(Runtime.getRuntime().availableProcessors() + 1, 16);
    public static final String DEFAULT_THREAD_POOL = "fixed";
    public static final String THREAD_POOL_KEY = "thread.pool";
    public static final String THREAD_POOL_CACHED = "cached";
    public static final String THREAD_POOL_FIXED = "fixed";
    public static final String THREADS_KEY = "threads";
    public static final String QUEUES_KEY = "queues";
    public static final String MESSAGE_RECEIVE_QUEUE_KEY = "message.receive.queue.key";

    // -1 代表不限制
    public static final int DEFAULT_QUEUES = -1;
    // 网络参数
    public static final String ACCEPTS_KEY = "accepts";
    public static final String PAYLOAD_KEY = "payload";
    // 内存
    public static final String BUFFER_KEY = "buffer";
    // default buffer size is 8k.
    public static final int DEFAULT_BUFFER_SIZE = 8 * 1024;
    public static final int MAX_BUFFER_SIZE = 16 * 1024;
    public static final int MIN_BUFFER_SIZE = 1 * 1024;

    // 协议包头
    public static final String MSG_HEADER_KEY = " msgheader";
    // default msg header size is 2 byte.
    public static final int DEFAULT_MSG_HEADER_SIZE = BYTES_2_LENGTH;
    public static final int MAX_MSG_HEADER_SIZE = BYTES_4_LENGTH;
    public static final int MIN_MSG_HEADER_SIZE = BYTES_1_LENGTH;

    // 业务线程相关
    public static final String MESSAGE_RECEIVE_THREAD_NAME = "Message-Business";
    public static final String MSG_RECEIVE_THREAD_POOL_OPEN_KEY = "msg.receive.thread.pool.open";
    public static final String MSG_RECEIVE_ORDERED_QUEUE_OPEN_KEY = "msg.receive.ordered.queue.open";
    public static final boolean DEFAULT_MSG_RECEIVE_THREAD_OPEN = false;
    public static final boolean DEFAULT_MSG_RECEIVE_ORDERED_QUEUE_OPEN = false;

    // 基础网络协议类型
    public static final String NET_PROTOCOL_KEY = "net.protocol";
    public static final String DEFAULT_NET_PROTOCOL = DEFAULT_TCP;

    // 自定义编码解码协议
    public static final String PROTOCOL_MODE_KEY = "protocol.mode";
    // 协议模式
    public static final String DEFAULT_PROTOCOL_MODE = NetConstants.DEFAULT_PROTOCOL_MODE_BITACTOR;
    public static final String DEFAULT_PROTOCOL_MODE_BITACTOR = "BITACTOR";


    // 握手数据绑定器键
    public static final String HAND_SHAKE_DATA_BOUND_CLASS_KEY = "hand.shake.data.bound.class";

    // 是否打印延迟
    public static final String LOGGER_DELAY_KEY = "logger.delay";
    public static final Boolean DEFAULT_LOGGER_DELAY = false;


    public static final String CONTROLLER_COMMAND_ID_KEY = "controller.command.id";
    /***********************************************KCP使用*****************************************************/

    // kcp  会话编号 key
    public static final String KCP_CONV_KEY = "kcp.conv";
    // kcp  会话编号 默认值
    public static final int DEFAULT_KCP_CONV = 10;

    // kcp  最大传输单元 key
    public static final String KCP_MTU_KEY = "kcp.mtu";
    // kcp  最大传输单元 默认值
    public static final int DEFAULT_KCP_MTU = 470;
    // kcp  最大发送窗口 key
    public static final String KCP_SND_WND = "kcp.sndwnd";
    // kcp  最大发送窗口 默认值
    public static final int DEFAULT_KCP_SND_WND = 256;
    // kcp  最大接收窗口 key
    public static final String KCP_RCV_WND = "kcp.rcvwnd";
    // kcp  最大接收窗口 默认值
    public static final int DEFAULT_KCP_RCV_WND = 256;

    // kcp  是否启动无延迟模式。无延迟模式rtomin将设置为0，拥塞控制不启动 key
    public static final String KCP_NODELAY_KEY = "kcp.nodelay";
    // kcp  是否启动无延迟模式。无延迟模式rtomin将设置为0，拥塞控制不启动 默认值
    public static final boolean DEFAULT_KCP_KCPNODELAY = true;

    // kcp  取消拥塞控制 key
    public static final String KCP_NOCWND_KEY = "kcp.nocwnd";
    // kcp  取消拥塞控制 默认值
    public static final boolean DEFAULT_KCP_NOCWND = true;

    // kcp  内部flush刷新间隔，对系统循环效率有非常重要影响 key
    public static final String KCP_INTERVAL_KEY = "kcp.interval";
    // kcp  内部flush刷新间隔，对系统循环效率有非常重要影响 默认值
    public static final int DEFAULT_KCP_INTERVAL = 10;

    // kcp  触发快速重传的重复ACK个数 key
    public static final String KCP_FASTRESEND_KEY = "kcp.fastresend";
    // kcp  触发快速重传的重复ACK个数 默认值
    public static final int DEFAULT_KCP_FASTRESEND = 2;

    /***********************************************WS使用*****************************************************/
    // ws 的路径
    public static final String WS_URL_PATH_KEY = "ws.path";
    // ws 的默认路径
    public static final String DEFAULT_WS_URL_PATH = "/ws";
    // ws 的路径
    public static final String WS_OPEN_SSL = "ws.ssl";
    // ws 的默认路径
    public static final boolean DEFAULT_WS_OPEN_SSL = false;
}
