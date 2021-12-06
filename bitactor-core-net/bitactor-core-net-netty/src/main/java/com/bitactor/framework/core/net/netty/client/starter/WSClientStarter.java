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
import com.bitactor.framework.core.net.netty.handler.ws.ChannelConnectCWSHandler;
import com.bitactor.framework.core.net.netty.handler.ws.DecoderWSHandler;
import com.bitactor.framework.core.net.netty.handler.ws.EncoderWSHandler;
import com.bitactor.framework.core.net.netty.starter.AbstractNettyClientStarter;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.Objects;

/**
 * WS协议启动器
 *
 * @author WXH
 */
public class WSClientStarter extends AbstractNettyClientStarter<NioSocketChannel> {
    private static final Logger logger = LoggerFactory.getLogger(WSClientStarter.class);

    public WSClientStarter(ChannelBound channelBound, ChannelInit<ChannelNettyOptions> channelInit) {
        super(channelBound, channelInit, NetworkProtocol.WS);
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
            // SSL.
            final SslContext sslCtx;
            if (getUrl().isOpenWsSsl()) {
                sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
            } else {
                sslCtx = null;
            }
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(getWorkerGroup())
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, getUrl().getPositiveParameter(NetConstants.TIMEOUT_KEY, NetConstants.DEFAULT_TIMEOUT))
                    .channel(getChannelClass());
            bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel socketChannel) throws Exception {
                    if (Objects.nonNull(sslCtx)) {
                        socketChannel.pipeline().addLast("SSL", sslCtx.newHandler(socketChannel.alloc()));
                    }
                    socketChannel.pipeline().addLast("EncoderHandler", new EncoderWSHandler(getChannelBound()));
                    // websocket 基于http协议，所以要有http编解码器
                    socketChannel.pipeline().addLast("HttpClientCodec", new HttpClientCodec());
                    // 对写大数据流的支持
                    socketChannel.pipeline().addLast("ChunkedWriteHandler", new ChunkedWriteHandler());
                    // 对httpMessage进行聚合，聚合成FullHttpRequest或FullHttpResponse
                    int maxContentLength = getUrl().getParameter(NetConstants.BUFFER_KEY, NetConstants.DEFAULT_BUFFER_SIZE);
                    socketChannel.pipeline().addLast("HttpObjectAggregator", new HttpObjectAggregator(maxContentLength));
                    // ====================== 以上是用于支持http协议    ======================

                    socketChannel.pipeline().addLast("ChannelConnectHandler", new ChannelConnectCWSHandler(getChannelBound()));
                    socketChannel.pipeline().addLast("DecoderHandler", new DecoderWSHandler(getChannelBound()));
                    socketChannel.pipeline().addLast("MsgAckHandler", new MsgAckHandler(getChannelBound()));
                    socketChannel.pipeline().addLast("MsgHandshakeHandler", new MsgHandshakeHandler(getChannelBound()));
                    socketChannel.pipeline().addLast("MsgCloseHandler", new MsgCloseHandler(getChannelBound()));
                    socketChannel.pipeline().addLast("MsgDataWSHandler", new MsgDataHandler(getChannelBound()));
                    socketChannel.pipeline().addLast("ExceptionHandler", new ExceptionHandler(getChannelBound()));
                }

            });
            if (Objects.nonNull(channelInit)) {
                channelInit.init(bootstrap::option);
            }
            setFuture(bootstrap.connect(getUrl().getHost(), getUrl().getPort()).sync());
            printStartLog();
            getChannelBound().startNotify();
            getFuture().channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            getWorkerGroup().shutdownGracefully();
            getChannelBound().shutdownNotify();
        }
    }
}
