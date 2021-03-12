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
import com.bitactor.framework.core.net.api.Channel;
import com.bitactor.framework.core.net.api.Server;
import com.bitactor.framework.core.net.api.ChannelManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 抽象net provider
 *
 * @author WXH
 */
public abstract class AbstractServer extends AbstractNetPoint implements Server {

    /**
     * 网络服务管理器
     */
    protected final ChannelManager channelManager;

    protected final UrlProperties url;

    /**
     * 通道集合;
     */
    private ConcurrentHashMap<String, Channel> channels = new ConcurrentHashMap<String, Channel>();

    public AbstractServer(ChannelManager channelManager, UrlProperties url) {
        this.channelManager = channelManager;
        this.url = url;
    }

    private Timer timer;

    @Override
    public AbstractServer threadStart() {
        EndpointThread serverThread = new EndpointThread(this);
        serverThread.setDaemon(true);
        serverThread.setName("#ServerStartThread");
        serverThread.start();
        return this;
    }

    protected Timer getTimer() {
        if (timer == null) {
            timer = new Timer("NettyServerTimer");
        }
        return timer;
    }

    private void cancelTimer() {
        if (timer == null) {
            return;
        }
        timer.cancel();
    }

    /**
     * 返回一个通道实例
     *
     * @param channelId 通道id
     * @return 通道实例
     */
    public Channel getChannel(String channelId) {
        return channels.get(channelId);
    }

    /**
     * 获取通道集合
     *
     * @return
     */
    public Collection<Channel> getChannels() {
        return channels.values();
    }

    /**
     * 获取集合中的所有存在的channel
     *
     * @param channelIds
     * @return
     */
    public List<Channel> getChannels(List<String> channelIds) {
        List<Channel> resultChannels = new ArrayList<Channel>();
        for (String channelId : channelIds) {
            Channel channel = this.channels.get(channelId);
            if (channel != null) {
                resultChannels.add(channel);
            }
        }
        return resultChannels;
    }

    /**
     * 添加一个通道
     */
    protected void addChannel(Channel channel) {
        channels.putIfAbsent(channel.getChannelId(), channel);

    }

    /**
     * 移除一个channel
     *
     * @param channelId 通道Id
     * @return Channel
     */
    protected Channel removeChannel(String channelId) {
        return channels.remove(channelId);
    }

    public ChannelManager getChannelManager() {
        return channelManager;
    }

    @Override
    public void shutdownNotify() {
        channelManager.shutdownNotify();
        cancelTimer();
        // 唤醒启动等待
        this.signal();
    }

    @Override
    public void startNotify() {
        this.signal();
    }

    @Override
    public UrlProperties getUrl() {
        return this.url;
    }
}
