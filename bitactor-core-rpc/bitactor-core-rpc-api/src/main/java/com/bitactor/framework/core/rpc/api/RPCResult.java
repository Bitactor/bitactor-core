
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

import java.io.Serializable;

/**
 * 调用RPC的结果
 */
public class RPCResult implements Result, Serializable {


    private static final long serialVersionUID = -6483579832986450422L;
    private Object result;

    private Throwable exception;

    public RPCResult() {
    }

    public RPCResult(Object result) {
        this.result = result;
    }

    public RPCResult(Throwable exception) {
        this.exception = exception;
    }

    public Object get() throws Throwable {
        if (exception != null) {
            throw exception;
        }
        return result;
    }

    public Object getValue() {
        return result;
    }

    public void setValue(Object value) {
        this.result = value;
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable e) {
        this.exception = e;
    }

    public boolean hasException() {
        return exception != null;
    }

    @Override
    public String toString() {
        return "RPCResult{" +
                "result=" + result +
                ", exception=" + exception +
                '}';
    }
}