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

package com.bitactor.framework.core.net.netty.handler;

import com.bitactor.framework.core.constant.NetConstants;
import com.bitactor.framework.core.net.api.ChannelBound;
import com.bitactor.framework.core.net.api.transport.message.MessageWrapper;
import com.bitactor.framework.core.utils.assist.UrlPropertiesUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author WXH
 */
public class EncoderHandler extends MessageToByteEncoder<MessageWrapper> {

    /**
     * 抽象化连接类，用于通知服务（NetServer 或 NettyClient）
     */
    private ChannelBound channelBound;

    private int lengthFieldLength;

    public EncoderHandler(ChannelBound channelBound) {
        this.channelBound = channelBound;
        this.lengthFieldLength = UrlPropertiesUtils.getPortoHeadLength(channelBound.getUrl());
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, MessageWrapper messageWrapper, ByteBuf byteBuf) throws Exception {
        ByteBuf data = (ByteBuf) this.channelBound.getCodec().encode(messageWrapper);
        long byteLength = data.readableBytes();
        writeHeadLength(channelHandlerContext, byteBuf, byteLength);
        byteBuf.writeBytes(data);
    }

    /**
     * 写入头长度
     *
     * @param channelHandlerContext
     * @param byteBuf
     * @param byteLength
     */
    private void writeHeadLength(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, long byteLength) {
        if (byteLength > channelBound.getUrl().getParameter(NetConstants.BUFFER_KEY, NetConstants.DEFAULT_BUFFER_SIZE)) {
            throw new EncoderException("the size: " + byteLength + " ,greater than max bytes size");
        }
        byteBuf = byteBuf.order(channelBound.getByteOrder());
        switch (lengthFieldLength) {
            case 1:
                byteBuf.writeByte((int) byteLength);
                break;
            case 2:
                byteBuf.writeShort((int) byteLength);
                break;
            case 3:
                byteBuf.writeMedium((int) byteLength);
                break;
            case 4:
                byteBuf.writeInt((int) byteLength);
                break;
            case 8:
                byteBuf.writeLong(byteLength);
                break;
            default:
                throw new EncoderException(
                        "unsupported lengthFieldLength: " + lengthFieldLength + " (expected: 1, 2, 3, 4, or 8)");
        }
    }
}
