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

package com.bitactor.framework.core.net.netty.handler;

import com.bitactor.framework.core.net.api.ChannelBound;
import com.bitactor.framework.core.net.api.transport.message.MessageHeartBeat;
import com.bitactor.framework.core.logger.Logger;
import com.bitactor.framework.core.logger.LoggerFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * @author WXH
 */
@ChannelHandler.Sharable
public class HeartBeatSHandler extends IdleStateHandler {
    private static final Logger logger = LoggerFactory.getLogger(HeartBeatSHandler.class);
    /**
     * 抽象化连接类，用于通知服务（NetServer 或 NettyClient）
     */
    private ChannelBound channelBound;

    public HeartBeatSHandler(ChannelBound channelBound, long readTimeout) {
        super(readTimeout, 0, 0, TimeUnit.MILLISECONDS);
        this.channelBound = channelBound;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof MessageHeartBeat) {
            channelBound.receiveHeartbeat(ctx.channel().id().toString(), (MessageHeartBeat) msg);
        }
        super.channelRead(ctx, msg);
    }

    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        if (evt.state() == IdleState.READER_IDLE) {
            Channel channel = ctx.channel();
            logger.info("heartbeat timeout will close connection id: " + channel.id().toString() + " remoteAddress" + channel.remoteAddress());
            if (channelBound != null) {
                channelBound.closeNotify(ctx.channel().id().toString());
            }
            ctx.close();
        }
        super.channelIdle(ctx, evt);
    }

}
