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

package com.bitactor.framework.core.net.netty.handler.ws;


import com.bitactor.framework.core.net.netty.handler.ChannelConnectHandler;
import com.bitactor.framework.core.net.api.ChannelBound;
import com.bitactor.framework.core.logger.Logger;
import com.bitactor.framework.core.logger.LoggerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

/**
 * @author WXH
 */
public class DecoderWSHandler extends SimpleChannelInboundHandler<BinaryWebSocketFrame> {
    private static final Logger logger = LoggerFactory.getLogger(ChannelConnectHandler.class);
    private ChannelBound channelBound;

    public DecoderWSHandler(ChannelBound channelBound) {
        this.channelBound = channelBound;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            return;
        }
        super.channelRead(ctx, msg);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BinaryWebSocketFrame msg) throws Exception {
        ByteBuf content = msg.content();
        Object object = this.channelBound.getCodec().decode(content);
        ctx.fireChannelRead(object);
    }
}
