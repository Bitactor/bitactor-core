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


import com.bitactor.framework.core.exception.NotSupportException;
import com.bitactor.framework.core.exception.RpcException;
import com.bitactor.framework.core.logger.Logger;
import com.bitactor.framework.core.logger.LoggerFactory;
import com.bitactor.framework.core.net.api.Channel;
import com.bitactor.framework.core.net.api.ChannelContext;
import com.bitactor.framework.core.net.api.transport.AbstractServer;
import com.bitactor.framework.core.net.api.transport.message.MessageWrapper;
import com.bitactor.framework.core.net.netty.channel.ChannelNettySendPolicy;
import com.bitactor.framework.core.net.netty.channel.NettyChannel;
import com.bitactor.framework.core.net.netty.channel.NettyChannelContext;
import com.bitactor.framework.core.net.netty.server.NettyModeServer;
import com.bitactor.framework.core.rpc.api.RPCRequest;
import com.bitactor.framework.core.rpc.api.RPCResponse;
import com.bitactor.framework.core.rpc.api.RPCResult;
import com.bitactor.framework.core.rpc.api.annotation.Broadcast;
import com.bitactor.framework.core.rpc.api.annotation.NoWaitReturn;
import com.bitactor.framework.core.rpc.api.async.AsyncResultImpl;
import com.bitactor.framework.core.rpc.api.invoker.Invocation;
import com.bitactor.framework.core.rpc.api.invoker.Invoker;
import com.bitactor.framework.core.rpc.api.support.AbstractExport;
import com.bitactor.framework.core.rpc.netty.codec.MessageRPCRequest;
import com.bitactor.framework.core.rpc.netty.codec.MessageRPCResponse;
import com.bitactor.framework.core.rpc.netty.provider.ProviderListener;
import com.bitactor.framework.core.utils.collection.CollectionUtils;
import io.netty.channel.ChannelFuture;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * @author WXH
 */
public class ProviderExport extends AbstractExport<ChannelFuture> {
    private static final Logger logger = LoggerFactory.getLogger(ProviderExport.class);
    private AbstractServer<ChannelFuture> server;
    private ChannelNettySendPolicy sendPolicy;

    public ProviderExport() {
    }

    public ProviderExport(ChannelNettySendPolicy sendPolicy) {
        this.sendPolicy = sendPolicy;
    }

    private boolean checkCanExport() {
        if (isExport()) {
            return false;
        }
        if (url == null) {
            return false;
        }
        return true;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void exportLocal() throws Throwable {
        if (!checkCanExport()) {
            return;
        }
        this.server = new NettyModeServer(new ProviderListener(this), url);
        this.server.threadStart().sync();
    }

    @Override
    public void shutdown() {
        if (server != null) {
            this.server.close();
        }
    }

    @Override
    public boolean isExport() {
        return (server != null && server.isStart());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Channel buildChannel(ChannelContext channelContext) {
        return new NettyChannel((NettyChannelContext) channelContext, sendPolicy) {
            @Override
            public void onReceived(MessageWrapper message) {
                if (message instanceof MessageRPCRequest) {
                    RPCRequest request = ((MessageRPCRequest) message).getRequest();
                    boolean needResp = false;
                    try {
                        Invocation invocation = request.getInvocation();
                        Class apiCls = Class.forName(request.getApiId());
                        Method originalMethod = apiCls.getMethod(invocation.getMethodName(), invocation.getParameterTypes());
                        needResp = checkIsReturn(originalMethod);
                        Invoker invoker = getInvoker(request.getApiId());
                        // @see  com.bitactor.framework.rpc.api.annotation.Async
                        if (invocation.isAsync()) {
                            doAsyncInvoker(request, invocation, invoker);
                        } else {
                            doSyncInvoker(request, invocation, originalMethod, invoker);
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                        if (!needResp) {
                            return;
                        }
                        RPCResult result = new RPCResult(new RpcException("Do Invoker exception: " + request + " url : " + getUrl().toFullString(), e));
                        this.send(new MessageRPCResponse(new RPCResponse(request, result)));
                    }
                } else {
                    //TODO 需要对非法请求做出来
                }
            }

            /**
             * 同步调用
             * @param request
             * @param invocation
             * @param originalMethod
             * @param invoker
             * @return
             */
            private void doSyncInvoker(RPCRequest request, Invocation invocation, Method originalMethod, Invoker invoker) {
                if (invoker == null) {
                    throw new NotSupportException("Can not find sync Invoker cause by [invoker] null");
                } else {
                    RPCResult result = (RPCResult) invoker.invoke(invocation);
                    // 无注解默认返回
                    if (!checkIsReturn(originalMethod)) {
                        return;
                    }
                    this.send(new MessageRPCResponse(new RPCResponse(request, result)));
                }
            }

            /**
             * 异步调用
             * @param request
             * @param invocation
             * @param invoker
             */
            private void doAsyncInvoker(RPCRequest request, Invocation invocation, Invoker invoker) {
                List<AsyncResultImpl> asyncResults = getAsyncResult(invocation.getArguments());
                List<CompletableFuture> futures = new ArrayList<>();
                // 异步回调处理
                if (!CollectionUtils.isEmpty(asyncResults)) {
                    for (AsyncResultImpl asyncResult : asyncResults) {
                        CompletableFuture<Object> future = new CompletableFuture<>();
                        futures.add(future);
                        asyncResult.setAsync((o, cause) -> {
                            // 清除 异步消费者属性，防止序列化失败
                            asyncResult.setAsync(null);
                            asyncResult.setArgs(o);
                            if (Objects.nonNull(cause)) {
                                asyncResult.setCause((Throwable) cause);
                                ((Throwable) cause).printStackTrace();
                            }
                            future.complete(o);
                        });
                    }
                }
                if (invoker == null) {
                    throw new NotSupportException("Can not find async Invoker cause by [invoker] null");
                } else {
                    // 异步调用必须要返回响应
                    RPCResult result = (RPCResult) invoker.invoke(invocation);
                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRunAsync(() -> {
                        this.send(new MessageRPCResponse(new RPCResponse(request, result)));
                    });
                }
            }

            @Override
            public void onActivity() {

            }

            private boolean checkIsReturn(Method originalMethod) {
                if (originalMethod == null) {
                    return false;
                }
                // 获取调用RPC的注解确认是否返回结果，默认返回
                NoWaitReturn noWaitReturn = originalMethod.getAnnotation(NoWaitReturn.class);
                Broadcast broadcast = originalMethod.getAnnotation(Broadcast.class);
                if (noWaitReturn != null || broadcast != null) {
                    return false;
                }
                return true;
            }

            private List<AsyncResultImpl> getAsyncResult(Object[] arguments) {
                if (Objects.isNull(arguments)) {
                    return Collections.emptyList();
                }
                List<AsyncResultImpl> list = new ArrayList<>();
                for (Object argument : arguments) {
                    if (argument instanceof AsyncResultImpl) {
                        list.add((AsyncResultImpl) argument);
                    }
                }
                return list;
            }

            @Override
            public void onDestroy() {
                // TODO 需要处理被销毁时的逻辑
            }
        };
    }

    @Override
    public void activityChannel(Channel<ChannelFuture> channel) {
        // TODO: 2019/6/27 provider 端是否需要做处理 ，待考虑
    }

    @Override
    public void shutdownNotify() {
        // do nothing
    }
}
