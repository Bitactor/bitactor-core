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

package com.bitactor.framework.core.net.netty.server;

import com.alibaba.fastjson.JSON;
import com.bitactor.framework.core.config.UrlProperties;
import com.bitactor.framework.core.constant.NetConstants;
import com.bitactor.framework.core.constant.RPCConstants;
import com.bitactor.framework.core.eventloop.BitactorEventLoopGroup;
import com.bitactor.framework.core.exception.NotSupportException;
import com.bitactor.framework.core.logger.Logger;
import com.bitactor.framework.core.logger.LoggerFactory;
import com.bitactor.framework.core.net.api.*;
import com.bitactor.framework.core.net.api.suport.SystemHandShakeDataBound;
import com.bitactor.framework.core.net.api.transport.AbstractServer;
import com.bitactor.framework.core.net.api.transport.HandShakeData;
import com.bitactor.framework.core.net.netty.server.starter.KCPServerStarter;
import com.bitactor.framework.core.net.netty.server.starter.TCPServerStarter;
import com.bitactor.framework.core.net.netty.server.starter.WSServerStarter;
import com.bitactor.framework.core.threadpool.NamedThreadFactory;
import com.bitactor.framework.core.utils.collection.CollectionUtils;

import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于netty4的io服务
 *
 * @author WXH
 */
public abstract class NettyBaseServer extends AbstractServer {
    private static final Logger logger = LoggerFactory.getLogger(NettyBaseServer.class);
    /**
     * 等待确认的通道集合;
     */
    private ConcurrentHashMap<String, Channel> ackChannels = new ConcurrentHashMap<String, Channel>();
    /**
     * 握手消息
     */
    private byte[] HandShake;
    /**
     * 是否绑定启动端口
     */
    private boolean bound = false;

    private Starter starter;


    private Codec codec;

    private ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;

    private BitactorEventLoopGroup msgEventLoopGroup = null;

    public void setBound(boolean bound) {
        this.bound = bound;
    }

    public boolean isBound() {
        return bound;
    }

    protected byte[] getHandShake() {
        return HandShake;
    }

    protected ConcurrentHashMap<String, Channel> getAckChannels() {
        return ackChannels;
    }

    public InetSocketAddress getLocalAddress() {
        return getUrl().toInetSocketAddress();
    }

    public BitactorEventLoopGroup getMsgEventLoopGroup() {
        return msgEventLoopGroup;
    }

    @Override
    public ByteOrder getByteOrder() {
        return byteOrder;
    }

    public NettyBaseServer(ChannelManager channelManager, UrlProperties url) throws Throwable {
        super(channelManager, url);
        this.initializer();
    }

    private void initializer() throws Throwable {

        initByteOder();

        initializerStart();

        initializerCodec();

        initializerHandShakeData();

        initializerAckTimer();

        initializerThreadPool();

    }

    /**
     * 初始化大端小端类型
     */
    private void initByteOder() {
        if (!getUrl().getParameter(NetConstants.BYTE_ODER_BIG_ENDIAN_KEY, NetConstants.DEFAULT_BYTE_ODER_BIG)) {
            byteOrder = ByteOrder.LITTLE_ENDIAN;
        } else {
            byteOrder = ByteOrder.BIG_ENDIAN;
        }
    }

    /**
     * 启动器初始化
     *
     * @throws Throwable
     */
    private void initializerStart() throws Throwable {
        String netProtocol = getNetProtocol();
        if (netProtocol.equals(NetConstants.DEFAULT_TCP)) {
            this.starter = new TCPServerStarter(this);
        } else if (netProtocol.equals(NetConstants.DEFAULT_KCP)) {
            this.starter = new KCPServerStarter(this);
        } else if (netProtocol.equals(NetConstants.DEFAULT_WS)) {
            this.starter = new WSServerStarter(this);
        } else {
            throw new NotSupportException("Not support the protocol: [" + netProtocol + "],please check config");
        }

        logger.info(String.format("[Server message protocol starter use             ] : %s  ", this.starter.getClass().getName()));
    }

    public String getNetProtocol() {
        return getUrl().getParameter(NetConstants.NET_PROTOCOL_KEY, NetConstants.DEFAULT_NET_PROTOCOL);
    }

    /**
     * 解码器初始化
     *
     * @throws Throwable
     */
    private void initializerCodec() throws Throwable {
        Class codecCls = Class.forName(getUrl().getParameter(NetConstants.CODEC_KEY, NetConstants.DEFAULT_REMOTING_CODEC));
        Constructor constructor = codecCls.getConstructor(UrlProperties.class);
        this.codec = (Codec) constructor.newInstance(getUrl());
        logger.info(String.format("[Server message codec use                        ] : %s  ", this.codec.getClass().getName()));
    }

    /**
     * 初始化消息接收线程池
     */
    private void initializerThreadPool() {
        if (isOpenMsgReceiveEventLoop()) {
            String threadNamePrefix = this.getUrl().getParameter(NetConstants.MESSAGE_RECEIVE_THREAD_NAME, NetConstants.MESSAGE_RECEIVE_THREAD_NAME);
            int threads = getUrl().getParameter(NetConstants.THREADS_KEY, NetConstants.DEFAULT_IO_THREADS);
            this.msgEventLoopGroup = new BitactorEventLoopGroup(threads, new NamedThreadFactory(threadNamePrefix));
            logger.info(String.format("[Server open message receive event loop prefix   ] : %s  ", threadNamePrefix));
            logger.info(String.format("[Server open message receive event loop size     ] : %s  ", threads));
        }
    }

    protected boolean isOpenMsgReceiveEventLoop() {
        return this.getUrl().getParameter(NetConstants.MSG_RECEIVE_EVENT_LOOP_KEY, NetConstants.DEFAULT_MSG_RECEIVE_EVENT_LOOP_OPEN);
    }

    /**
     * 初始化握手消息
     */
    private void initializerHandShakeData() throws Throwable {
        HandShakeData hs = new HandShakeData();
        HandShakeDataBound systemBond = new SystemHandShakeDataBound();
        systemBond.buildCustomHandShakeData(hs, getUrl());

        List<String> classes = Arrays.asList(getUrl().getParameter(NetConstants.HAND_SHAKE_DATA_BOUND_CLASS_KEY, new String[0]));
        if (!CollectionUtils.isEmpty(classes)) {
            for (String className : classes) {
                HandShakeDataBound customBond = (HandShakeDataBound) Class.forName(className).newInstance();
                customBond.buildCustomHandShakeData(hs, getUrl());
                logger.info(String.format("[Server use custom HandShakeDataBound            ] : %s  ", className));
            }
        }
        this.HandShake = JSON.toJSONBytes(hs);
        logger.info(String.format("[Server hand shake data                          ] : %s  ", hs));
    }

    @Override
    public void shutdownNotify() {
        super.shutdownNotify();
        if (Objects.nonNull(this.msgEventLoopGroup)) {
            this.msgEventLoopGroup.shutdownNow();
            logger.info("close messageThreadPool...");
        }
        logger.info("close singleService...");
    }

    /**
     * 初始化连接确认任务
     */
    private void initializerAckTimer() {
        getTimer().schedule(new TimerTask() {
            @Override
            public void run() {
                for (Channel channel : ackChannels.values()) {
                    checkTimeOutAndClose(channel);
                }
            }

            private void checkTimeOutAndClose(Channel channel) {
                long timeout = getUrl().getParameter(NetConstants.ACK_TIMEOUT_KEY, NetConstants.DEFAULT_ACK_TIMEOUT);
                if (System.currentTimeMillis() - channel.getAttrVal(NetConstants.ACK_KEY, 0L) > timeout) {
                    logger.info("Ack timeout will close connection id: " + channel.getChannelId() + " remoteAddress" + channel
                            .getRemoteAddress());
                    channel.close();
                }
            }
        }, 2000, getUrl().getParameter(NetConstants.ACK_PERIOD_KEY, NetConstants.DEFAULT_ACK_PERIOD));
    }

    public String getServerDefinition() {
        return getUrl().getParameter(RPCConstants.SERVER_DEFINITION_KEY);
    }


    @Override
    public void start() {
        this.starter.start();
    }

    @Override
    public void close() {
        this.starter.close();
    }

    @Override
    public boolean isActive() {
        return starter.isActive();
    }

    @Override
    public boolean isStart() {
        return isActive();
    }

    @Override
    public Codec getCodec() {
        return this.codec;
    }

    public String getChannelIP(io.netty.channel.Channel channel) {
        return ((InetSocketAddress) channel.remoteAddress()).getAddress().getHostAddress();
    }

    public boolean isOpenHeartbeat() {
        return getUrl().getParameter(NetConstants.HEARTBEAT_OPEN_KEY, NetConstants.DEFAULT_HEARTBEAT_OPEN);
    }
}
