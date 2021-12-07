
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

package com.bitactor.framework.core.net.api;

import java.util.Collection;
import java.util.List;

/**
 * 网络服务端接口
 *
 * @author WXH
 */
public interface Server<CF> {

    /**
     * is bound.
     *
     * @return bound
     */
    boolean isBound();

    /**
     * get channels.
     *
     * @return channels
     */
    Collection<Channel<CF>> getChannels();

    /**
     * get channel.
     *
     * @return channel
     */
    Channel<CF> getChannel(String channelId);

    /**
     * get channel.
     *
     * @return channel
     */
    List<Channel<CF>> getChannels(List<String> channelIds);

}
