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
import com.bitactor.framework.core.rpc.api.Export;
import com.bitactor.framework.core.rpc.api.cache.VMCache;
import com.bitactor.framework.core.utils.collection.CollectionUtils;
import com.bitactor.framework.core.logger.LoggerFactory;
import com.bitactor.framework.core.rpc.api.invoker.Invoker;
import com.bitactor.framework.core.rpc.api.proxy.JavassistProxyFactory;

import java.util.List;

/**
 * @author WXH
 */
public abstract class AbstractExport<CF> implements Export<CF> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractExport.class);

    private JavassistProxyFactory proxyFactory = new JavassistProxyFactory();
    protected UrlProperties url;

    @Override
    public UrlProperties getUrl() {
        return url;
    }

    @Override
    public <T> void addService(Class<T> service) throws Throwable {
        if (service.isInterface()) {
            throw new IllegalArgumentException("addService failed by service is not a implement class name : " + service.getName());
        }
        if (isExport()) {
            throw new IllegalAccessError("addService failed by service has export ");
        }
        Class<?>[] interfaces = service.getInterfaces();
        String apiKeys = buildClassInterfacesStr(interfaces);

        if (checkServiceHasRealize(interfaces)) {
            throw new IllegalAccessError("addService failed by service has exist implemented  interfaces:" + service.getName());
        }
        if (VMCache.getInstance().getServices().containsKey(apiKeys)) {
            return;
        }
        BoundInvokerAndKey(interfaces, apiKeys, proxyFactory.getInvoker(service.newInstance(), service, url));
    }

    @Override
    public <T> void addServiceBean(T bean) throws Throwable {
        Class<T> service = (Class<T>) bean.getClass();
        if (service.isInterface()) {
            throw new IllegalArgumentException("addService failed by service is not a implement class name : " + service.getName());
        }
        if (isExport()) {
            throw new IllegalAccessError("addService failed by service has export ");
        }
        Class<?>[] interfaces = service.getInterfaces();
        String apiKeys = buildClassInterfacesStr(interfaces);

        if (checkServiceHasRealize(interfaces)) {
            throw new IllegalAccessError("addService failed by service has exist implemented  interfaces:" + service.getName());
        }
        if (VMCache.getInstance().getServices().containsKey(apiKeys)) {
            return;
        }
        BoundInvokerAndKey(interfaces, apiKeys, proxyFactory.getInvoker(bean, service, url));
    }

    @Override
    public <T> void addServiceProxyBean(T bean, Class<T> original) throws Throwable {
        Class<T> service = original;
        if (service.isInterface()) {
            throw new IllegalArgumentException("addService failed by service is not a implement class name : " + service.getName());
        }
        if (isExport()) {
            throw new IllegalAccessError("addService failed by service has export ");
        }
        Class<?>[] interfaces = service.getInterfaces();
        String apiKeys = buildClassInterfacesStr(interfaces);

        if (checkServiceHasRealize(interfaces)) {
            throw new IllegalAccessError("addService failed by service has exist implemented  interfaces:" + service.getName());
        }
        if (VMCache.getInstance().getServices().containsKey(apiKeys)) {
            return;
        }
        BoundInvokerAndKey(interfaces, apiKeys, proxyFactory.getInvoker(bean, service, url));
    }

    @Override
    public <T> void addServices(List<Class<T>> services) throws Throwable {
        for (Class<T> service : services) {
            addService(service);
        }
    }

    @Override
    public <T> void addServicesBean(List<T> beans) throws Throwable {
        for (T bean : beans) {
            addServiceBean(bean);
        }
    }

    /**
     * 检测传入的接口中是否含有已经被添加过的
     *
     * @param interfaces 接口类数组
     * @return 是否被注册
     */
    private boolean checkServiceHasRealize(Class<?>[] interfaces) {
        for (Class<?> cl : interfaces) {
            if (VMCache.getInstance().getMatchKeys().containsKey(cl.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 绑定服务调用，后绑定的会覆盖旧的绑定服务
     *
     * @param interfaces 绑定服务的接口集
     * @param keys       绑定服务的接口集构建的Keys
     * @param invoker    调用实例
     */
    private void BoundInvokerAndKey(Class<?>[] interfaces, String keys, Invoker invoker) {
        for (Class<?> api : interfaces) {
            VMCache.getInstance().getMatchKeys().put(api.getName(), keys);
        }
        VMCache.getInstance().getServices().put(keys, invoker);
        this.url = url.setServiceInterface(CollectionUtils.getKeys(VMCache.getInstance().getMatchKeys()));
    }

    /**
     * 构建一个传入的class集的name的组合字符串，以逗号隔开
     *
     * @param classes
     * @return
     */
    private String buildClassInterfacesStr(Class<?>[] classes) {
        StringBuilder keys = new StringBuilder();
        for (Class<?> cl : classes) {
            keys.append(cl.getName()).append(",");
        }
        if (keys.length() > 0) {
            keys.deleteCharAt(keys.length() - 1);
        }
        return keys.toString();
    }

    public void addUrl(UrlProperties url) throws IllegalAccessException {
        if (this.url != null) {
            return;
        }
        this.url = url;
        // 添加服务时需要绑定本地缓存中的当前服务id
        VMCache.getInstance().Bound(this.url);
    }

    protected Invoker getInvoker(String apiId) {
        return VMCache.getInstance().getInvoker(apiId);
    }
}
