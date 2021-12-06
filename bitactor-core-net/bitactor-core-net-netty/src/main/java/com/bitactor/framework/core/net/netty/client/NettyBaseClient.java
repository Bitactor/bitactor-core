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

package com.bitactor.framework.core.net.netty.client;

import com.bitactor.framework.core.config.UrlProperties;
import com.bitactor.framework.core.constant.NetConstants;
import com.bitactor.framework.core.exception.ErrorConfigException;
import com.bitactor.framework.core.exception.NotSupportException;
import com.bitactor.framework.core.logger.Logger;
import com.bitactor.framework.core.logger.LoggerFactory;
import com.bitactor.framework.core.net.api.ChannelInit;
import com.bitactor.framework.core.net.api.ChannelManager;
import com.bitactor.framework.core.net.api.Codec;
import com.bitactor.framework.core.net.api.Starter;
import com.bitactor.framework.core.net.api.transport.AbstractClient;
import com.bitactor.framework.core.net.api.transport.HandShakeData;
import com.bitactor.framework.core.net.netty.channel.AckNettyChannel;
import com.bitactor.framework.core.net.netty.channel.ChannelNettyOptions;
import com.bitactor.framework.core.net.netty.client.starter.KCPClientStarter;
import com.bitactor.framework.core.net.netty.client.starter.TCPClientStarter;
import com.bitactor.framework.core.net.netty.client.starter.WSClientStarter;
import com.bitactor.framework.core.utils.lang.StringUtils;

import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;
import java.nio.ByteOrder;

/**
 * netty 的网络客户端
 *
 * @author WXH
 */
public abstract class NettyBaseClient extends AbstractClient {
    private static final Logger logger = LoggerFactory.getLogger(NettyBaseClient.class);
    private Starter starter;
    private Codec codec;
    private AckNettyChannel ackChannel;
    private HandShakeData handShakeData;
    private ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;

    private boolean isOpenHeartbeat = false;

    public NettyBaseClient(ChannelManager channelManager, UrlProperties url) throws Throwable {
        super(channelManager, url);
        this.initializer();
    }

    protected AckNettyChannel getAckChannel() {
        return ackChannel;
    }

    protected void setAckChannel(AckNettyChannel ackChannel) {
        this.ackChannel = ackChannel;
    }

    protected HandShakeData getHandShakeData() {
        return handShakeData;
    }

    protected void setHandShakeData(HandShakeData handShakeData) {
        this.handShakeData = handShakeData;
    }

    private void initializer() throws Throwable {
        initByteOder();

        initializerStart();

        initializerCodec();

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
        ChannelInit<ChannelNettyOptions> channelInit = null;
        String channelInitClazz = getChannelInitClazz();
        if (!StringUtils.isBlank(channelInitClazz)) {
            try {
                channelInit = (ChannelInit) Class.forName(channelInitClazz).newInstance();
            } catch (Exception e) {
                throw new ErrorConfigException("Init client channel class failed: [" + channelInitClazz + "],please check config");
            }
        }
        if (netProtocol.equals(NetConstants.DEFAULT_TCP)) {
            this.starter = new TCPClientStarter(this, channelInit);
        } else if (netProtocol.equals(NetConstants.DEFAULT_KCP)) {
            this.starter = new KCPClientStarter(this, channelInit);
        } else if (netProtocol.equals(NetConstants.DEFAULT_WS)) {
            this.starter = new WSClientStarter(this, channelInit);
        } else {
            throw new NotSupportException("Not support the protocol: [" + netProtocol + "],please check config");
        }
        logger.info(String.format("[Client message protocol starter use             ] %s  ", this.starter.getClass().getName()));
    }

    public String getNetProtocol() {
        return getUrl().getParameter(NetConstants.NET_PROTOCOL_KEY, NetConstants.DEFAULT_NET_PROTOCOL);
    }

    public String getChannelInitClazz() {
        return getUrl().getParameter(NetConstants.CHANNEL_INIT_CLASS_KEY);
    }

    /**
     * 解码器初始化
     *
     * @throws Throwable
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void initializerCodec() throws Throwable {
        Class codecCls = Class.forName(getUrl().getParameter(NetConstants.CODEC_KEY, NetConstants.DEFAULT_REMOTING_CODEC));
        Constructor constructor = codecCls.getConstructor(UrlProperties.class);
        this.codec = (Codec) constructor.newInstance(getUrl());
        logger.info(String.format("[Client message codeC use                        ] %s  ", this.codec.getClass().getName()));
    }

    @Override
    public void reconnect() {
        if (isActive()) {
            logger.warn("The old connection still exists. The old connection will be closed");
            this.close();
        }
        this.threadStart();
    }

    @Override
    public void start() {
        if (isStart()) {
            logger.warn("It is already connected and does not need to be restarted");
            return;
        }
        this.starter.start();
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return null;
    }

    @Override
    public void close() {
        this.starter.close();
    }

    @Override
    public boolean isActive() {
        if (getChannel() == null) {
            return false;
        }
        return getChannel().isActive();
    }

    @Override
    public boolean isStart() {
        return isActive();
    }

    @Override
    public UrlProperties getUrl() {
        return this.url;
    }

    @Override
    public Codec getCodec() {
        return this.codec;
    }


    @Override
    public ByteOrder getByteOrder() {
        return this.byteOrder;
    }

    @Override
    public boolean isOpenHeartbeat() {
        return false;
    }

    protected void setIsOpenHeartbeat(boolean isOpenHeartbeat) {
        this.isOpenHeartbeat = isOpenHeartbeat;
    }
}
