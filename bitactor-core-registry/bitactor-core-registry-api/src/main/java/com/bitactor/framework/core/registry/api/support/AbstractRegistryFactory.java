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
import com.bitactor.framework.core.logger.Logger;
import com.bitactor.framework.core.logger.LoggerFactory;
import com.bitactor.framework.core.registry.api.Registry;
import com.bitactor.framework.core.registry.api.RegistryFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * AbstractRegistryFactory
 *
 * @author WXH
 */
public abstract class AbstractRegistryFactory implements RegistryFactory {

    private static final Logger logger = LoggerFactory.getLogger(AbstractRegistryFactory.class);

    private static final ReentrantLock LOCK = new ReentrantLock();

    private static final Map<String, Registry> REGISTRIES = new ConcurrentHashMap<String, Registry>();

    /**
     * 得到所有 registries
     */
    public static Collection<Registry> getRegistries() {
        return Collections.unmodifiableCollection(REGISTRIES.values());
    }

    /**
     * 关闭所有创建的 registries
     */
    public static void destroyAll() {
        if (logger.isInfoEnabled()) {
            logger.info("Close all registries " + getRegistries());
        }
        LOCK.lock();
        try {
            for (Registry registry : getRegistries()) {
                try {
                    registry.destroy();
                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                }
            }
            REGISTRIES.clear();
        } finally {
            LOCK.unlock();
        }
    }

    public Registry getRegistry(UrlProperties url) {
        String key = url.toServiceString();
        // 锁定注册表访问进程，以确保注册表的单个实例
        LOCK.lock();
        try {
            Registry registry = REGISTRIES.get(key);
            if (registry != null) {
                return registry;
            }
            registry = createRegistry(url);
            if (registry == null) {
                throw new IllegalStateException("Can not create registry " + url);
            }
            REGISTRIES.put(key, registry);
            return registry;
        } finally {
            LOCK.unlock();
        }
    }

    protected abstract Registry createRegistry(UrlProperties url);

}