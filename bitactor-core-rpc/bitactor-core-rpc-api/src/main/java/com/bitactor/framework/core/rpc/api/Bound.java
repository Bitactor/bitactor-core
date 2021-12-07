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
import com.bitactor.framework.core.rpc.api.filter.Filter;
import com.bitactor.framework.core.rpc.api.router.RouterAdapter;

import java.util.List;

/**
 * @author WXH
 */
public interface Bound<CF> extends ListenerAssist<CF> {
    public void addRouterAdapter(RouterAdapter<CF> routerAdapter);

    public void addRouterAdapter(String routerAdapterStr) throws Throwable;

    public boolean addUrl(UrlProperties url) throws Throwable;

    public <T> T get(Class<T> api) throws Throwable;

    public void addFilter(Filter filter);

    public void addStrFilter(String filterStr) throws Throwable;

    public void addFilters(List<Filter> filters);

    public void addStrFilters(List<String> filters) throws Throwable;
}
