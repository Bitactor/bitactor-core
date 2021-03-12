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

package com.bitactor.framework.core.rpc.api.invoker;

import javassist.util.proxy.MethodHandler;

import java.lang.reflect.Method;

/**
 * @author WXH
 */
public abstract class AbstractInvokerHandler<T> implements MethodHandler {

    private Class<T> inf;

    private String tempStr;

    /**
     * javassist 专用
     *
     * @param inf
     */
    public AbstractInvokerHandler(Class<T> inf) {
        this.inf = inf;
    }

    /**
     * javassist 专用
     *
     * @param inf
     */
    public AbstractInvokerHandler(Class<T> inf, String tempStr) {
        this.inf = inf;
        this.tempStr = tempStr;
    }

    /**
     * javassist 专用
     */
    @Override
    public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
        return doInvokeByCheck(self, thisMethod, proceed, args);
    }

    public Class<T> getInterface() {
        return this.inf;
    }

    public String getTempStr() {
        return tempStr;
    }

    private Object doInvokeByCheck(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
        String methodName = thisMethod.getName();
        Class<?>[] parameterTypes = thisMethod.getParameterTypes();
        if (thisMethod.getDeclaringClass() == Object.class
                || "toString".equals(methodName) && parameterTypes.length == 0
                || "hashCode".equals(methodName) && parameterTypes.length == 0
                || "equals".equals(methodName) && parameterTypes.length == 1) {
            return proceed.invoke(self, args);
        }
        return doInvoke(self, thisMethod, proceed, args);
    }


    protected abstract Object doInvoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable;
}
