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

package com.bitactor.framework.core.net.netty.channel;

import com.bitactor.framework.core.config.UrlProperties;
import com.bitactor.framework.core.net.api.Channel;
import com.bitactor.framework.core.net.api.transport.message.MessageClose;
import com.bitactor.framework.core.net.api.transport.message.MessageWrapper;
import com.bitactor.framework.core.logger.Logger;
import com.bitactor.framework.core.logger.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author WXH
 */
public abstract class AbstractNettyChannel implements Channel {
    private static final Logger logger = LoggerFactory.getLogger(AbstractNettyChannel.class);
    private NettyChannelContext channelContext;
    private ConcurrentHashMap<String, Object> longMap = new ConcurrentHashMap<String, Object>();

    public AbstractNettyChannel(NettyChannelContext channelContext) {
        this.channelContext = channelContext;
    }

    public NettyChannelContext getChannelContext() {
        return channelContext;
    }

    @Override
    public String getChannelId() {
        return this.getChannelContext().getContext().channel().id().toString();
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return (InetSocketAddress) getChannelContext().getContext().channel().remoteAddress();
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return (InetSocketAddress) getChannelContext().getContext().channel().localAddress();
    }

    @Override
    public void justClose() {
        getChannelContext().getContext().close();
    }

    @Override
    public void close() {
        send(new MessageClose());
        getChannelContext().getContext().close();
    }

    @Override
    public boolean isActive() {
        return getChannelContext().getContext().channel().isActive();
    }

    @Override
    public void send(MessageWrapper message) {
        this.getChannelContext().getContext().writeAndFlush(message);
    }

    @Override
    public void setAttrVal(String key, Object val) {
        longMap.put(key, val);
    }

    @Override
    public <V> V getAttrVal(String key, V def) {
        Object val = longMap.get(key);
        return (V) (val == null ? def : val);
    }

    @Override
    public UrlProperties getUrl() {
        return getChannelContext().getUrl();
    }
}
