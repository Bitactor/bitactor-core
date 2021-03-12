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

import com.bitactor.framework.core.net.netty.channel.NettyChannelContext;
import com.bitactor.framework.core.net.api.ChannelBound;
import com.bitactor.framework.core.logger.Logger;
import com.bitactor.framework.core.logger.LoggerFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 拦截消息处理器
 *
 * @author WXH
 */
@ChannelHandler.Sharable
public class ChannelConnectHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ChannelConnectHandler.class);
    /**
     * 抽象化连接类，用于通知服务（NetServer 或 NettyClient）
     */
    private ChannelBound channelBound;

    public ChannelConnectHandler(ChannelBound channelBound) {
        this.channelBound = channelBound;
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.debug(String.format("Channel [channelActive] context %s", ctx));
        if (channelBound != null) {
            channelBound.registerNotify(new NettyChannelContext(ctx, channelBound.getCodec(), channelBound.getUrl()));
        }
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.debug(String.format("Channel [channelInactive] context %s", ctx));
        if (channelBound != null) {
            channelBound.closeNotify(ctx.channel().id().toString());
        }
        super.channelInactive(ctx);
    }
}
