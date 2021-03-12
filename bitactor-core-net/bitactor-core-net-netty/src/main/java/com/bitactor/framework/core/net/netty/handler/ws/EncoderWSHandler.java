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

import com.bitactor.framework.core.net.api.ChannelBound;
import com.bitactor.framework.core.net.api.transport.message.MessageClose;
import com.bitactor.framework.core.net.api.transport.message.MessageWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

/**
 * MessageWrapper 转  WebSocketFrame
 *
 * @author WXH
 */
public class EncoderWSHandler extends ChannelOutboundHandlerAdapter {

    /**
     * 抽象化连接类，用于通知服务（NetServer 或 NettyClient）
     */
    private ChannelBound channelBound;

    public EncoderWSHandler(ChannelBound channelBound) {
        this.channelBound = channelBound;
    }

    private boolean checkIsBinary(MessageWrapper messageWrapper) {
        return !(messageWrapper instanceof MessageClose);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof MessageWrapper) {
            MessageWrapper wrapper = (MessageWrapper) msg;
            WebSocketFrame frame = null;
            if (checkIsBinary(wrapper)) {
                ByteBuf data = (ByteBuf) this.channelBound.getCodec().encode(wrapper);
                frame = new BinaryWebSocketFrame(data);
            } else {
                frame = new CloseWebSocketFrame();
            }
            ctx.channel().writeAndFlush(frame);
        } else {
            ctx.write(msg, promise);
        }
    }
}
