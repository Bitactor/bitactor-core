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

package com.bitactor.framework.core.rpc.netty;


import com.bitactor.framework.core.config.UrlProperties;
import com.bitactor.framework.core.exception.IllegalityRPCException;
import com.bitactor.framework.core.exception.NoMatchApiException;
import com.bitactor.framework.core.exception.NoMatchServerException;
import com.bitactor.framework.core.logger.Logger;
import com.bitactor.framework.core.logger.LoggerFactory;
import com.bitactor.framework.core.net.api.Channel;
import com.bitactor.framework.core.net.api.ChannelContext;
import com.bitactor.framework.core.net.api.transport.AbstractClient;
import com.bitactor.framework.core.net.api.transport.message.MessageWrapper;
import com.bitactor.framework.core.net.netty.channel.NettyChannel;
import com.bitactor.framework.core.net.netty.channel.NettyChannelContext;
import com.bitactor.framework.core.net.netty.client.NettyModeClient;
import com.bitactor.framework.core.rpc.api.RPCRequest;
import com.bitactor.framework.core.rpc.api.RPCResponse;
import com.bitactor.framework.core.rpc.api.annotation.Broadcast;
import com.bitactor.framework.core.rpc.api.annotation.NoWaitReturn;
import com.bitactor.framework.core.rpc.api.cache.VMCache;
import com.bitactor.framework.core.rpc.api.invoker.AbstractInvokerHandler;
import com.bitactor.framework.core.rpc.api.invoker.RPCInvocation;
import com.bitactor.framework.core.rpc.api.support.AbstractBound;
import com.bitactor.framework.core.rpc.api.support.ModeClients;
import com.bitactor.framework.core.rpc.netty.codec.MessageRPCResponse;
import com.bitactor.framework.core.rpc.netty.consumer.ConsumerListener;
import com.bitactor.framework.core.rpc.netty.future.RPCSender;
import com.bitactor.framework.core.rpc.netty.future.RequestRPCFuture;
import com.bitactor.framework.core.utils.collection.CollectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author WXH
 */
public abstract class ConsumerBound extends AbstractBound {
    private static final Logger logger = LoggerFactory.getLogger(ConsumerBound.class);

    private final Lock getAllServerLock = new ReentrantLock();
    private final Lock getAssignServerLock = new ReentrantLock();

    public ConsumerBound(String serverName) {
        super(serverName);
    }


    public boolean addUrl(UrlProperties url) throws Throwable {
        if (!checkUpdateUrl(url)) {
            return false;
        }
        // 判断当前url的实例是否是本地服务，若是远程服务则添加新的连接 并请更新
        if (!VMCache.getInstance().isLocalServerTypeId(url.getGroupAndId()) && !isActive(url.getGroupAndId())) {
            AbstractClient nettyClient = new NettyModeClient(new ConsumerListener(this), url);
            ModeClients modeClients = new ModeClients(url, () -> {
                return new NettyModeClient(new ConsumerListener(this), url);
            });
            clients.put(url.getGroupAndId(), modeClients);
        }
        apiNames.addAll(url.getServiceInterface());
        return true;
    }

    /**
     * 是否存在该api
     *
     * @param api
     * @return
     */
    public boolean existApi(Class<?> api) {
        try {
            if (get(api) != null) {
                return true;
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return false;
        }
        return false;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> T get(Class<T> api) throws Throwable {
        if (proxyObjs.containsKey(api)) {
            return (T) proxyObjs.get(api);
        }
        getAllServerLock.lock();
        try {
            if (!proxyObjs.containsKey(api)) {
                reference(api);
            }
            return (T) proxyObjs.get(api);
        } finally {
            getAllServerLock.unlock();
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> T get(String groupAndId, Class<T> api) throws Throwable {
        if (!clients.containsKey(groupAndId) && !VMCache.getInstance().isLocalServerTypeId(groupAndId)) {
            throw new NoMatchServerException("cat not find server by : " + groupAndId);
        }
        ConcurrentHashMap<Class, Object> proxyMap = proxyServerObjs.get(groupAndId);
        if (proxyMap == null) {
            proxyMap = new ConcurrentHashMap<Class, Object>();
            ConcurrentHashMap<Class, Object> puts = proxyServerObjs.putIfAbsent(groupAndId, proxyMap);
            if (puts != null) {
                proxyMap = puts;
            }
        }
        if (proxyMap.containsKey(api)) {
            return (T) proxyMap.get(api);
        }
        getAssignServerLock.lock();
        try {
            if (!proxyMap.containsKey(api)) {
                reference(groupAndId, api);
            }
            return (T) proxyMap.get(api);
        } finally {
            getAssignServerLock.unlock();
        }
    }

    private <T> void reference(final Class<T> api) throws Throwable {
        reference(null, api);
    }

    private <T> void reference(final String groupAndId, final Class<T> api) throws Throwable {
        if (apiNames.contains(api.getName())) {
            T obj = null;
            if (groupAndId == null) {
                // groupAndId == null 时表示不指定具体的提供者的服务
                obj = proxyFactory.getProxy(new AbstractInvokerHandler<T>(api) {
                    @Override
                    protected Object doInvoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
                        Method originalMethod = getInterface().getMethod(thisMethod.getName(), thisMethod.getParameterTypes());
                        RPCInvocation invocation = new RPCInvocation(originalMethod, args);
                        RPCRequest request = new RPCRequest(getInterface().getName(), getAppGroup(), invocation);
                        Object result = null;
                        if (originalMethod.getAnnotation(Broadcast.class) != null) {
                            result = invokeBroadcast(self, thisMethod, proceed, args, invocation, request);
                        } else {
                            if (hasLocalServer()) {
                                //本地调用
                                result = invokeLocal(getInterface(), invocation.getLocalInvocation());
                            } else {
                                Channel channel = routerAdapter.routerAdapter(getActivityClients(), request);
                                result = invokeRPC(self, proceed, originalMethod, request, channel);
                            }
                        }
                        return result;
                    }

                    // 广播调用
                    private Object invokeBroadcast(Object self, Method thisMethod, Method proceed, Object[] args, RPCInvocation invocation, RPCRequest request) throws IllegalAccessException, InvocationTargetException {
                        Object result;// 广播
                        if ((thisMethod.getReturnType() == Void.class || thisMethod.getReturnType() == void.class)) {
                            throw new IllegalityRPCException("Broadcast Method: " + thisMethod.getName() + " must be void. ");
                        }
                        //远程调用
                        for (AbstractClient client : getActivityClients()) {
                            try {//FutureRPCRequest future = new FutureRPCRequest(0, client.getChannel(), request);
                                RPCSender.send(client.getChannel(), request);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        // 本地调用一次
                        if (VMCache.getInstance().isLocalAppGroup(getAppGroup())) {
                            invokeLocal(getInterface(), invocation.getLocalInvocation());
                        }
                        result = proceed.invoke(self, args);
                        return result;
                    }
                });
                proxyObjs.put(api, obj);
            } else {
                //groupAndId != null 时表示指定对应服务，指定服务时 Broadcast 注解将无效,广播消息将只请求到指定服务上
                obj = proxyFactory.getProxy(new AbstractInvokerHandler<T>(api, groupAndId) {
                    @Override
                    protected Object doInvoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
                        Method originalMethod = getInterface().getMethod(thisMethod.getName(), thisMethod.getParameterTypes());
                        RPCInvocation invocation = new RPCInvocation(originalMethod, args);
                        RPCRequest request = new RPCRequest(getInterface().getName(), getAppGroup(), invocation);
                        Object result = null;
                        // 如果指定服务时本地服务则本地调用
                        if (VMCache.getInstance().isLocalServerTypeId(getTempStr())) {
                            //本地调用
                            result = invokeLocal(getInterface(), invocation.getLocalInvocation());
                        } else {
                            //远程调用
                            Channel channel = clients.get(getTempStr()).getChannel();
                            result = invokeRPC(self, proceed, originalMethod, request, channel);
                        }
                        return result;
                    }
                });
                proxyServerObjs.get(groupAndId).put(api, obj);
            }
        } else {
            throw new NoMatchApiException("can not match api,want reference :" + api.getName() + "service apis : " + apiNames);
        }
    }


    private Object invokeRPC(Object self, Method proceed, Method originalMethod, RPCRequest request, Channel channel) throws Throwable {
        Object result = null;
        //远程调用
        doFilterBefore(request);
        RPCResponse response = null;
        if (originalMethod.getAnnotation(NoWaitReturn.class) != null || originalMethod.getAnnotation(Broadcast.class) != null) {
            //不等待响应
            RPCSender.send(channel, request);
            //调用本地代理方法，让其返回默认的返回值
            result = proceed.invoke(self, request.getInvocation().getArguments());
        } else if (request.getInvocation().isAsync()) {
            //不等待响应
            RequestRPCFuture<RPCResponse> future = RPCSender.async(channel, request);
            future.whenComplete((resp, cause) -> {
                if (resp != null) {
                    try {
                        doFilterAfter(request, resp);
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                } else if (Objects.nonNull(cause)) {
                    cause.printStackTrace();
                }
            });
            result = proceed.invoke(self, request.getInvocation().getArguments());
        } else {
            //等待响应
            response = RPCSender.sync(channel, request);
            result = response.getResult().getValue();
            doFilterAfter(request, response);
        }
        return result;
    }

    @Override
    public Channel buildChannel(ChannelContext channelContext) {
        return new NettyChannel((NettyChannelContext) channelContext) {
            @Override
            public void onReceived(MessageWrapper message) {
                if (message instanceof MessageRPCResponse) {
                    RPCSender.received(((MessageRPCResponse) message).getResponse(), this);
                }
            }

            @Override
            public void onActivity() {

            }

            @Override
            public void onDestroy() {
                ModeClients client = clients.remove(getUrl().getGroupAndId());
                if (client != null && client.isActive()) {
                    logger.warn("will close channel by groupAndId : " + getUrl().getGroupAndId());
                    client.close();
                    shutdownNotify(client);
                }
            }
        };

    }

    @Override
    public void activityChannel(Channel channel) {
        // do nothing
    }


    @Override
    public boolean hasActivityChannel() {
        if (CollectionUtils.isEmpty(clients)) {
            return false;
        }
        for (ModeClients client : clients.values()) {
            if (client.isActive()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void shutdown() {
        for (ModeClients client : clients.values()) {
            client.close();
        }
        // RPCSender shutdown
        RPCSender.shutdown();
    }
}
