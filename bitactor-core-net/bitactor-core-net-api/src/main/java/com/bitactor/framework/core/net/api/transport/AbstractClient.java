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

package com.bitactor.framework.core.net.api.transport;


import com.bitactor.framework.core.config.UrlProperties;
import com.bitactor.framework.core.logger.Logger;
import com.bitactor.framework.core.logger.LoggerFactory;
import com.bitactor.framework.core.net.api.Channel;
import com.bitactor.framework.core.net.api.ChannelManager;
import com.bitactor.framework.core.net.api.Client;

import java.util.Timer;


/**
 * 抽象net provider
 *
 * @author WXH
 */
public abstract class AbstractClient<CF> extends AbstractNetPoint implements Client<CF> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractClient.class);
    /**
     * 网络服务管理器
     */
    protected final ChannelManager channelManager;

    protected final UrlProperties url;

    private Channel<CF> channel;

    public AbstractClient(ChannelManager channelManager, UrlProperties url) {
        this.channelManager = channelManager;
        this.url = url;
    }

    private Timer timer;

    protected Timer getTimer() {
        if (timer == null) {
            timer = new Timer("NettyClientTimer");
        }
        return timer;
    }

    private void cancelTimer() {
        if (timer == null) {
            return;
        }
        timer.cancel();
    }

    @Override
    public AbstractClient<CF> threadStart() {
        EndpointThread clientThread = new EndpointThread(this);
        clientThread.setName("#ClientStartThread");
        clientThread.start();
        return this;
    }

    public Channel<CF> getChannel() {
        return this.channel;
    }

    public void destroyChannel() {
        if (this.channel == null) {
            return;
        }
        channelManager.destroyChannel(this.channel.getChannelId());
        this.channel.onDestroy();
        logger.info("Client data channel has closed channel id:" + this.channel.getChannelId() + " remote address : " + this.channel.getRemoteAddress());
        this.channel = null;
    }

    /**
     * 如果通道已经存在，我们将关闭现有的通道
     *
     * @param channel
     */
    protected void addChannel(Channel<CF> channel) {
        if (this.channel != null) {
            this.channel.close();
        }
        this.channel = channel;

        // 通知解除等待 await
        this.signal();
    }

    @Override
    public void shutdownNotify() {
        channelManager.shutdownNotify();
        cancelTimer();
        // 通知解除等待 await
        this.signal();
    }

    @Override
    public void startNotify() {
        // do nothing
    }

}
