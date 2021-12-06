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
import io.jpower.kcp.netty.ChannelOptionHelper;
import io.jpower.kcp.netty.UkcpChannel;
import io.jpower.kcp.netty.UkcpChannelOption;
import io.jpower.kcp.netty.UkcpClientChannel;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.Objects;

/**
 * TCP协议启动器
 *
 * @author WXH
 */
public class KCPClientStarter extends AbstractNettyClientStarter<UkcpClientChannel> {
    private static final Logger logger = LoggerFactory.getLogger(KCPClientStarter.class);

    public KCPClientStarter(ChannelBound channelBound, ChannelInit<ChannelNettyOptions> channelInit) {
        super(channelBound, channelInit, NetworkProtocol.KCP);
    }

    @Override
    protected void initBossGroup() {
        // do nothing;
    }

    @Override
    protected void initWorkGroup() {
        setWorkerGroup(new NioEventLoopGroup(getUrl().getPositiveParameter(NetConstants.IO_THREADS_KEY, NetConstants.DEFAULT_IO_THREADS),
                new DefaultThreadFactory("Netty-Client-Worker", false)));
    }

    @Override
    protected Class<? extends UkcpClientChannel> getChannelClass() {
        return UkcpClientChannel.class;
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
                    .channel(getChannelClass())
                    .handler(new ChannelInitializer<UkcpChannel>() {
                        @Override
                        public void initChannel(UkcpChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast("DecoderHandler", new DecoderHandler(getChannelBound()));
                            p.addLast("EncoderHandler", new EncoderHandler(getChannelBound()));
                            p.addLast("ChannelConnectHandler", new ChannelConnectHandler(getChannelBound()));
                            p.addLast("MsgAckHandler", new MsgAckHandler(getChannelBound()));
                            p.addLast("MsgHandshakeHandler", new MsgHandshakeHandler(getChannelBound()));
                            p.addLast("MsgCloseHandler", new MsgCloseHandler(getChannelBound()));
                            p.addLast("MsgDataKCPHandler", new MsgDataHandler(getChannelBound()));
                            p.addLast("ExceptionHandler", new ExceptionHandler(getChannelBound()));
                        }
                    });

            ChannelOptionHelper.nodelay(bootstrap
                    , getUrl().getParameter(NetConstants.KCP_NODELAY_KEY, NetConstants.DEFAULT_KCP_KCPNODELAY)
                    , getUrl().getParameter(NetConstants.KCP_INTERVAL_KEY, NetConstants.DEFAULT_KCP_INTERVAL)
                    , getUrl().getParameter(NetConstants.KCP_FASTRESEND_KEY, NetConstants.DEFAULT_KCP_FASTRESEND)
                    , getUrl().getParameter(NetConstants.KCP_NOCWND_KEY, NetConstants.DEFAULT_KCP_NOCWND))
                    .option(UkcpChannelOption.UKCP_MTU, getUrl().getParameter(NetConstants.KCP_MTU_KEY, NetConstants.DEFAULT_KCP_MTU))
                    .option(UkcpChannelOption.UKCP_SND_WND, getUrl().getParameter(NetConstants.KCP_SND_WND, NetConstants.DEFAULT_KCP_SND_WND))
                    .option(UkcpChannelOption.UKCP_RCV_WND, getUrl().getParameter(NetConstants.KCP_RCV_WND, NetConstants.DEFAULT_KCP_RCV_WND))
                    .option(UkcpChannelOption.UKCP_AUTO_SET_CONV, true);
            if (Objects.nonNull(channelInit)) {
                channelInit.init(bootstrap::option);
            }
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
