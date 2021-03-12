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

package com.bitactor.framework.core.rpc.api.async;

import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * @author WXH
 */
public class AsyncResultImpl<T> implements AsyncResult<T> {

    private static final long serialVersionUID = 4876686268921122648L;
    private T args;
    private Throwable cause;

    private BiConsumer<Object, Throwable> async;

    public T getArgs() {
        return args;
    }

    public void setArgs(T args) {
        this.args = args;
    }

    public Throwable getCause() {
        return cause;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    public void setAsync(BiConsumer<Object, Throwable> async) {
        this.async = async;
    }

    @Override
    public void callback(T args, Throwable cause) {
        this.args = args;
        this.cause = cause;
        if (Objects.nonNull(async)) {
            async.accept(args, cause);
        }
    }
}
