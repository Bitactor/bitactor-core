
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


import com.bitactor.framework.core.config.UrlProperties;
import com.bitactor.framework.core.net.api.transport.message.MessageWrapper;

import java.net.InetSocketAddress;

/**
 * 网络连接的通道接口
 *
 * @author WXH
 */
public interface Channel {
    /**
     * 获取通道id
     *
     * @return channel id.
     */
    String getChannelId();

    /**
     * 获取远程地址
     *
     * @return remote address.
     */
    InetSocketAddress getRemoteAddress();

    /**
     * 获取本地地址.
     *
     * @return remote address.
     */
    InetSocketAddress getLocalAddress();

    /**
     * 发送消息
     *
     * @param message
     */
    void send(MessageWrapper message);

    /**
     * 接收消息
     *
     * @param message
     */
    void onReceived(MessageWrapper message);

    /**
     * 变得活跃
     */
    void onActivity();

    /**
     * 销毁
     */
    void onDestroy();

    /**
     * 仅关闭通道
     */
    void justClose();

    /**
     * 关闭通道，但是可能会有其他的业务
     */
    void close();

    /**
     * 是否活跃
     *
     * @return active
     */
    boolean isActive();

    void setAttrVal(String key, Object val);

    <V> V getAttrVal(String key, V def);

    UrlProperties getUrl();

}
