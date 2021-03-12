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


import com.bitactor.framework.core.rpc.api.type.ResponseType;
import com.bitactor.framework.core.utils.lang.StringUtils;

/**
 * @author WXH
 */
public class RPCResponse {
    private RPCRequest request;
    private RPCResult result;
    private String errorMsg;
    private ResponseType status = ResponseType.OK;

    public RPCResponse() {
    }

    public RPCResponse(RPCRequest request, RPCResult result) {
        this.request = request;
        this.result = result;
        if (result.hasException()) {
            status = ResponseType.EXCEPTION;
            setErrorMsg(StringUtils.toString(result.getException()));
        }
    }

    public RPCResponse(RPCRequest request, RPCResult result, ResponseType status) {
        this.request = request;
        this.result = result;
        this.status = status;
    }

    public ResponseType getStatus() {
        return status;
    }

    public void setStatus(ResponseType status) {
        this.status = status;
    }

    public RPCRequest getRequest() {
        return request;
    }

    public void setRequest(RPCRequest request) {
        this.request = request;
    }

    public RPCResult getResult() {
        return result;
    }

    public void setResult(RPCResult result) {
        this.result = result;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    @Override
    public String toString() {
        return "RPCResponse{" +
                "request=" + request +
                ", result=" + result +
                ", errorMsg='" + errorMsg + '\'' +
                ", status=" + status +
                '}';
    }
}
