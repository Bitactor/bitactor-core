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

package com.bitactor.framework.core.rpc.api;

import com.bitactor.framework.core.config.UrlProperties;

import java.util.List;

/**
 * @author WXH
 */
public interface Export extends ListenerAssist {
    <T> void addService(Class<T> service) throws Throwable;

    <T> void addServices(List<Class<T>> services) throws Throwable;

    <T> void addServiceBean(T bean) throws Throwable;

    <T> void addServiceProxyBean(T bean, Class<T> original) throws Throwable;

    <T> void addServicesBean(List<T> bean) throws Throwable;

    void exportLocal() throws Throwable;

    void shutdown();

    boolean isExport();

    UrlProperties getUrl();
}
