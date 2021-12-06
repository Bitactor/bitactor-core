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

package com.bitactor.framework.core.net.netty.server.starter;


import com.bitactor.framework.core.constant.NetConstants;
import com.bitactor.framework.core.logger.Logger;
import com.bitactor.framework.core.logger.LoggerFactory;
import com.bitactor.framework.core.net.api.ChannelBound;
import com.bitactor.framework.core.net.api.ChannelInit;
import com.bitactor.framework.core.net.api.type.NetworkProtocol;
import com.bitactor.framework.core.net.netty.channel.ChannelNettyOptions;
import com.bitactor.framework.core.net.netty.handler.*;
import com.bitactor.framework.core.net.netty.handler.ws.ChannelConnectSWSHandler;
import com.bitactor.framework.core.net.netty.handler.ws.DecoderWSHandler;
import com.bitactor.framework.core.net.netty.handler.ws.EncoderWSHandler;
import com.bitactor.framework.core.net.netty.starter.AbstractNettyServerStarter;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.Objects;

/**
 * WS协议启动器
 *
 * @author WXH
 */
public class WSServerStarter extends AbstractNettyServerStarter<ServerChannel> {
    private static final Logger logger = LoggerFactory.getLogger(WSServerStarter.class);

    public WSServerStarter(ChannelBound channelBound, ChannelInit<ChannelNettyOptions> channelInit) {
        super(channelBound, channelInit, NetworkProtocol.WS);
    }

    @Override
    public boolean isActive() {
        return getFuture() != null && getFuture().channel().isActive();
    }

    @Override
    protected void initBossGroup() {
        if (Epoll.isAvailable()) {
            setBossGroup(new EpollEventLoopGroup(1, new DefaultThreadFactory("NettyServerBoss", true)));
        } else {
            setBossGroup(new NioEventLoopGroup(1, new DefaultThreadFactory("NettyServerBoss", true)));
        }
    }

    @Override
    protected void initWorkGroup() {
        if (Epoll.isAvailable()) {
            setWorkerGroup(new EpollEventLoopGroup(getUrl().getPositiveParameter(NetConstants.IO_THREADS_KEY, NetConstants.DEFAULT_IO_THREADS),
                    new DefaultThreadFactory("NettyServerWorker", true)));
        } else {
            setWorkerGroup(new NioEventLoopGroup(getUrl().getPositiveParameter(NetConstants.IO_THREADS_KEY, NetConstants.DEFAULT_IO_THREADS),
                    new DefaultThreadFactory("NettyServerWorker", true)));
        }
    }

    @Override
    public void start() {
        initBossGroup();
        initWorkGroup();
        int port = getUrl().getPort();
        try {

            ServerBootstrap bootstrap = new ServerBootstrap(); //作为一个引导程序
            bootstrap.group(getBossGroup(), getWorkerGroup());//添加连接处理器组
            addChannelHandler(bootstrap);
            bootstrap.childOption(ChannelOption.TCP_NODELAY, Boolean.TRUE);
            bootstrap.childOption(ChannelOption.SO_REUSEADDR, Boolean.TRUE);
            bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            if (Objects.nonNull(channelInit)) {
                channelInit.init(bootstrap::childOption);
            }
            //绑定端口启动服务，并等待client连接
            setFuture(bootstrap.bind(port).sync());
            printStartLog();
            getChannelBound().startNotify();
            //直到服务器关闭才进行下一步
            getFuture().channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            getWorkerGroup().shutdownGracefully();
            getBossGroup().shutdownGracefully();
            //关闭通知
            getChannelBound().shutdownNotify();
        }
    }

    @Override
    protected void addChannelHandler(ServerBootstrap bootstrap) throws Exception {
        // SSL.
        final SslContext sslCtx;
        if (getUrl().isOpenWsSsl()) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } else {
            sslCtx = null;
        }
        if (Epoll.isAvailable()) {
            bootstrap.channel(EpollServerSocketChannel.class);
            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel nioSocketChannel) throws Exception {
                    addChannelPipeline(sslCtx, nioSocketChannel.pipeline(), nioSocketChannel.alloc());
                }

            });
        } else {
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel nioSocketChannel) throws Exception {
                    addChannelPipeline(sslCtx, nioSocketChannel.pipeline(), nioSocketChannel.alloc());
                }

            });
        }
    }

    private void addChannelPipeline(SslContext sslCtx, ChannelPipeline pipeline, ByteBufAllocator alloc) {
        if (Objects.nonNull(sslCtx)) {
            pipeline.addLast("SSL", sslCtx.newHandler(alloc));
        }
        pipeline.addLast("EncoderHandler", new EncoderWSHandler(getChannelBound()));
        // websocket 基于http协议，所以要有http编解码器
        pipeline.addLast("HttpServerCodec", new HttpServerCodec());
        // 对写大数据流的支持
        pipeline.addLast("ChunkedWriteHandler", new ChunkedWriteHandler());
        // 对httpMessage进行聚合，聚合成FullHttpRequest或FullHttpResponse
        int maxContentLength = getUrl().getParameter(NetConstants.BUFFER_KEY, NetConstants.DEFAULT_BUFFER_SIZE);
        pipeline.addLast("HttpObjectAggregator", new HttpObjectAggregator(maxContentLength));
        // ====================== 以上是用于支持http协议    ======================

        pipeline.addLast("ChannelConnectHandler", new ChannelConnectSWSHandler(getChannelBound()));
        pipeline.addLast("DecoderHandler", new DecoderWSHandler(getChannelBound()));
        pipeline.addLast("IPLimitHandler", new IPLimitHandler(getChannelBound()));
        pipeline.addLast("MsgAckHandler", new MsgAckHandler(getChannelBound()));
        pipeline.addLast("MsgCloseHandler", new MsgCloseHandler(getChannelBound()));
        pipeline.addLast("MsgDataWSHandler", new MsgDataHandler(getChannelBound()));
        pipeline.addLast("ExceptionHandler", new ExceptionHandler(getChannelBound()));
    }
}
