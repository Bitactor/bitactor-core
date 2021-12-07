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
import com.bitactor.framework.core.eventloop.inf.IEventLoop;
import com.bitactor.framework.core.logger.Logger;
import com.bitactor.framework.core.logger.LoggerFactory;
import com.bitactor.framework.core.net.api.Channel;
import com.bitactor.framework.core.net.api.ChannelContext;
import com.bitactor.framework.core.net.api.ChannelManager;
import com.bitactor.framework.core.net.api.transport.message.*;
import com.bitactor.framework.core.net.netty.channel.AckNettyChannel;
import com.bitactor.framework.core.net.netty.channel.NettyChannelContext;
import com.bitactor.framework.core.net.netty.handler.HeartBeatSHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

/**
 * 基于netty4的io服务
 *
 * @author WXH
 */
public class NettyModeServer extends NettyBaseServer {
    private static final Logger logger = LoggerFactory.getLogger(NettyModeServer.class);


    public NettyModeServer(ChannelManager<ChannelFuture> channelManager, UrlProperties url) throws Throwable {
        super(channelManager, url);
    }

    @Override
    public void registerNotify(ChannelContext channelContext) {
        if (channelContext == null) {
            return;
        }
        NettyChannelContext nettyChannelContext = (NettyChannelContext) channelContext;
        // 连接数限制
        int accepts = getUrl().getParameter(NetConstants.ACCEPTS_KEY, NetConstants.DEFAULT_ACCEPTS);
        if (accepts > 0 && getChannels().size() >= accepts) {
            nettyChannelContext.getContext().close();
            return;
        }
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
        Channel<ChannelFuture> channel = this.channelManager.registerChannel(ackChannel.getChannelContext());
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

    private void complete(Channel<ChannelFuture> channel) {
        // 如果开启了消息接收线程池
        if (isOpenMsgReceiveEventLoop()) {
            channel.setAttrVal(NetConstants.MESSAGE_RECEIVE_EVENT_LOOP_KEY, getMsgEventLoopGroup().next());
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
        Channel<ChannelFuture> channel = this.getChannel(channelId);
        if (channel == null) {
            logger.error("Server receiveClose msg but can not find channel, channel id:" + channelId);
            return;
        }
        channel.justClose();
    }

    @Override
    public void receiveHeartbeat(String channelId, MessageHeartBeat heartBeat) {
        Channel<ChannelFuture> channel = this.getChannel(channelId);
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
        Channel<ChannelFuture> channel = this.getChannel(channelId);
        if (channel == null) {
            logger.error("Server receive msg but can not find channel, channel id:" + channelId);
            return;
        }
        if (isOpenMsgReceiveEventLoop()) {

            IEventLoop msgEventLoop = channel.getAttrVal(NetConstants.MESSAGE_RECEIVE_EVENT_LOOP_KEY, null);
            msgEventLoop.execute(() -> {
                channel.onReceived(message);
            });
        } else {
            // netty worker 线程池 执行
            channel.onReceived(message);
        }
    }

    @Override
    public void closeNotify(String channelId) {
        Channel<ChannelFuture> ackChannel = this.getAckChannels().remove(channelId);
        if (ackChannel != null) {
            logger.debug("Server ACK channel has closed channel id:" + channelId + " remote address : " + ackChannel.getRemoteAddress());
            return;
        }
        Channel<ChannelFuture> channel = this.removeChannel(channelId);
        if (channel == null) {
            return;
        }
        this.channelManager.destroyChannel(channelId);
        channel.onDestroy();
        logger.info("Server data channel has closed channel id:" + channelId + " remote address : " + channel.getRemoteAddress());
    }
}
