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

import com.bitactor.framework.core.net.api.transport.message.MessageData;
import com.bitactor.framework.core.rpc.api.RPCResponse;
import com.bitactor.framework.core.rpc.api.type.RPCDataType;
import com.bitactor.framework.core.utils.serialize.ProtostuffSerialize;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;


/**
 * @author WXH
 */
public class MessageRPCResponse extends MessageData {
    private RPCResponse response;

    public MessageRPCResponse(byte[] data, byte[] serializeData) {
        super(data);
        long start = System.currentTimeMillis();
        response = ProtostuffSerialize.deserializerToObj(serializeData, RPCResponse.class);
    }
    public MessageRPCResponse(RPCResponse response) {
        super(null);
        this.response = response;
        this.setData(encodeResponse(response));
    }

    private byte[] encodeResponse(RPCResponse response){
        byte[] data = ProtostuffSerialize.serializerToBytes(response);
        ByteBuf buf = Unpooled.buffer(data.length + 1);
        buf.writeByte(RPCDataType.RESPONSE);
        buf.writeBytes(data);
        return ByteBufUtil.getBytes(buf, buf.readerIndex(), buf.readableBytes());
    }

    public RPCResponse getResponse() {
        return response;
    }

}
