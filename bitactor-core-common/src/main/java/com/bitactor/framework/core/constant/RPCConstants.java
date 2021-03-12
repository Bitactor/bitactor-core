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

package com.bitactor.framework.core.constant;

/**
 * @author WXH
 */
public class RPCConstants {


    public static final String DEFAULT_UNDEFINED_VALUE = "undefined";

    public static final String SERVER_DEFINITION_KEY = "server.definition";
    //对外连接器服务定义类型
    public static final String SERVER_DEFINITION_CONNECTOR = "connector";
    //后端提供者服务定义类型（RPC）
    public static final String SERVER_DEFINITION_BACKEND_PROVIDER = "backend_provider";
    //后端消费者服务定义类型（RPC）
    public static final String SERVER_DEFINITION_BACKEND_CONSUMER = "backend_consumer";


    public static final String INTERFACE_KEY = "interface";

    public static final String DEFAULT_RPC_CODEC = "com.bitactor.framework.core.rpc.netty.codec.NettyRPCCodec";

    public static final String DEFAULT_FRONT_CODEC = "com.bitactor.framework.core.spring.support.codec.NettyFrontCodec";

    public static final String ROUTER_ADAPTER_KEY = "router.adapter";

    public static final String DEFAULT_ROUTER_ADAPTER = "com.bitactor.framework.core.rpc.api.router.PollingRouterAdapter";

    public static final String COPY_RPC_PARAMETERS_FILTER_KEY = "copy.rpc.parameters.filter";

    public static final boolean DEFAULT_RPC_COPY_PARAMETERS_FILTER = true;
    //自定义rpc过滤器key
    public static final String CUSTOM_RPC_FILTER_KEY = "custom.rpc.filter";

    public static final String DEFAULT_CUSTOM_RPC_FILTER = "[]";

    public static final String NACOS_REGISTRY = "nacos";

    public static final String DEFAULT_REGISTRY = NACOS_REGISTRY;

    public static final String SIDE_KEY = "side";

    public static final String PROVIDER_SIDE = "provider";

    public static final String CONSUMER_SIDE = "consumer";
    public static final String PROVIDER_PROTOCOL = "provider";

    public static final String CONSUMER_PROTOCOL = "consumer";

    public static final String REGISTRY_ROOT_KEY = "registry.root";

    public static final String CATEGORY_KEY = "category";

    public static final String PROVIDERS_CATEGORY = "providers";

    public static final String CONSUMERS_CATEGORY = "consumers";

    public static final String DEFAULT_CATEGORY = CONSUMERS_CATEGORY;

    public static final String CONSUMERS_CHANNEL_SIZE_KEY = "consumers.channel.size";

    public static final int DEFAULT_CONSUMERS_CHANNEL_SIZE = 1;
}
