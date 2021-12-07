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

package com.bitactor.framework.core.rpc.api.support;


import com.bitactor.framework.core.config.UrlProperties;
import com.bitactor.framework.core.logger.Logger;
import com.bitactor.framework.core.rpc.api.Bound;
import com.bitactor.framework.core.rpc.api.RPCRequest;
import com.bitactor.framework.core.rpc.api.cache.VMCache;
import com.bitactor.framework.core.rpc.api.invoker.Invocation;
import com.bitactor.framework.core.rpc.api.router.PollingRouterAdapter;
import com.bitactor.framework.core.rpc.api.router.RouterAdapter;
import com.bitactor.framework.core.utils.assist.UrlPropertiesUtils;
import com.bitactor.framework.core.utils.collection.CollectionUtils;
import com.bitactor.framework.core.utils.lang.StringUtils;
import com.bitactor.framework.core.net.api.transport.AbstractClient;
import com.bitactor.framework.core.logger.LoggerFactory;
import com.bitactor.framework.core.rpc.api.RPCResponse;
import com.bitactor.framework.core.rpc.api.filter.Filter;
import com.bitactor.framework.core.rpc.api.proxy.JavassistProxyFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author WXH
 */
public abstract class AbstractBound<CF> implements Bound<CF> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractBound.class);
    protected ConcurrentMap<String, ModeClients<CF>> clients = new ConcurrentHashMap<>();
    protected JavassistProxyFactory proxyFactory = new JavassistProxyFactory();
    protected ConcurrentMap<Class, Object> proxyObjs = new ConcurrentHashMap<Class, Object>();
    protected ConcurrentHashMap<String, ConcurrentHashMap<Class, Object>> proxyServerObjs = new ConcurrentHashMap<String, ConcurrentHashMap<Class, Object>>();
    protected Set<String> apiNames = new HashSet<String>();
    protected RouterAdapter<CF> routerAdapter = new PollingRouterAdapter<CF>();
    private List<Filter> filters = new ArrayList<Filter>();
    private final String appGroup;

    public AbstractBound(String appGroup) {
        this.appGroup = appGroup;
    }

    public String getAppGroup() {
        return appGroup;
    }

    protected boolean checkUpdateUrl(UrlProperties url) {
        ModeClients<CF> client = clients.get(url.getGroupAndId());
        if (client != null && client.isActive()) {
            if (!UrlPropertiesUtils.isMatch(url, client.getUrl())) {
                client.close();
                logger.warn("The url has old connect,will connect new groupAndId: " + url.getGroupAndId());
                return true;
            }
            return false;
        }
        return true;
    }

    @Override
    public void addRouterAdapter(RouterAdapter<CF> routerAdapter) {
        if (routerAdapter == null) {
            throw new NullPointerException("Null routerAdapter cannot be added");
        }
        this.routerAdapter = routerAdapter;
    }

    @Override
    public void addRouterAdapter(String routerAdapterStr) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if (StringUtils.isEmpty(routerAdapterStr)) {
            throw new NullPointerException("Null routerAdapter cannot be added");
        }
        this.routerAdapter = (RouterAdapter<CF>) Class.forName(routerAdapterStr).newInstance();
    }

    @Override
    public void addFilter(Filter filter) {
        if (filter == null) {
            return;
        }
        this.filters.add(filter);
    }

    @Override
    public void addStrFilter(String filterStr) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if (StringUtils.isEmpty(filterStr)) {
            return;
        }
        Filter filter = (Filter) Class.forName(filterStr).newInstance();
        this.filters.add(filter);
    }

    @Override
    public void addFilters(List<Filter> filters) {
        if (CollectionUtils.isEmpty(filters)) {
            return;
        }
        this.filters.addAll(filters);
    }

    @Override
    public void addStrFilters(List<String> filters) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if (CollectionUtils.isEmpty(filters)) {
            return;
        }
        for (String filterStr : filters) {
            Filter filter = (Filter) Class.forName(filterStr).newInstance();
            this.filters.add(filter);
        }
    }

    @Override
    public void shutdownNotify() {
        // do nothing
    }

    protected abstract void shutdownNotify(ModeClients<CF> client);

    /**
     * 判断传入的接口是否是本地实现了的接口
     *
     * @return 是否是本地实现了的接口
     */
    protected boolean hasLocalServer() {
        return VMCache.getInstance().isLocalAppGroup(getAppGroup());
    }


    protected void doFilterAfter(RPCRequest request, RPCResponse response) throws Throwable {
        for (Filter filter : filters) {
            filter.doFilterAfter(request, response);
        }
    }

    protected void doFilterBefore(RPCRequest request) throws Throwable {
        for (Filter filter : filters) {
            filter.doFilterBefore(request);
        }
    }

    /**
     * 获取连接正常的client的list
     *
     * @return
     */
    protected List<AbstractClient<CF>> getActivityClients() {
        List<AbstractClient<CF>> activityClients = new ArrayList<>();
        for (ModeClients<CF> client : clients.values()) {
            if (client.isActive()) {
                activityClients.add(client.getClient());
            }
        }
        return activityClients;
    }

    /**
     * 本地调用
     *
     * @param api        接口名
     * @param invocation 方法调用的数据
     * @return 方法调用接口数据
     */
    protected Object invokeLocal(Class api, Invocation invocation) {
        return VMCache.getInstance().localInvoke(api, invocation).getValue();
    }

    /**
     * 对应的client 是否活跃
     *
     * @param groupAndId
     * @return
     */
    public boolean isActive(String groupAndId) {
        ModeClients<CF> client = clients.get(groupAndId);
        if (client == null) {
            return false;
        }
        if (!client.isActive()) {
            return false;
        }
        return true;
    }

    /**
     * 关闭指定客户端的通道
     *
     * @param groupAndId
     */
    public boolean closeClient(String groupAndId) {
        ModeClients<CF> client = clients.remove(groupAndId);
        if (client == null) {
            return false;
        }
        if (!client.isActive()) {
            return false;
        }
        client.close();
        return true;
    }

    public abstract boolean hasActivityChannel();

    public abstract void shutdown();
}
