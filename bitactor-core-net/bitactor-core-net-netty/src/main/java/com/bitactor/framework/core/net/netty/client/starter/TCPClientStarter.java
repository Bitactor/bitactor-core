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

package com.bitactor.framework.core.net.netty.client.starter;

import com.bitactor.framework.core.constant.NetConstants;
import com.bitactor.framework.core.logger.Logger;
import com.bitactor.framework.core.logger.LoggerFactory;
import com.bitactor.framework.core.net.api.ChannelBound;
import com.bitactor.framework.core.net.api.ChannelInit;
import com.bitactor.framework.core.net.api.type.NetworkProtocol;
import com.bitactor.framework.core.net.netty.channel.ChannelNettyOptions;
import com.bitactor.framework.core.net.netty.handler.*;
import com.bitactor.framework.core.net.netty.starter.AbstractNettyClientStarter;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;

/**
 * TCP协议启动器
 *
 * @author WXH
 */
public class TCPClientStarter extends AbstractNettyClientStarter<NioSocketChannel> {
    private static final Logger logger = LoggerFactory.getLogger(TCPClientStarter.class);

    public TCPClientStarter(ChannelBound channelBound, ChannelInit<ChannelNettyOptions> channelInit) {
        super(channelBound, channelInit, NetworkProtocol.TCP);
    }

    @Override
    protected void initBossGroup() {
        // do nothing
    }

    @Override
    protected void initWorkGroup() {
        setWorkerGroup(new NioEventLoopGroup(getUrl().getPositiveParameter(NetConstants.IO_THREADS_KEY, NetConstants.DEFAULT_IO_THREADS),
                new DefaultThreadFactory("Netty-Client-Worker", false)));
    }

    @Override
    protected Class<? extends NioSocketChannel> getChannelClass() {
        return NioSocketChannel.class;
    }

    @Override
    public boolean isActive() {
        return getFuture() != null && getFuture().isSuccess();
    }


    @Override
    public void start() {
        initWorkGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(getWorkerGroup())
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, getUrl().getPositiveParameter(NetConstants.TIMEOUT_KEY, NetConstants.DEFAULT_TIMEOUT))
                    .channel(getChannelClass());
            bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                    nioSocketChannel.pipeline().addLast("DecoderHandler", new DecoderHandler(getChannelBound()));
                    nioSocketChannel.pipeline().addLast("EncoderHandler", new EncoderHandler(getChannelBound()));
                    nioSocketChannel.pipeline().addLast("ChannelConnectHandler", new ChannelConnectHandler(getChannelBound()));
                    nioSocketChannel.pipeline().addLast("MsgAckHandler", new MsgAckHandler(getChannelBound()));
                    nioSocketChannel.pipeline().addLast("MsgHandshakeHandler", new MsgHandshakeHandler(getChannelBound()));
                    nioSocketChannel.pipeline().addLast("MsgCloseHandler", new MsgCloseHandler(getChannelBound()));
                    nioSocketChannel.pipeline().addLast("MsgDataTCPHandler", new MsgDataHandler(getChannelBound()));
                    nioSocketChannel.pipeline().addLast("ExceptionHandler", new ExceptionHandler(getChannelBound()));
                }

            });
            channelOptionInit(bootstrap);
            setFuture(bootstrap.connect(getUrl().getHost(), getUrl().getPort()).sync());
            printStartLog();
            getChannelBound().startNotify();
            getFuture().channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            getWorkerGroup().shutdownGracefully();
            getChannelBound().shutdownNotify();
        }
    }
}
