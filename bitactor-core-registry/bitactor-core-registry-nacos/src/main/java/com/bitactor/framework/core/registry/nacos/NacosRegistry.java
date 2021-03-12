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

package com.bitactor.framework.core.registry.nacos;


import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import com.bitactor.framework.core.config.UrlProperties;
import com.bitactor.framework.core.exception.SameProviderException;
import com.bitactor.framework.core.logger.Logger;
import com.bitactor.framework.core.logger.LoggerFactory;
import com.bitactor.framework.core.registry.api.NotifyListener;
import com.bitactor.framework.core.registry.api.support.AbstractRegistry;
import com.bitactor.framework.core.utils.assist.UrlPropertiesUtils;
import com.bitactor.framework.core.utils.lang.StringUtils;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.alibaba.nacos.api.common.Constants.DEFAULT_GROUP;
import static com.bitactor.framework.core.constant.CommonConstants.*;
import static com.bitactor.framework.core.constant.RPCConstants.*;
import static com.bitactor.framework.core.registry.nacos.NacosServiceName.*;


/**
 * Nacos
 *
 * @author WXH
 */
public class NacosRegistry extends AbstractRegistry {


    /**
     * 所有支持的 categories
     */
    private static final List<String> ALL_SUPPORTED_CATEGORIES = Arrays.asList(
            PROVIDERS_CATEGORY,
            CONSUMERS_CATEGORY
    );

    /**
     * 重试次数
     */
    private static final int RETRY_COUNT = 6;
    /**
     * 重试间隔时间
     */
    private static final int RETRY_INTERVAL_MS = 10000;
    /**
     * 服务名称的分隔符
     */
    private static final String SERVICE_NAME_SEPARATOR = System.getProperty("nacos.service.name.separator", ":");

    /**
     * 查找Nacos服务名称的秒间隔
     */
    private static final long LOOKUP_INTERVAL = Long.getLong("nacos.service.names.lookup.interval", 30);

    /**
     * {@link ScheduledExecutorService} 查找Nacos服务名称 的定时任务执行器
     */
    private volatile ScheduledExecutorService scheduledExecutorService;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final NamingService namingService;

    private final ConcurrentMap<String, EventListener> nacosListeners;

    public NacosRegistry(UrlProperties url, NamingService namingService) {
        super(url);
        this.namingService = namingService;
        this.nacosListeners = new ConcurrentHashMap<>();
    }

    @Override
    public boolean isAvailable() {
        return "UP".equals(namingService.getServerStatus());
    }

    @Override
    public void doRegister(UrlProperties url) {
        final String serviceName = getServiceName(url);
        final String group = getGroup();
        final Instance instance = createInstance(url);

        checkHasSameTypeIdProvider(serviceName, group, instance);
        execute(namingService -> namingService.registerInstance(serviceName, group, instance));
    }

    /**
     * 检查是否存在 已经注册了的和当前相同的通知
     *
     * @param serviceName
     * @param group
     * @param instance
     */
    private void checkHasSameTypeIdProvider(String serviceName, String group, Instance instance) {
        if (!PROVIDERS_CATEGORY.equals(instance.getMetadata().get(CATEGORY_KEY))) {
            return;
        }
        String registerAppGroup = instance.getMetadata().get(GROUP_KEY);
        String registerAppId = instance.getMetadata().get(APP_ID_KEY);
        AtomicBoolean hasSame = new AtomicBoolean(true);
        AtomicInteger retryCount = new AtomicInteger(0);
        while (hasSame.get() && retryCount.intValue() < RETRY_COUNT) {
            retryCount.incrementAndGet();
            execute(namingService -> {
                List<Instance> allInstances = namingService.getAllInstances(serviceName, group);
                if (allInstances.stream().noneMatch(v -> {
                    String appGroup = v.getMetadata().get(GROUP_KEY);
                    String appId = v.getMetadata().get(APP_ID_KEY);
                    return registerAppGroup.equals(appGroup) && registerAppId.equals(appId);
                })) {
                    hasSame.set(false);
                } else {
                    logger.warn("Find same server name&id  group:\"" + registerAppGroup + "\" id: \"" + registerAppId + "\" remaining attempts: " + (RETRY_COUNT - retryCount.intValue()));
                    try {
                        Thread.sleep(RETRY_INTERVAL_MS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        if (hasSame.get()) {
            throw new SameProviderException();
        }
    }

    @Override
    public void doUnregister(final UrlProperties url) {
        execute(namingService -> {
            String serviceName = getServiceName(url);
            String group = getGroup();
            Instance instance = createInstance(url);
            namingService.deregisterInstance(serviceName, group, instance);
        });
    }

    @Override
    public void doSubscribe(final UrlProperties url, final NotifyListener listener) {
        startSubscribe(url, listener);
    }

    @Override
    public void doUnsubscribe(UrlProperties url, NotifyListener listener) {
        shutdownServiceNamesLookup();
    }

    private void doSubscribe(final UrlProperties url, final NotifyListener listener, final Set<String> serviceNames) {
        execute(namingService -> {
            for (String serviceName : serviceNames) {
                subscribeEventListener(serviceName, url, listener);
            }
        });
    }


    private void shutdownServiceNamesLookup() {
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdown();
        }
    }

    /**
     * 启动定时任务更新订阅的服务名称
     *
     * @param url
     * @param listener
     */
    private void startSubscribe(UrlProperties url, NotifyListener listener) {
        scheduleServiceNamesLookup(url, listener);
    }

    /**
     * 定时任务更新订阅的服务名称
     *
     * @param url
     * @param listener
     */
    private void scheduleServiceNamesLookup(final UrlProperties url, final NotifyListener listener) {
        if (scheduledExecutorService == null) {
            scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            scheduledExecutorService.scheduleAtFixedRate(() -> {
                Set<String> serviceNames = getProviderAndFilterOther(url);
                doSubscribe(url, listener, serviceNames);
            }, 0, LOOKUP_INTERVAL, TimeUnit.SECONDS);
        }
    }

    /**
     * 获取服务提供者并过滤其他非提供者的服务名
     *
     * @return
     */
    private Set<String> getProviderAndFilterOther(UrlProperties url) {
        Set<String> serviceNames = getAllServiceNames();
        filterData(serviceNames, serviceName -> {
            boolean accepted = false;
            String prefix = PROVIDERS_CATEGORY + SERVICE_NAME_SEPARATOR;
            if (serviceName != null && serviceName.startsWith(prefix)) {
                accepted = true;
            }
            return accepted;
        });
        filterServiceNames(serviceNames, url);
        return serviceNames;
    }

    /**
     * 获取所有的服务名
     *
     * @return
     */
    private Set<String> getAllServiceNames() {
        final Set<String> serviceNames = new LinkedHashSet<>();
        execute(namingService -> {
            ListView<String> listView = namingService.getServicesOfServer(1, Integer.MAX_VALUE, getGroup());
            serviceNames.addAll(listView.getData());
        });
        return serviceNames;
    }

    /**
     * 过滤掉不匹配Url的服务名
     *
     * @param serviceNames
     * @param url
     */
    private void filterServiceNames(Set<String> serviceNames, UrlProperties url) {

        final List<String> categories = getCategories(url);

        final String targetAppGroup = url.getGroup();

        final String targetVersion = url.getParameter(VERSION_KEY, "");

        final String targetNacosGroup = url.getParameter(NACOS_GROUP_KEY, DEFAULT_GROUP);

        filterData(serviceNames, serviceName -> {
            // 将服务名称分割为段
            // (required) segments[0] = category
            // (required) segments[1] = appGroup
            // (optional) segments[2] = naocsGroup
            // (optional) segments[3] = version
            String[] segments = serviceName.split(SERVICE_NAME_SEPARATOR, -1);
            int length = segments.length;
            if (length != 4) { // must present 4 segments
                return false;
            }

            String category = segments[CATEGORY_INDEX];
            if (!categories.contains(category)) { // no match category
                return false;
            }

            String group = segments[SERVER_APP_GROUP_INDEX];
            // no match  app group
            if (!WILDCARD.equals(targetAppGroup) &&
                    !StringUtils.isEquals(targetAppGroup, group)) {
                return false;
            }

            // no match service version
            String version = segments[SERVICE_VERSION_INDEX];
            if (!WILDCARD.equals(targetVersion) && !StringUtils.isEquals(targetVersion, version)) {
                return false;
            }
            // naocs group
            String nacosGroup = segments[SERVICE_NACOS_GROUP_INDEX];
            return nacosGroup == null || WILDCARD.equals(targetNacosGroup) || StringUtils.isEquals(targetNacosGroup, nacosGroup);
        });
    }

    private <T> void filterData(Collection<T> collection, NacosDataFilter<T> filter) {
        // remove if not accept
        collection.removeIf(data -> !filter.accept(data));
    }

    private List<UrlProperties> toUrlWithEmpty(UrlProperties consumerUrlProperties, Collection<Instance> instances) {
        return buildUrlProperties(consumerUrlProperties, instances);
    }

    private List<UrlProperties> buildUrlProperties(UrlProperties consumerUrlProperties, Collection<Instance> instances) {
        List<UrlProperties> urls = new LinkedList<>();
        if (instances != null && !instances.isEmpty()) {
            for (Instance instance : instances) {
                UrlProperties url = buildUrlProperties(instance);
                if (UrlPropertiesUtils.isMatch(consumerUrlProperties, url)) {
                    urls.add(url);
                }
            }
        }
        return urls;
    }

    /**
     * 调用 nacos 的订阅
     *
     * @param serviceName
     * @param url
     * @param listener
     * @throws NacosException
     */
    private void subscribeEventListener(String serviceName, final UrlProperties url, final NotifyListener listener)
            throws NacosException {
        if (!nacosListeners.containsKey(serviceName)) {
            EventListener eventListener = event -> {
                if (event instanceof NamingEvent) {
                    NamingEvent e = (NamingEvent) event;
                    logger.info("nacos EventListener change : " + serviceName + " \n" + e.getInstances());
                    notifySubscriber(url, listener, serviceName, e.getInstances());
                }
            };
            namingService.subscribe(serviceName, getGroup(), eventListener);
            nacosListeners.put(serviceName, eventListener);
        }
    }

    /**
     * 向订阅者通知健康的{@link实例 Instance}
     *
     * @param url       {@link UrlProperties}
     * @param listener  {@link NotifyListener}
     * @param instances all {@link Instance instances}
     */
    private void notifySubscriber(UrlProperties url, NotifyListener listener, String serviceName, Collection<Instance> instances) {
        List<Instance> healthyInstances = new LinkedList<>(instances);
        if (healthyInstances.size() > 0) {
            // Healthy Instances
            filterHealthyInstances(healthyInstances);
            filterMoreIdenticalServerTypeIdInstances(healthyInstances);
        }

        List<UrlProperties> urls = toUrlWithEmpty(url, healthyInstances);
        NacosRegistry.this.notify(url, listener, serviceName, urls);
    }

    /**
     * 过滤掉相同 groupAndId 的实例 保留最后注册的实例
     *
     * @param healthyInstances
     */
    private void filterMoreIdenticalServerTypeIdInstances(List<Instance> healthyInstances) {
        healthyInstances.removeIf(v -> {
            List<Instance> find = healthyInstances.stream().filter(f -> f.getMetadata().get(APPLICATION_KEY).equals(v.getMetadata().get(APPLICATION_KEY))).collect(Collectors.toList());
            boolean isRemove = false;
            long vT = Long.parseLong(v.getMetadata().get(TIMESTAMP_KEY));
            for (Instance instance : find) {
                if (Long.parseLong(instance.getMetadata().get(TIMESTAMP_KEY)) > vT) {
                    isRemove = true;
                    break;
                }
            }
            return isRemove;
        });
    }

    /**
     * 从{@link UrlProperties}获取匹配 Categories
     *
     * @param url {@link UrlProperties}s
     * @return non-null array
     */
    private List<String> getCategories(UrlProperties url) {
        return ANY_VALUE.equals(url.getGroup()) ?
                ALL_SUPPORTED_CATEGORIES : Collections.singletonList(DEFAULT_CATEGORY);
    }

    private UrlProperties buildUrlProperties(Instance instance) {
        Map<String, String> metadata = instance.getMetadata();
        String protocol = metadata.get(PROTOCOL_KEY);
        String appGroup = metadata.get(GROUP_KEY);
        return new UrlProperties(protocol, instance.getIp(), instance.getPort(), appGroup, instance.getMetadata());
    }

    private Instance createInstance(UrlProperties url) {
        String category = url.getParameter(CATEGORY_KEY, DEFAULT_CATEGORY);
        UrlProperties newUrlProperties = url.addParameter(CATEGORY_KEY, category);
        newUrlProperties = newUrlProperties.addParameter(PROTOCOL_KEY, url.getProtocol());
        newUrlProperties = newUrlProperties.addParameter(GROUP_KEY, url.getGroup());
        String ip = url.getHost();
        int port = url.getPort();
        Instance instance = new Instance();
        instance.setIp(ip);
        instance.setPort(port);
        instance.setClusterName(url.getGroup());
        instance.setMetadata(new HashMap<>(newUrlProperties.getParameters()));
        return instance;
    }

    private NacosServiceName createServiceName(UrlProperties url) {
        return valueOf(url);
    }

    private String getServiceName(UrlProperties url) {
        return new NacosServiceName(url).toString();
    }

    private String getGroup() {
        return this.getUrl().getParameter(NACOS_GROUP_KEY, DEFAULT_GROUP);
    }

    private void execute(NamingServiceCallback callback) {
        try {
            callback.callback(namingService);
        } catch (NacosException e) {
            if (logger.isErrorEnabled()) {
                logger.error(e.getErrMsg(), e);
            }
        }
    }

    private void filterHealthyInstances(Collection<Instance> instances) {
        filterData(instances, Instance::isEnabled);
    }

    /**
     * 一个用于Nacos数据的过滤器接口
     */
    private interface NacosDataFilter<T> {

        /**
         * 测试是否接受指定的数据。
         *
         * @param data 将被测试的数据
         * @return <code>true</code>当且仅当<code>data</code>
         * 应被接纳
         */
        boolean accept(T data);

    }

    /**
     * {@link NamingService} 的回调接口
     */
    interface NamingServiceCallback {

        /**
         * Callback
         *
         * @param namingService {@link NamingService}
         * @throws NacosException
         */
        void callback(NamingService namingService) throws NacosException;

    }
}
