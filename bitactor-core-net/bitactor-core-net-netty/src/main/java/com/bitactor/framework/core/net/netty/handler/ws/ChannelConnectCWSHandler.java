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


import com.bitactor.framework.core.constant.NetConstants;
import com.bitactor.framework.core.net.api.ChannelBound;
import com.bitactor.framework.core.logger.Logger;
import com.bitactor.framework.core.logger.LoggerFactory;
import com.bitactor.framework.core.net.netty.channel.NettyChannelContext;
import com.bitactor.framework.core.net.netty.handler.ChannelConnectHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;

import java.net.URI;
import java.net.URISyntaxException;

import static io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_COMPLETE;

/**
 * @author WXH
 */
public class ChannelConnectCWSHandler extends WebSocketClientProtocolHandler {
    private static final Logger logger = LoggerFactory.getLogger(ChannelConnectHandler.class);
    private ChannelBound channelBound;

    public ChannelConnectCWSHandler(ChannelBound channelBound) throws Exception {
        super(getHandshaker(channelBound));
        this.channelBound = channelBound;
    }

    private static WebSocketClientHandshaker getHandshaker(ChannelBound channelBound) throws URISyntaxException {
        return WebSocketClientHandshakerFactory
                .newHandshaker(getWSUri(channelBound), WebSocketVersion.V13, null, false, new DefaultHttpHeaders());
    }

    private static URI getWSUri(ChannelBound channelBound) throws URISyntaxException {
        String path = channelBound.getUrl().getParameter(NetConstants.WS_URL_PATH_KEY, NetConstants.DEFAULT_WS_URL_PATH);
        StringBuilder url = new StringBuilder();
        if (channelBound.getUrl().isOpenWsSsl()) {
            url.append("wss");
        } else {
            url.append("ws");
        }
        url.append("://").
                append(channelBound.getUrl().getHost()).
                append(":").
                append(channelBound.getUrl().getPort()).
                append(path);
        return new URI(url.toString());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt == HANDSHAKE_COMPLETE) {
            logger.info(String.format("Channel [channelActive] context %s", ctx));
            if (channelBound != null) {
                channelBound.registerNotify(new NettyChannelContext(ctx, channelBound.getCodec(), channelBound.getUrl()));
            }
            return;
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
        logger.debug(String.format("Channel [channelInactive] context %s", ctx));
        if (channelBound != null) {
            channelBound.closeNotify(ctx.channel().id().toString());
        }
    }
}
