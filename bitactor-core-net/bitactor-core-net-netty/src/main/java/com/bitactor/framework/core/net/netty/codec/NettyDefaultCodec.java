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

package com.bitactor.framework.core.net.netty.codec;

import com.bitactor.framework.core.config.UrlProperties;
import com.bitactor.framework.core.constant.NetConstants;
import com.bitactor.framework.core.net.api.transport.message.*;
import com.bitactor.framework.core.net.api.type.MessageType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

import java.nio.ByteOrder;

/**
 * netty下的默认编码解码器
 *
 * @author WXH
 */
public class NettyDefaultCodec extends NettyAbstractCodec {
    public NettyDefaultCodec(UrlProperties url) {
        super(url);
    }

    @Override
    public final MessageWrapper decode(Object buffer) {
        ByteBuf buf = (ByteBuf) buffer;
        MessageWrapper message = null;
        if (buf.isReadable()) {
            byte type = buf.readByte();
            byte[] data = null;
            data = ByteBufUtil.getBytes(buf, buf.readerIndex(), buf.readableBytes());
            message = buildMessage(type, data);
        }
        return message;
    }

    @Override
    public Object encode(MessageWrapper message) {
        ByteBuf in = Unpooled.buffer(message.getAllBytesLength());
        in.writeByte(message.getType());
        if (message.getData(getByteOrder()) != null) {
            in.writeBytes(message.getData());
        }
        return in;
    }

    private MessageWrapper buildMessage(byte type, byte[] data) {
        MessageWrapper msg = null;
        switch (type) {
            case MessageType.TYPE_HANDSHAKE:
                msg = new MessageHandShake(data);
                break;
            case MessageType.TYPE_ACK:
                msg = new MessageAck(data);
                break;
            case MessageType.TYPE_HEARTBEAT:
                msg = new MessageHeartBeat(data);
                break;
            case MessageType.TYPE_DATA:
                msg = buildMessageData(data);
                break;
            case MessageType.TYPE_CLOSE:
                msg = new MessageClose(data);
                break;
            default:
                msg = new MessageWrapper(type, data);
                break;
        }
        return msg;
    }

    public MessageWrapper buildMessageData(byte[] data) {
        return new MessageData(data);
    }

    public static byte[] getLongToBytes(long var, ByteOrder byteOrder) {
        try {
            ByteBuf buf = Unpooled.buffer(NetConstants.BYTES_8_LENGTH);
            if (byteOrder.equals(ByteOrder.LITTLE_ENDIAN)) {
                buf.writeLongLE(var);
            } else {
                buf.writeLong(var);
            }
            return ByteBufUtil.getBytes(buf, buf.readerIndex(), buf.readableBytes());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static long getBytesToLong(byte[] var, ByteOrder byteOrder) {
        try {
            ByteBuf buf = Unpooled.buffer(NetConstants.BYTES_8_LENGTH);
            buf.writeBytes(var);
            long last = 0;
            if (byteOrder.equals(ByteOrder.LITTLE_ENDIAN)) {
                last = buf.readLongLE();
            } else {
                last = buf.readLong();
            }
            return last;
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }
}
