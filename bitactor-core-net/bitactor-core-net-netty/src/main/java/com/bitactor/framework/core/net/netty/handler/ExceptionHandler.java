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
import com.bitactor.framework.core.net.api.transport.message.MessageWrapper;
import com.bitactor.framework.core.exception.UnknownMsgException;
import com.bitactor.framework.core.logger.Logger;
import com.bitactor.framework.core.logger.LoggerFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 异常处理的handler
 *
 * @author WXH
 */
@ChannelHandler.Sharable
public class ExceptionHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);
    /**
     * 抽象化连接类，用于通知服务（NetServer 或 NettyClient）
     */
    private ChannelBound channelBound;

    public ExceptionHandler(ChannelBound channelBound) {
        this.channelBound = channelBound;
    }

    /**
     * 接收到客户端数据是
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof MessageWrapper) {
            super.channelRead(ctx, msg);
            return;
        }
        throw new UnknownMsgException("Unknown msg: " + msg);
    }

    /**
     * 异常时通知服务管理器，通知服务器执行相应动作并执行断开连接
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.debug(String.format("Channel [exceptionCaught] context %s", ctx));
        cause.printStackTrace();
        ctx.close();
    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

}
