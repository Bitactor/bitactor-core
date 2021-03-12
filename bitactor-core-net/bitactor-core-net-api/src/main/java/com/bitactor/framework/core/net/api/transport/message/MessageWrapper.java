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

package com.bitactor.framework.core.net.api.transport.message;


import com.bitactor.framework.core.net.api.type.ByteSize;

import java.util.ArrayList;
import java.util.List;

/**
 * 消息包装器
 *
 * @author WXH
 */
public class MessageWrapper {
    /**
     * 消息类型
     */
    private byte type;
    /**
     * 消息数据
     */
    private byte[] data;

    public MessageWrapper(byte type, byte[] data) {
        this.type = type;
        this.data = data;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getAllBytesLength() {
        return ByteSize.BYTE_BYTES_SIZE.getVal() + (data == null ? 0 : data.length);
    }

    public String toString() {
        return "Class:" + this.getClass().getSimpleName() + " type: " + type + " data: " + toToByteStr(data) + " size: " + (data!=null?data.length:0);
    }

    public static String toToByteStr(byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        List<Byte> temp = new ArrayList<Byte>(bytes.length);
        for (byte aByte : bytes) {
            temp.add(aByte);
        }
        return temp.toString();
    }
}
