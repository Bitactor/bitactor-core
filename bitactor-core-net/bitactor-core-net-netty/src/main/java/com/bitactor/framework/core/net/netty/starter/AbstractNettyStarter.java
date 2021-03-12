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

package com.bitactor.framework.core.net.netty.starter;


import com.bitactor.framework.core.config.UrlProperties;
import com.bitactor.framework.core.net.api.ChannelBound;
import com.bitactor.framework.core.net.api.Starter;
import com.bitactor.framework.core.logger.Logger;
import com.bitactor.framework.core.logger.LoggerFactory;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;

/**
 * 启动器抽象类
 *
 * @author WXH
 */
public abstract class AbstractNettyStarter<T> implements Starter {

    private static final Logger logger = LoggerFactory.getLogger(AbstractNettyStarter.class);
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    protected ChannelFuture future;


    protected final ChannelBound channelBound;

    public AbstractNettyStarter(ChannelBound channelBound) {
        this.channelBound = channelBound;
    }

    public UrlProperties getUrl() {
        return channelBound.getUrl();
    }

    protected EventLoopGroup getWorkerGroup() {
        return workerGroup;
    }

    protected void setWorkerGroup(EventLoopGroup workerGroup) {
        this.workerGroup = workerGroup;
    }

    protected EventLoopGroup getBossGroup() {
        return bossGroup;
    }

    protected void setBossGroup(EventLoopGroup bossGroup) {
        this.bossGroup = bossGroup;
    }

    protected ChannelFuture getFuture() {
        return future;
    }

    protected void setFuture(ChannelFuture future) {
        this.future = future;
    }

    protected abstract void initBossGroup();

    protected abstract void initWorkGroup();

    protected abstract Class<? extends T> getChannelClass();

    @Override
    public ChannelBound getChannelBound() {
        return channelBound;
    }

}
