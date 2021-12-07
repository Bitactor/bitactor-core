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


import com.bitactor.framework.core.constant.NetConstants;

/**
 * netty的通道实现
 *
 * @author WXH
 */
public abstract class NettyChannel extends AbstractNettyChannel {

    private long lastHeartbeatTime = System.currentTimeMillis();

    public NettyChannel(NettyChannelContext channelContext, ChannelNettySendPolicy sendPolicy) {
        super(channelContext, sendPolicy);
        setAttrVal(NetConstants.ACK_KEY, System.currentTimeMillis());
        setAttrVal(NetConstants.CONNECT_TIME, System.currentTimeMillis());
    }

}
