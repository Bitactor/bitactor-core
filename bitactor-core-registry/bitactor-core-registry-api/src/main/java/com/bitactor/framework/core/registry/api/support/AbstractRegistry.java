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

package com.bitactor.framework.core.registry.api.support;


import com.bitactor.framework.core.config.UrlProperties;
import com.bitactor.framework.core.constant.CommonConstants;
import com.bitactor.framework.core.logger.Logger;
import com.bitactor.framework.core.logger.LoggerFactory;
import com.bitactor.framework.core.registry.api.Registry;
import com.bitactor.framework.core.utils.lang.ConcurrentHashSet;
import com.bitactor.framework.core.utils.lang.StringUtils;
import com.bitactor.framework.core.registry.api.NotifyListener;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * AbstractRegistry
 *
 * @author WXH
 */
public abstract class AbstractRegistry implements Registry {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    /**
     * 注册的url
     */
    private final Set<UrlProperties> registered = new ConcurrentHashSet<UrlProperties>();
    /**
     * 订阅集合 key 订阅的url,value 监听器
     */
    private final ConcurrentMap<UrlProperties, Set<NotifyListener>> subscribed = new ConcurrentHashMap<UrlProperties, Set<NotifyListener>>();
    /**
     * 订阅集合 key 订阅的url,value ：key 服务名，value 服务名下的集群
     */
    private final ConcurrentMap<UrlProperties, Map<String, List<UrlProperties>>> notified = new ConcurrentHashMap<UrlProperties, Map<String, List<UrlProperties>>>();

    /**
     * 注册的url
     */
    private UrlProperties registryUrl;


    public AbstractRegistry(UrlProperties url) {
        setUrl(url);
    }

    protected static List<UrlProperties> filterEmpty(UrlProperties url, List<UrlProperties> urls) {
        if (urls == null || urls.isEmpty()) {
            List<UrlProperties> result = new ArrayList<UrlProperties>(1);
            result.add(url.setProtocol(CommonConstants.EMPTY_PROTOCOL));
            return result;
        }
        return urls;
    }

    public UrlProperties getUrl() {
        return registryUrl;
    }

    protected void setUrl(UrlProperties url) {
        if (url == null) {
            throw new IllegalArgumentException("registry url == null");
        }
        this.registryUrl = url;
    }

    public Set<UrlProperties> getRegistered() {
        return registered;
    }

    public Map<UrlProperties, Set<NotifyListener>> getSubscribed() {
        return subscribed;
    }

    public Map<UrlProperties, Map<String, List<UrlProperties>>> getNotified() {
        return notified;
    }

    public void register(UrlProperties url) {
        if (url == null) {
            throw new IllegalArgumentException("register url == null");
        }
        if (logger.isInfoEnabled()) {
            logger.debug("Register: " + url);
        }
        registered.add(url);
        doRegister(url);
    }

    public void unregister(UrlProperties url) {
        if (url == null) {
            throw new IllegalArgumentException("unregister url == null");
        }
        if (logger.isInfoEnabled()) {
            logger.info("Unregister: " + url);
        }
        registered.remove(url);
        doUnregister(url);
    }

    public void subscribe(UrlProperties url, NotifyListener listener) {
        if (url == null) {
            throw new IllegalArgumentException("subscribe url == null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("subscribe listener == null");
        }
        if (logger.isInfoEnabled()) {
            logger.debug("Subscribe: " + url);
        }
        Set<NotifyListener> listeners = subscribed.get(url);
        if (listeners == null) {
            subscribed.putIfAbsent(url, new ConcurrentHashSet<NotifyListener>());
            listeners = subscribed.get(url);
        }
        listeners.add(listener);
        doSubscribe(url, listener);
    }

    public void unsubscribe(UrlProperties url, NotifyListener listener) {
        if (url == null) {
            throw new IllegalArgumentException("unsubscribe url == null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("unsubscribe listener == null");
        }
        if (logger.isInfoEnabled()) {
            logger.info("Unsubscribe: " + url);
        }
        Set<NotifyListener> listeners = subscribed.get(url);
        if (listeners != null) {
            listeners.remove(listener);
        }
        doUnsubscribe(url, listener);
    }


    protected void notify(UrlProperties url, NotifyListener listener, String serviceName, List<UrlProperties> urls) {
        if (url == null) {
            throw new IllegalArgumentException("notify url == null");
        }
        if (StringUtils.isEmpty(serviceName)) {
            throw new IllegalArgumentException("notify serviceName == null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("notify listener == null");
        }
        if (logger.isInfoEnabled()) {
            logger.debug("Notify urls for subscribe url: " + url + ", urls: " + urls);
        }
        Map<String, List<UrlProperties>> serviceNotified = notified.get(url);
        if (serviceNotified == null) {
            notified.putIfAbsent(url, new ConcurrentHashMap<String, List<UrlProperties>>());
            serviceNotified = notified.get(url);
        }

        List<UrlProperties> oldSubs = filedOldSub(url, serviceName, urls);
        List<UrlProperties> newAdds = filedNewAdd(url, serviceName, urls);

        listener.notifySub(oldSubs);
        listener.notifyAdd(newAdds);
        serviceNotified.put(serviceName, urls);
    }

    /**
     * 过滤出将要添加的url或更新的
     *
     * @param consumerUrl
     * @param serviceName
     * @param newUrls
     * @return
     */
    protected List<UrlProperties> filedNewAdd(UrlProperties consumerUrl, String serviceName, List<UrlProperties> newUrls) {
        List<UrlProperties> oldUrls = getOldUrls(consumerUrl,serviceName);
        return newUrls.stream().filter(v -> !oldUrls.contains(v)).collect(Collectors.toList());
    }

    /**
     * 过滤出将要删减的
     *
     * @param consumerUrl
     * @param serviceName
     * @param newUrls
     * @return
     */
    protected List<UrlProperties> filedOldSub(UrlProperties consumerUrl, String serviceName, List<UrlProperties> newUrls) {
        List<UrlProperties> oldUrls = getOldUrls(consumerUrl,serviceName);
        return oldUrls.stream().filter(v -> !newUrls.contains(v)).collect(Collectors.toList());
    }

    /**
     * 获取旧的urls
     *
     * @param consumerUrl
     * @param serviceName
     * @return
     */
    protected List<UrlProperties> getOldUrls(UrlProperties consumerUrl, String serviceName) {
        Map<String, List<UrlProperties>> serviceNotified = Optional.ofNullable(notified.get(consumerUrl)).orElse(Collections.<String, List<UrlProperties>>emptyMap());
        return Optional.ofNullable(serviceNotified.get(serviceName)).orElse(Collections.emptyList());
    }

    public void destroy() {
        if (logger.isInfoEnabled()) {
            logger.info("Destroy registry:" + getUrl());
        }
        Set<UrlProperties> destroyRegistered = new HashSet<UrlProperties>(getRegistered());
        if (!destroyRegistered.isEmpty()) {
            for (UrlProperties url : new HashSet<UrlProperties>(getRegistered())) {
                if (url.getParameter(CommonConstants.DYNAMIC_KEY, true)) {
                    try {
                        unregister(url);
                        if (logger.isInfoEnabled()) {
                            logger.info("Destroy unregister url " + url);
                        }
                    } catch (Throwable t) {
                        logger.warn("Failed to unregister url " + url + " to registry " + getUrl() + " on destroy, cause: " + t.getMessage(), t);
                    }
                }
            }
        }
        Map<UrlProperties, Set<NotifyListener>> destroySubscribed = new HashMap<UrlProperties, Set<NotifyListener>>(getSubscribed());
        if (!destroySubscribed.isEmpty()) {
            for (Map.Entry<UrlProperties, Set<NotifyListener>> entry : destroySubscribed.entrySet()) {
                UrlProperties url = entry.getKey();
                for (NotifyListener listener : entry.getValue()) {
                    try {
                        unsubscribe(url, listener);
                        if (logger.isInfoEnabled()) {
                            logger.info("Destroy unsubscribe url " + url);
                        }
                    } catch (Throwable t) {
                        logger.warn("Failed to unsubscribe url " + url + " to registry " + getUrl() + " on destroy, cause: " + t.getMessage(), t);
                    }
                }
            }
        }
    }

    public abstract void doRegister(UrlProperties url);

    public abstract void doUnregister(UrlProperties url);

    public abstract void doSubscribe(UrlProperties url, NotifyListener listener);

    public abstract void doUnsubscribe(UrlProperties url, NotifyListener listener);

    public String toString() {
        return getUrl().toString();
    }

}