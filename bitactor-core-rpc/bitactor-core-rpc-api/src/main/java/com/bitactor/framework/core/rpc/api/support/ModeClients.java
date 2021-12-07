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

package com.bitactor.framework.core.rpc.api.support;

import com.bitactor.framework.core.config.UrlProperties;
import com.bitactor.framework.core.constant.CommonConstants;
import com.bitactor.framework.core.constant.RPCConstants;
import com.bitactor.framework.core.utils.collection.CollectionUtils;
import com.bitactor.framework.core.utils.lang.CycleAtomicInteger;
import com.bitactor.framework.core.net.api.Channel;
import com.bitactor.framework.core.net.api.transport.AbstractClient;
import com.bitactor.framework.core.logger.Logger;
import com.bitactor.framework.core.logger.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author WXH
 */
public class ModeClients<CF> {

    private static final Logger logger = LoggerFactory.getLogger(ModeClients.class);
    private List<AbstractClient<CF>> clients;
    private CycleAtomicInteger lastIndex = new CycleAtomicInteger();
    private UrlProperties url;

    public ModeClients(UrlProperties url, clientBuilder<CF> builder) {
        this.url = url;
        int channelSize = Math.max(0, Math.min(this.url.getParameter(RPCConstants.CONSUMERS_CHANNEL_SIZE_KEY, RPCConstants.DEFAULT_CONSUMERS_CHANNEL_SIZE), CommonConstants.RUN_THREADS));
        clients = new ArrayList<>(channelSize);
        for (int i = 0; i < channelSize; i++) {
            try {
                AbstractClient<CF> nettyClient = builder.build();
                nettyClient.threadStart().sync();
                clients.add(nettyClient);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
        logger.info("Instance ModeClients size:{},url:{}", clients.size(), url.getGroupAndId());
    }

    public UrlProperties getUrl() {
        return this.url;
    }

    public boolean isActive() {
        if (CollectionUtils.isEmpty(clients)) {
            return false;
        }
        for (AbstractClient<CF> client : clients) {
            if (client.isActive()) {
                return true;
            }
        }
        return false;
    }

    public void close() {
        if (CollectionUtils.isEmpty(clients)) {
            return;
        }
        for (AbstractClient<CF> client : clients) {
            client.close();
        }
        clients.clear();
        logger.info("Remove ModeClients size:{},url:{}", clients.size(), url.getGroupAndId());
    }

    public Channel<CF> getChannel() {
        return Optional.of(getClient()).orElse(null).getChannel();
    }

    public AbstractClient<CF> getClient() {
        try {
            lastIndex.next(clients.size());
            if (CollectionUtils.isEmpty(clients)) {
                return null;
            }
            int nowIndex = lastIndex.next(clients.size());
            return clients.get(nowIndex);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @FunctionalInterface
    public interface clientBuilder<CF> {
        AbstractClient<CF> build() throws Throwable;
    }
}
