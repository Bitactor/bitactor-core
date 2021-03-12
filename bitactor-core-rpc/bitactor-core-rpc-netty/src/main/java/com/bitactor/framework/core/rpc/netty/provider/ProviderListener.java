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

package com.bitactor.framework.core.rpc.netty.provider;


import com.bitactor.framework.core.net.api.Channel;
import com.bitactor.framework.core.net.api.ChannelContext;
import com.bitactor.framework.core.net.api.ChannelManager;
import com.bitactor.framework.core.rpc.api.ListenerAssist;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author WXH
 */
public class ProviderListener implements ChannelManager {
    private ListenerAssist listenerAssist;

    public ProviderListener(ListenerAssist listenerAssist) {
        this.listenerAssist = listenerAssist;
    }

    private ConcurrentHashMap<String, Channel> channels = new ConcurrentHashMap<String, Channel>();

    @Override
    public Channel registerChannel(ChannelContext channelContext) {
        Channel channel = listenerAssist.buildChannel(channelContext);
        channels.put(channel.getChannelId(), channel);
        return channel;

    }

    @Override
    public Channel destroyChannel(String channelId) {
        return channels.remove(channelId);
    }

    @Override
    public void activityChannel(Channel channel) {
        if (channels.containsKey(channel.getChannelId())) {
            listenerAssist.activityChannel(channel);
        }
    }

    @Override
    public void shutdownNotify() {
        listenerAssist.shutdownNotify();
    }

    public Channel getChannel(String channelId) {
        return channels.get(channelId);
    }
}
