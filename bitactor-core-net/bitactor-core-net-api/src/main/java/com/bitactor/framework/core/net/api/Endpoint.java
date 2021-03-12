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

import java.net.InetSocketAddress;

/**
 * 网络服务管理接口
 *
 * @author WXH
 */
public interface Endpoint {
    /**
     * 新开线程启动
     */
    Endpoint threadStart();

    /**
     * 同步等待启动完成
     */
    void sync() throws InterruptedException;

    /**
     * 启动服务
     */
    void start();

    /**
     * get local address.
     *
     * @return local address.
     */
    InetSocketAddress getLocalAddress();

    /**
     * close the channel.
     */
    void close();

    /**
     * is closed.
     *
     * @return closed
     */
    boolean isActive();

    /**
     * 获取服务参数
     *
     * @return
     */
    UrlProperties getUrl();

    /**
     * 是否成功启动
     *
     * @return
     */
    boolean isStart();
}
