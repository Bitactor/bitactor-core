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

package com.bitactor.framework.core.rpc.netty.codec;

import com.bitactor.framework.core.config.UrlProperties;
import com.bitactor.framework.core.net.api.transport.message.MessageData;
import com.bitactor.framework.core.net.api.transport.message.MessageWrapper;
import com.bitactor.framework.core.net.netty.codec.NettyDefaultCodec;
import com.bitactor.framework.core.rpc.api.type.RPCDataType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

/**
 * netty下的默认编码解码器
 *
 * @author WXH
 */
public class NettyRPCCodec extends NettyDefaultCodec {
    public NettyRPCCodec(UrlProperties url) {
        super(url);
    }

    public MessageWrapper buildMessageData(byte[] data) {
        ByteBuf buf = Unpooled.buffer(data.length);
        buf.writeBytes(data);
        byte type = buf.readByte();
        byte[] serializeData = ByteBufUtil.getBytes(buf, buf.readerIndex(), buf.readableBytes());
        if (type == RPCDataType.REQUEST) {
            return new MessageRPCRequest(data, serializeData);
        } else if (type == RPCDataType.RESPONSE) {
            return new MessageRPCResponse(data, serializeData);
        } else {
            return new MessageData(data);
        }
    }
}
