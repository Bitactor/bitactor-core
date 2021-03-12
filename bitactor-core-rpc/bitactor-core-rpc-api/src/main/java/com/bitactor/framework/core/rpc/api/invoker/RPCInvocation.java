
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

import com.bitactor.framework.core.exception.IllegalityRPCException;
import com.bitactor.framework.core.utils.lang.ClassHelper;
import com.bitactor.framework.core.utils.lang.StringUtils;
import com.bitactor.framework.core.rpc.api.annotation.Async;
import com.bitactor.framework.core.rpc.api.async.AsyncResult;
import com.bitactor.framework.core.rpc.api.async.AsyncResultImpl;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;

/**
 * RPC 调用.
 */
public class RPCInvocation implements Invocation, Serializable {

    private static final long serialVersionUID = -5773128125951441295L;
    // 调用方法名
    private String methodName;
    // 方法参数类型
    private Class<?>[] parameterTypes;
    //方法参数
    private Object[] arguments;
    // 附加参数
    private Map<String, String> attachments;

    private boolean async = false;
    // lamda 不能被序列化，所以禁止
    private transient List<AsyncResult> callbacks = new ArrayList<>();
    // 不被序列化的本地调用的RPCInvocation，区别在于，是原始的 Invocation
    private transient Object[] originalArguments;

    public RPCInvocation() {
    }

    public RPCInvocation(Invocation invocation) {
        this(invocation.getMethodName(), invocation.getParameterTypes(), invocation.getArguments(), invocation.getAttachments(), invocation.isAsync());
    }

    public RPCInvocation(Method method, Object[] arguments) {
        this(method, arguments, null);
    }

    public RPCInvocation(Method method, Object[] arguments, Map<String, String> attachments) {
        boolean async = method.getAnnotation(Async.class) != null;
        this.methodName = method.getName();
        this.parameterTypes = method.getParameterTypes();
        this.arguments = arguments == null ? new Object[0] : arguments;
        this.originalArguments = this.arguments.clone();
        this.attachments = attachments == null ? new HashMap<String, String>() : attachments;
        this.async = async;
        // 检查 异步 方法的 ReturnType 是否合法
        if (async && (method.getReturnType() != Void.TYPE)) {
            throw new IllegalityRPCException("Async Method: " + method.getName() + " must be void. ");
        }
        //  检查 参数是否含有异步回调参数
        if (async && !checkHasAsyncResultArg(this.arguments)) {
            throw new IllegalityRPCException("Async Method: " + method.getName() + " must contain [AsyncResult]. ");
        }
        if (checkHasNotSupportLambda(this.arguments)) {
            throw new IllegalityRPCException("RPC service Method: " + method.getName() + " can not support lambda except AsyncResult. ");
        }
        replaceAsyncArg(method.getParameterTypes());

    }


    public RPCInvocation(String methodName, Class<?>[] parameterTypes, Object[] arguments, boolean async) {
        this(methodName, parameterTypes, arguments, null, async);
    }


    public RPCInvocation(String methodName, Class<?>[] parameterTypes, Object[] arguments, Map<String, String> attachments, boolean async) {
        this.methodName = methodName;
        this.parameterTypes = parameterTypes == null ? new Class<?>[0] : parameterTypes;
        this.arguments = arguments == null ? new Object[0] : arguments;
        this.originalArguments = this.arguments.clone();
        this.attachments = attachments == null ? new HashMap<String, String>() : attachments;
        this.async = async;
        replaceAsyncArg(parameterTypes);
    }

    /**
     * 获取本地调用的Invocation 因为远程调用的 Arguments 可能被替换过，无法处理本地调用的参数引用
     *
     * @return
     */
    public RPCInvocation getLocalInvocation() {
        RPCInvocation invocation = new RPCInvocation();
        invocation.setMethodName(this.getMethodName());
        invocation.setParameterTypes(this.getParameterTypes());
        invocation.setAttachments(this.getAttachments());
        invocation.setArguments(this.originalArguments);
        invocation.setAsync(this.isAsync());
        return invocation;
    }

    /**
     * 替换 Async 参数
     */
    private void replaceAsyncArg(Class<?>[] parameterTypes) {
        if (parameterTypes == null) {
            return;
        }
        for (int i = 0; i < parameterTypes.length; i++) {
            if (arguments[i] instanceof AsyncResult) {
                callbacks.add((AsyncResult) arguments[i]);
                arguments[i] = new AsyncResultImpl();
            }
        }
    }

    /**
     * 检查是否含有异步回调参数
     *
     * @param args
     * @return
     */
    private boolean checkHasAsyncResultArg(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof AsyncResult) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查是否有不支持的lambda
     *
     * @param args
     * @return
     */
    private boolean checkHasNotSupportLambda(Object[] args) {
        for (Object arg : args) {
            Class<?> clazz = arg.getClass();
            Class<?> fClazz = null;
            try {
                fClazz = ClassHelper.forName(arg.getClass().getName());
            } catch (Throwable ignored) {
            }
            if (fClazz == null && StringUtils.contains(arg.getClass().getSimpleName(), "$$Lambda$")) {
                if (arg instanceof AsyncResult) {
                    continue;
                }
                System.out.println("checkHasNotSupportLambda : " + arg.getClass().getName());
                return true;
            }


        }
        return false;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes == null ? new Class<?>[0] : parameterTypes;
    }

    public Object[] getArguments() {
        return arguments;
    }

    public void setArguments(Object[] arguments) {
        this.arguments = arguments == null ? new Object[0] : arguments;
    }

    public Map<String, String> getAttachments() {
        return attachments;
    }

    public void setAttachments(Map<String, String> attachments) {
        this.attachments = attachments == null ? new HashMap<String, String>() : attachments;
    }

    public void setAttachment(String key, String value) {
        if (attachments == null) {
            attachments = new HashMap<String, String>();
        }
        attachments.put(key, value);
    }

    public void setAttachmentIfAbsent(String key, String value) {
        if (attachments == null) {
            attachments = new HashMap<String, String>();
        }
        if (!attachments.containsKey(key)) {
            attachments.put(key, value);
        }
    }

    public void addAttachments(Map<String, String> attachments) {
        if (attachments == null) {
            return;
        }
        if (this.attachments == null) {
            this.attachments = new HashMap<String, String>();
        }
        this.attachments.putAll(attachments);
    }

    public void addAttachmentsIfAbsent(Map<String, String> attachments) {
        if (attachments == null) {
            return;
        }
        for (Map.Entry<String, String> entry : attachments.entrySet()) {
            setAttachmentIfAbsent(entry.getKey(), entry.getValue());
        }
    }

    public String getAttachment(String key) {
        if (attachments == null) {
            return null;
        }
        return attachments.get(key);
    }

    public String getAttachment(String key, String defaultValue) {
        if (attachments == null) {
            return defaultValue;
        }
        String value = attachments.get(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return value;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    @Override
    public boolean isAsync() {
        return async;
    }

    public List<AsyncResult> getCallbacks() {
        return callbacks;
    }

    public void setCallbacks(List<AsyncResult> callbacks) {
        this.callbacks = callbacks;
    }

    @Override
    public String toString() {
        return "RpcInvocation [methodName=" + methodName + ", parameterTypes="
                + Arrays.toString(parameterTypes) + ", arguments=" + Arrays.toString(arguments)
                + ", attachments=" + attachments + "]";
    }

}