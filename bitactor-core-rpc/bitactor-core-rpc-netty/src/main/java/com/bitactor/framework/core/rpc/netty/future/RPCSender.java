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

package com.bitactor.framework.core.rpc.netty.future;

import com.bitactor.framework.core.constant.NetConstants;
import com.bitactor.framework.core.logger.Logger;
import com.bitactor.framework.core.logger.LoggerFactory;
import com.bitactor.framework.core.net.api.Channel;
import com.bitactor.framework.core.rpc.api.RPCRequest;
import com.bitactor.framework.core.rpc.api.RPCResponse;
import com.bitactor.framework.core.rpc.api.async.AsyncResult;
import com.bitactor.framework.core.rpc.api.async.AsyncResultImpl;
import com.bitactor.framework.core.rpc.netty.codec.MessageRPCRequest;
import com.bitactor.framework.core.threadpool.NamedThreadFactory;
import io.netty.channel.ChannelFuture;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * @author WXH
 */
public class RPCSender {
    private static final Logger logger = LoggerFactory.getLogger(RPCSender.class);
    private static final Map<Long, RequestRPCFuture<RPCResponse>> FUTURES = new ConcurrentHashMap<>();

    private static final ScheduledExecutorService TIMEOUT_SERVICE = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("RPC-Timeout", true));
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(NetConstants.DEFAULT_THREADS, new NamedThreadFactory("RPC-Async-callback-Pool", true));

    /**
     * 同步请求
     *
     * @param channel
     * @param request
     * @return
     * @throws Exception
     */
    public static RPCResponse sync(Channel<ChannelFuture> channel, RPCRequest request) throws Exception {
        return sync(channel, request, 0);
    }

    /**
     * 同步请求
     *
     * @param channel
     * @param request
     * @param timeout
     * @return
     * @throws Exception
     */
    public static RPCResponse sync(Channel<ChannelFuture> channel, RPCRequest request, long timeout) throws Exception {
        timeout = timeout > 0 ? timeout : channel.getUrl().getPositiveParameter(NetConstants.TIMEOUT_KEY, NetConstants.DEFAULT_TIMEOUT);
        RequestRPCFuture<RPCResponse> future = async(channel, request, timeout);
        return future.get(timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * 异步请求
     *
     * @param channel
     * @param request
     * @return
     */
    public static RequestRPCFuture<RPCResponse> async(Channel<ChannelFuture> channel, RPCRequest request) {
        return async(channel, request, 0);
    }

    /**
     * 异步请求
     *
     * @param channel
     * @param request
     * @param timeout
     * @return
     */
    public static RequestRPCFuture<RPCResponse> async(Channel<ChannelFuture> channel, RPCRequest request, long timeout) {
        final long finalTimeout = timeout > 0 ? timeout : channel.getUrl().getPositiveParameter(NetConstants.TIMEOUT_KEY, NetConstants.DEFAULT_TIMEOUT);
        RequestRPCFuture<RPCResponse> future = new RequestRPCFuture<>(request);
        FUTURES.put(request.getReqId(), future);
        send(channel, request);
        TIMEOUT_SERVICE.schedule(() -> {
            if (Objects.nonNull(FUTURES.remove(request.getReqId()))) {
                future.completeExceptionally(new TimeoutException("rpc request failed by timeout, id:" + request.getReqId() + "api: " + request.getApiId() + " timeout :" + finalTimeout + " ms"));
            }
        }, finalTimeout, TimeUnit.MILLISECONDS);
        return future;
    }

    /**
     * 仅发送消息
     *
     * @param channel
     * @param request
     * @return
     */
    public static void send(Channel<ChannelFuture> channel, RPCRequest request) {
        channel.send(new MessageRPCRequest(request));
    }

    public static void received(RPCResponse response, Channel<ChannelFuture> channel) {
        RequestRPCFuture<RPCResponse> future = FUTURES.remove(response.getRequest().getReqId());
        if (future != null) {
            EXECUTOR_SERVICE.execute(() -> {
                if (future.getRequest().getInvocation().isAsync()) {
                    List<AsyncResult> callbacks = future.getRequest().getInvocation().getCallbacks();
                    int offset = 0;
                    for (Object argument : response.getRequest().getInvocation().getArguments()) {
                        if (argument instanceof AsyncResultImpl) {
                            AsyncResultImpl proxy = (AsyncResultImpl) argument;
                            AsyncResult callback = callbacks.get(offset);
                            callback.callback(proxy.getArgs(), proxy.getCause());
                            offset++;
                        }
                    }
                }
                future.complete(response);
            });
        } else {
            RPCRequest request = response.getRequest();
            logger.warn("The timeout response" + " id:" + request.getReqId() + "api: " + request.getApiId() + " finally returned at "
                    + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()))
                    + ", response " + response
                    + (channel == null ? "" : ", channel: " + channel.getLocalAddress()
                    + " -> " + channel.getRemoteAddress()));
        }

    }

    public static void shutdown() {
        TIMEOUT_SERVICE.shutdown();
        EXECUTOR_SERVICE.shutdown();
    }
}
