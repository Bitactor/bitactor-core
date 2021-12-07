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

package com.bitactor.framework.core.net.netty.client;

import com.alibaba.fastjson.JSON;
import com.bitactor.framework.core.config.UrlProperties;
import com.bitactor.framework.core.constant.NetConstants;
import com.bitactor.framework.core.logger.Logger;
import com.bitactor.framework.core.logger.LoggerFactory;
import com.bitactor.framework.core.net.api.Channel;
import com.bitactor.framework.core.net.api.ChannelContext;
import com.bitactor.framework.core.net.api.ChannelManager;
import com.bitactor.framework.core.net.api.transport.HandShakeData;
import com.bitactor.framework.core.net.api.transport.message.*;
import com.bitactor.framework.core.net.netty.channel.AckNettyChannel;
import com.bitactor.framework.core.net.netty.channel.NettyChannelContext;
import com.bitactor.framework.core.net.netty.codec.NettyDefaultCodec;
import com.bitactor.framework.core.net.netty.handler.HeartBeatCHandler;
import com.bitactor.framework.core.utils.assist.RandomUtil;
import io.jpower.kcp.netty.UkcpChannel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

/**
 * netty 的网络客户端
 *
 * @author WXH
 */
public class NettyModeClient extends NettyBaseClient {
    private static final Logger logger = LoggerFactory.getLogger(NettyModeClient.class);

    public NettyModeClient(ChannelManager channelManager, UrlProperties url) throws Throwable {
        super(channelManager, url);
    }

    @Override
    public void registerNotify(ChannelContext channelContext) {
        if (channelContext == null) {
            return;
        }
        AckNettyChannel ackNettyChannel = new AckNettyChannel((NettyChannelContext) channelContext);
        setAckChannel(ackNettyChannel);
        registerNetHandler(ackNettyChannel);
        getAckChannel().send(new MessageAck());
    }

    @Override
    public void receiveAck(String channelId, MessageAck ack) {

    }

    @Override
    public void receiveHandShack(String channelId, MessageHandShake handShake) {
        // 处理ACK channel 到正式连接的 channel 管理
        if (getAckChannel() == null) {
            logger.error("Client close client ack channel but not exist,ack channel id:" + channelId);
            return;
        }
        Channel<ChannelFuture> channel = this.channelManager.registerChannel(getAckChannel().getChannelContext());
        ChannelHandlerContext ctx = getAckChannel().getChannelContext().getContext();
        setAckChannel(null);
        if (channel == null) {
            logger.warn("Client receive msg but can not find channel, channel id:" + channelId);
            return;
        }
        this.addChannel(channel);
        // 处理握手消息
        logger.debug("Client receive handshake message :" + channelId + " message : " + handShake.getDataStr());
        HandShakeData handShakeData = JSON.parseObject(handShake.getData(), HandShakeData.class);
        setHandShakeData(handShakeData);

        initHeartbeat(ctx, handShakeData);
        logger.info("[hand shake data                                 ] " + new String(handShake.getData()));
        complete(channel);
        logger.info("[complete connect                                ] {} {}", channel.getChannelId(), channel.getRemoteAddress());
    }

    private void complete(Channel channel) {
        // 异步通知
        getCommonPool().execute(() -> {
            channel.onActivity();
            channelManager.activityChannel(channel);
        });
    }

    private void initHeartbeat(ChannelHandlerContext ctx, HandShakeData handShakeData) {
        boolean isOpenHeartbeat = handShakeData.getSystemParameterBool(NetConstants.HEARTBEAT_OPEN_KEY);
        setIsOpenHeartbeat(isOpenHeartbeat);
        // 心跳处理
        if (isOpenHeartbeat) {
            ctx.pipeline().addBefore("MsgAckHandler", "HeartBeatCHandler",
                    new HeartBeatCHandler(this, handShakeData.getSystemParameterLong(NetConstants.HEARTBEAT_PERIOD_KEY)));
        }
    }

    @Override
    public void receiveClose(String channelId, MessageClose close) {
        Channel<ChannelFuture> channel = this.getChannel();
        if (channel == null) {
            logger.error("Can not find channel by method receiveClose ,channel id:" + channelId);
            return;
        }
        channel.justClose();
        closeNotify(channel.getChannelId());
    }

    @Override
    public void receiveHeartbeat(String channelId, MessageHeartBeat heartBeat) {
        logger.debug("Client receive heartbeat channelId :" + channelId);
        long now = System.currentTimeMillis();
        if (getUrl().getParameter(NetConstants.LOGGER_DELAY_KEY, NetConstants.DEFAULT_LOGGER_DELAY)) {
            long last = NettyDefaultCodec.getBytesToLong(heartBeat.getData(), getByteOrder());
            logger.debug(getUrl().getGroupAndId() + " delay:" + (now - last) + "ms");
        }
    }

    /**
     * 根据协议类型发送不同的ack
     *
     * @return
     */
    private void registerNetHandler(AckNettyChannel ackChannel) {
        String netProtocol = getNetProtocol();
        if (NetConstants.DEFAULT_KCP.equals(netProtocol)) {
            UkcpChannel kcpCh = (UkcpChannel) ackChannel.getChannelContext().getContext().channel();
            kcpCh.conv(RandomUtil.getRandomInt(1000, 10000000));
        }
    }

    @Override
    public void receiveMessage(String channelId, MessageData message) {
        Channel<ChannelFuture> channel = this.getChannel();
        if (channel == null) {
            logger.error("Can not find channel by method receiveMessage ,channel id:" + channelId);
            return;
        }
        channel.onReceived(message);
    }

    @Override
    public void closeNotify(String channelId) {
        if (getAckChannel() != null && getAckChannel().getChannelId().equals(channelId)) {
            logger.info("Client ACK channel has closed channel id:" + channelId + " remote address : " + getAckChannel().getRemoteAddress());
            setAckChannel(null);
            return;
        }
        this.destroyChannel();
    }
}
