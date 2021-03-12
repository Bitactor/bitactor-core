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

package com.bitactor.framework.core.rpc.api;

import com.bitactor.framework.core.rpc.api.invoker.Invocation;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author WXH
 */
public class RPCRequest {
    private static final AtomicLong INVOKE_ID = new AtomicLong(0);
    private long reqId;
    private String apiId;
    private String group;
    private Invocation invocation;

    public RPCRequest() {
    }

    public RPCRequest(String apiId, String group, Invocation invocation) {
        this.reqId = newId();
        this.apiId = apiId;
        this.invocation = invocation;
        this.group = group;

    }

    private static long newId() {
        return INVOKE_ID.getAndIncrement();
    }

    public long getReqId() {
        return reqId;
    }

    public void setReqId(long reqId) {
        this.reqId = reqId;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getApiId() {
        return apiId;
    }

    public void setApiId(String apiId) {
        this.apiId = apiId;
    }

    public Invocation getInvocation() {
        return invocation;
    }

    public void setInvocation(Invocation invocation) {
        this.invocation = invocation;
    }

    @Override
    public String toString() {
        return "RPCRequest{" +
                "reqId=" + reqId +
                ", apiId='" + apiId + '\'' +
                ", group='" + group + '\'' +
                ", invocation=" + invocation +
                '}';
    }
}
