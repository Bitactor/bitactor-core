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

package com.bitactor.framework.core.rpc.api;


import com.bitactor.framework.core.net.api.Channel;
import com.bitactor.framework.core.net.api.ChannelContext;

/**
 * @author WXH
 */
public interface ListenerAssist {
    /**
     * 绑定一个通道
     *
     * @param channelContext 通道上下文
     * @return Channel
     */
    Channel buildChannel(ChannelContext channelContext);

    /**
     * 对应通道激活通知
     *
     * @param channel Channel
     */
    void activityChannel(Channel channel);

    /**
     * 服务关闭通知
     */
    void shutdownNotify();
}
