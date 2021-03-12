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

package com.bitactor.framework.core.rpc.api.cache;


import com.bitactor.framework.core.config.UrlProperties;
import com.bitactor.framework.core.utils.lang.StringUtils;
import com.bitactor.framework.core.rpc.api.Result;
import com.bitactor.framework.core.rpc.api.invoker.Invocation;
import com.bitactor.framework.core.rpc.api.invoker.Invoker;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author WXH
 */
public class VMCache {

    /**
     * 私有内部类实列单列对象
     */
    private static class SystemHolder {
        private static final VMCache INSTANCE = new VMCache();
    }

    /**
     * 防止外部实例化
     */
    private VMCache() {
    }

    /**
     * 获取基础系统的单列对象
     *
     * @return
     */
    public static VMCache getInstance() {
        return VMCache.SystemHolder.INSTANCE;

    }

    /**
     * 本地缓存对应服务类型id
     */
    private UrlProperties url;

    private ConcurrentHashMap<String, Invoker> services = new ConcurrentHashMap<String, Invoker>();

    /**
     * 一个可匹配services中Invoker的key 的match map
     */
    private ConcurrentHashMap<String, String> matchKeys = new ConcurrentHashMap<String, String>();

    public ConcurrentHashMap<String, Invoker> getServices() {
        return services;
    }

    public ConcurrentHashMap<String, String> getMatchKeys() {
        return matchKeys;
    }

    /**
     * RPC 暴露服务时需要绑定url 到 vm cache
     *
     * @param url 暴露服务的url
     * @throws IllegalAccessException
     */
    public void Bound(UrlProperties url) throws IllegalAccessException {
        if (url == null) {
            throw new NullPointerException("vm cache can not bound null");
        }
        if (this.url != null) {
            throw new IllegalAccessException("vm cache already bound url");
        }
        this.url = url;
    }

    public UrlProperties getUrl() {
        return url;
    }

    public Invoker getInvoker(String apiId) {
        if (!getMatchKeys().containsKey(apiId)) {
            return null;
        }
        return getServices().get(getMatchKeys().get(apiId));
    }

    /**
     * 是否存在本地可调用的对应服务类型的接口
     *
     * @param group 服务id
     * @param api        接口名
     * @return 是否可本地调用
     */
    public boolean hasLocalInvoke(String group, String api) {
        if (!isLocalAppGroup(group)) {
            return false;
        }
        if (!getMatchKeys().containsKey(api)) {
            return false;
        }
        return true;
    }

    /**
     * 本地是否有对应类型的服务
     *
     * @param group 服务类型
     * @return 是否是本地服务
     */
    public boolean isLocalAppGroup(String group) {
        if (StringUtils.isEmpty(group)) {
            return false;
        }
        if (this.getUrl() == null) {
            return false;
        }
        if (!group.equals(this.getUrl().getGroup())) {
            return false;
        }
        return true;
    }

    /**
     * 对应服务是否是本地的
     *
     * @param groupId 服务类型id
     * @return 是否是本地服务
     */
    public boolean isLocalServerTypeId(String groupId) {
        if (StringUtils.isEmpty(groupId)) {
            return false;
        }
        if (this.getUrl() == null) {
            return false;
        }
        if (!groupId.equals(this.getUrl().getGroupAndId())) {
            return false;
        }
        return true;
    }

    /**
     * 本地调用
     *
     * @param api        接口名
     * @param invocation 方法调用的数据
     * @return 方法调用接口数据
     */
    public Result localInvoke(Class api, Invocation invocation) {
        Invoker invoker = getInvoker(api.getName());
        return invoker.invoke(invocation);
    }
}
