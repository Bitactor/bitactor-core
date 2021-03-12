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

package com.bitactor.framework.core.net.netty.server;


import com.bitactor.framework.core.config.UrlProperties;
import com.bitactor.framework.core.constant.NetConstants;
import com.bitactor.framework.core.net.netty.channel.AckNettyChannel;
import com.bitactor.framework.core.net.netty.channel.NettyChannelContext;
import com.bitactor.framework.core.net.api.Channel;
import com.bitactor.framework.core.net.api.ChannelContext;
import com.bitactor.framework.core.net.api.ChannelManager;
import com.bitactor.framework.core.net.api.transport.message.*;
import com.bitactor.framework.core.logger.Logger;
import com.bitactor.framework.core.logger.LoggerFactory;
import com.bitactor.framework.core.net.netty.handler.HeartBeatSHandler;
import com.bitactor.framework.core.threadpool.AtomicOrderedExecutorQueue;
import io.netty.channel.ChannelHandlerContext;

/**
 * 基于netty4的io服务
 *
 * @author WXH
 */
public class NettyModeServer extends NettyBaseServer {
    private static final Logger logger = LoggerFactory.getLogger(NettyModeServer.class);


    public NettyModeServer(ChannelManager channelManager, UrlProperties url) throws Throwable {
        super(channelManager, url);
    }

    @Override
    public void registerNotify(ChannelContext channelContext) {
        if (channelContext == null) {
            return;
        }
        NettyChannelContext nettyChannelContext = (NettyChannelContext) channelContext;
        AckNettyChannel ackChannel = new AckNettyChannel(nettyChannelContext);
        ackChannel.setAttrVal(NetConstants.ACK_KEY, System.currentTimeMillis());
        getAckChannels().put(ackChannel.getChannelId(), ackChannel);
    }

    @Override
    public void receiveAck(String channelId, MessageAck ack) {
        AckNettyChannel ackChannel = (AckNettyChannel) getAckChannels().remove(channelId);
        if (ackChannel == null) {
            logger.error("Server close server ack channel but not exist,ack channel id:" + channelId);
            return;
        }
        Channel channel = this.channelManager.registerChannel(ackChannel.getChannelContext());
        if (channel == null) {
            logger.error("Server close server will register channel,by channelManager return null");
            return;
        }
        this.addChannel(channel);
        channel.send(new MessageHandShake(this.getHandShake()));
        complete(channel);
        logger.info("Server new channel has connect complete channel id:" + channelId + " remote address : " + channel.getRemoteAddress());
        ChannelHandlerContext ctx = ackChannel.getChannelContext().getContext();
        initHeartbeat(ctx);
    }

    private void initHeartbeat(ChannelHandlerContext ctx) {
        // 是否开启心跳
        if (isOpenHeartbeat()) {
            long readTimeout = getUrl().getParameter(NetConstants.HEARTBEAT_TIMEOUT_KEY, NetConstants.DEFAULT_HEARTBEAT_TIMEOUT);
            ctx.channel().pipeline().addBefore("MsgAckHandler", "HeartBeatSHandler", new HeartBeatSHandler(this, readTimeout));
        }

    }

    private void complete(Channel channel) {
        // 乳沟开启了消息接收线程池
        if (isOpenMsgReceiveThreadPool() && isOpenMsgReceiveOrderedQueue()) {
            channel.setAttrVal(NetConstants.MESSAGE_RECEIVE_QUEUE_KEY, new AtomicOrderedExecutorQueue(this.getMessageThreadPool()));
        }
        getCommonPool().execute(() -> {
            channel.onActivity();
            channelManager.activityChannel(channel);
        });
    }

    @Override
    public void receiveHandShack(String channelId, MessageHandShake handShake) {
    }

    @Override
    public void receiveClose(String channelId, MessageClose close) {
        Channel channel = this.getChannel(channelId);
        if (channel == null) {
            logger.error("Server receiveClose msg but can not find channel, channel id:" + channelId);
            return;
        }
        channel.justClose();
    }

    @Override
    public void receiveHeartbeat(String channelId, MessageHeartBeat heartBeat) {
        Channel channel = this.getChannel(channelId);
        if (channel == null) {
            logger.error("Server receiveHeartbeat msg but can not find channel, channel id:" + channelId);
            return;
        }
        logger.debug("Server " + getServerDefinition() + " receive heartbeat channelId :" + channelId);
        channel.setAttrVal(NetConstants.HEARTBEAT_KEY, System.currentTimeMillis());
        channel.send(heartBeat);
    }

    @Override
    public void receiveMessage(String channelId, MessageData message) {
        Channel channel = this.getChannel(channelId);
        if (channel == null) {
            logger.error("Server receive msg but can not find channel, channel id:" + channelId);
            return;
        }
        if (isOpenMsgReceiveThreadPool()) {
            Runnable runnable = () -> {
                channel.onReceived(message);
            };
            // 有序消息队列，channel 有序
            if (isOpenMsgReceiveOrderedQueue()) {
                AtomicOrderedExecutorQueue orderedExecutorQueue = channel.getAttrVal(NetConstants.MESSAGE_RECEIVE_QUEUE_KEY, null);
                orderedExecutorQueue.add(runnable);
            } else {
                // 无序，消息队列，快速执行
                getMessageThreadPool().execute(runnable);
            }

        } else {
            // netty worker 线程池 执行
            channel.onReceived(message);
        }
    }

    @Override
    public void closeNotify(String channelId) {
        Channel ackChannel = this.getAckChannels().remove(channelId);
        if (ackChannel != null) {
            logger.debug("Server ACK channel has closed channel id:" + channelId + " remote address : " + ackChannel.getRemoteAddress());
            return;
        }
        Channel channel = this.removeChannel(channelId);
        if (channel == null) {
            return;
        }
        this.channelManager.destroyChannel(channelId);
        channel.onDestroy();
        logger.debug("Server data channel has closed channel id:" + channelId + " remote address : " + channel.getRemoteAddress());
    }
}
