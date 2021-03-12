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

package com.bitactor.framework.core.net.api;


import com.bitactor.framework.core.config.UrlProperties;
import com.bitactor.framework.core.net.api.transport.message.*;

import java.nio.ByteOrder;

/**
 * 通道绑定器
 *
 * @author WXH
 */
public interface ChannelBound {
    /**
     * 获取解码编码器
     */
    Codec getCodec();

    UrlProperties getUrl();

    /**
     * 网络成功连接注册通知
     *
     * @param channelContext
     */
    void registerNotify(ChannelContext channelContext);

    /**
     * 接收到ACK
     *
     * @param channelId
     * @param ack
     */
    void receiveAck(String channelId, MessageAck ack);

    /**
     * 接收到握手消息，一般只用在client 端处理
     *
     * @param channelId
     * @param handShake
     */
    void receiveHandShack(String channelId, MessageHandShake handShake);

    /**
     * 远程端主动发送的收到关闭消息
     *
     * @param channelId
     * @param close
     */
    void receiveClose(String channelId, MessageClose close);

    /**
     * 收到心跳消息
     *
     * @param channelId
     * @param heartBeat
     */
    void receiveHeartbeat(String channelId, MessageHeartBeat heartBeat);

    /**
     * 接收网络消息通知
     *
     * @param channelId
     * @param message
     */
    void receiveMessage(String channelId, MessageData message);
    /**
     * 网络关闭通知
     *
     * @param channelId
     */
    void closeNotify(String channelId);


    /**
     * 服务关闭通知
     */
    void shutdownNotify();

    /**
     * 服务启动通知
     */
    void startNotify();

    /**
     * 获取大小端模式
     *
     * @return
     */
    ByteOrder getByteOrder();

    boolean isOpenHeartbeat();
}
