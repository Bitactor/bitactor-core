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
import com.bitactor.framework.core.net.netty.handler.*;
import com.bitactor.framework.core.net.netty.starter.AbstractNettyServerStarter;
import com.bitactor.framework.core.net.api.ChannelBound;
import com.bitactor.framework.core.net.api.type.NetworkProtocol;
import com.bitactor.framework.core.logger.Logger;
import com.bitactor.framework.core.logger.LoggerFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;

/**
 * TCP协议启动器
 *
 * @author WXH
 */
public class TCPServerStarter extends AbstractNettyServerStarter<ServerChannel> {
    private static final Logger logger = LoggerFactory.getLogger(TCPServerStarter.class);

    public TCPServerStarter(ChannelBound channelBound) {
        super(channelBound, NetworkProtocol.TCP);
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
    protected Class<? extends ServerChannel> getChannelClass() {
        if (Epoll.isAvailable()) {
            return EpollServerSocketChannel.class;
        } else {
            return NioServerSocketChannel.class;
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
            bootstrap.channel(getChannelClass());//
            bootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                    nioSocketChannel.pipeline().addLast("DecoderHandler", new DecoderHandler(getChannelBound()));
                    nioSocketChannel.pipeline().addLast("EncoderHandler", new EncoderHandler(getChannelBound()));
                    nioSocketChannel.pipeline().addLast("IPLimitHandler", new IPLimitHandler(getChannelBound()));
                    nioSocketChannel.pipeline().addLast("ChannelConnectHandler", new ChannelConnectHandler(getChannelBound()));
                    nioSocketChannel.pipeline().addLast("MsgAckHandler", new MsgAckHandler(getChannelBound()));
                    nioSocketChannel.pipeline().addLast("MsgCloseHandler", new MsgCloseHandler(getChannelBound()));
                    nioSocketChannel.pipeline().addLast("MsgDataHandler", new MsgDataHandler(getChannelBound()));
                    nioSocketChannel.pipeline().addLast("ExceptionHandler", new ExceptionHandler(getChannelBound()));
                }

            });
            bootstrap.childOption(ChannelOption.TCP_NODELAY, Boolean.TRUE);
            bootstrap.childOption(ChannelOption.SO_REUSEADDR, Boolean.TRUE);
            bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            //绑定端口启动服务，并等待client连接
            setFuture(bootstrap.bind(port).sync());
            printStartLog();
            getChannelBound().startNotify();
            //直到服务器关闭才进行下一步
            getFuture().channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {

            getWorkerGroup().shutdownGracefully();
            getBossGroup().shutdownGracefully();
            //关闭通知
            getChannelBound().shutdownNotify();
        }
    }
}
