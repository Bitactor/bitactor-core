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

package com.bitactor.framework.core.net.api.type;

/**
 * 消息类型
 *
 * @author WXH
 */
public class MessageType {
    /**
     * 客户端到服务器的握手请求以及服务器到客户端的握手响应
     */
    public static final byte TYPE_HANDSHAKE = 0x01;
    /**
     * 客户端到服务器的握手ack
     */
    public static final byte TYPE_ACK = 0x02;
    /**
     * 心跳包
     */
    public static final byte TYPE_HEARTBEAT = 0x03;
    /**
     * 数据包
     */
    public static final byte TYPE_DATA = 0x04;
    /**
     * 关闭消息
     */
    public static final byte TYPE_CLOSE = 0x10;

}
