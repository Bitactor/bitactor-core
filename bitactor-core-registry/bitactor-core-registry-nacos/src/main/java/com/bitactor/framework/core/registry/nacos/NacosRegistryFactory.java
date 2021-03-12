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

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.bitactor.framework.core.config.UrlProperties;
import com.bitactor.framework.core.logger.Logger;
import com.bitactor.framework.core.logger.LoggerFactory;
import com.bitactor.framework.core.registry.api.Registry;
import com.bitactor.framework.core.registry.api.support.AbstractRegistryFactory;

import java.util.Properties;

import static com.alibaba.nacos.api.PropertyKeyConst.*;
import static com.alibaba.nacos.client.naming.utils.UtilAndComs.NACOS_NAMING_LOG_NAME;
import static com.bitactor.framework.core.constant.CommonConstants.BACKUP_KEY;


/**
 * Nacos
 * @author WXH
 *
 */
public class NacosRegistryFactory extends AbstractRegistryFactory {

    private final Logger logger = LoggerFactory.getLogger(NacosRegistryFactory.class);

    @Override
    protected Registry createRegistry(UrlProperties url) {
        return new NacosRegistry(url, buildNamingService(url));
    }

    private NamingService buildNamingService(UrlProperties url) {
        Properties nacosProperties = buildNacosProperties(url);
        NamingService namingService;
        try {
            namingService = NacosFactory.createNamingService(nacosProperties);
        } catch (NacosException e) {
            if (logger.isErrorEnabled()) {
                logger.error(e.getErrMsg(), e);
            }
            throw new IllegalStateException(e);
        }
        return namingService;
    }

    private Properties buildNacosProperties(UrlProperties url) {
        Properties properties = new Properties();
        setServerAddr(url, properties);
        setProperties(url, properties);
        return properties;
    }

    private void setServerAddr(UrlProperties url, Properties properties) {
        StringBuilder serverAddrBuilder =
                new StringBuilder(url.getHost()) // Host
                        .append(":")
                        .append(url.getPort()); // Port

        // 将备份参数追加到其他服务器
        String backup = url.getParameter(BACKUP_KEY);
        if (backup != null) {
            serverAddrBuilder.append(",").append(backup);
        }

        String serverAddr = serverAddrBuilder.toString();
        properties.put(SERVER_ADDR, serverAddr);
    }

    private void setProperties(UrlProperties url, Properties properties) {
        putPropertyIfAbsent(url, properties, NAMESPACE);
        putPropertyIfAbsent(url, properties, NACOS_NAMING_LOG_NAME);
        putPropertyIfAbsent(url, properties, ENDPOINT);
        putPropertyIfAbsent(url, properties, ACCESS_KEY);
        putPropertyIfAbsent(url, properties, SECRET_KEY);
        putPropertyIfAbsent(url, properties, CLUSTER_NAME);
    }

    private void putPropertyIfAbsent(UrlProperties url, Properties properties, String propertyName) {
        String propertyValue = url.getParameter(propertyName);
        if (propertyValue != null && propertyValue.trim().length() != 0) {
            properties.setProperty(propertyName, propertyValue);
        }
    }
}
