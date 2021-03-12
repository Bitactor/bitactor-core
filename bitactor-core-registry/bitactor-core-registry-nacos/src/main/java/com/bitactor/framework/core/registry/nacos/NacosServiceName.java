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


import com.bitactor.framework.core.config.UrlProperties;
import com.bitactor.framework.core.utils.lang.StringUtils;

import java.util.Arrays;
import java.util.Objects;

import static com.alibaba.nacos.api.common.Constants.DEFAULT_GROUP;
import static com.bitactor.framework.core.constant.CommonConstants.NACOS_GROUP_KEY;
import static com.bitactor.framework.core.constant.CommonConstants.VERSION_KEY;
import static com.bitactor.framework.core.constant.RPCConstants.CATEGORY_KEY;


/**
 * @author WXH
 */
public class NacosServiceName {

    public static final String NAME_SEPARATOR = ":";

    public static final String WILDCARD_ALL = "*:*:*";

    public static final String VALUE_SEPARATOR = ",";

    public static final String WILDCARD = "*";

    public static final String DEFAULT_PARAM_VALUE = "";

    public static final int CATEGORY_INDEX = 0;

    public static final int SERVER_APP_GROUP_INDEX = 1;

    public static final int SERVICE_NACOS_GROUP_INDEX = 2;

    public static final int SERVICE_VERSION_INDEX = 3;


    private String category;

    private String appGroup;

    private String version;

    private String nacosGroup;

    private String value;

    public NacosServiceName() {
    }

    public NacosServiceName(UrlProperties url) {
        category = url.getParameter(CATEGORY_KEY);
        appGroup = url.getGroup();
        version = url.getParameter(VERSION_KEY, DEFAULT_PARAM_VALUE);
        nacosGroup = url.getParameter(NACOS_GROUP_KEY, DEFAULT_GROUP);
        value = toValue();
    }

    public NacosServiceName(String value) {
        this.value = value;
        String[] segments = value.split(NAME_SEPARATOR, -1);
        this.category = segments[CATEGORY_INDEX];
        this.appGroup = segments[SERVER_APP_GROUP_INDEX];
        this.nacosGroup = segments[SERVICE_NACOS_GROUP_INDEX];
        this.version = segments[SERVICE_VERSION_INDEX];
    }

    /**
     * 构建{@link NacosServiceName}的实例
     *
     * @param url
     * @return
     */
    public static NacosServiceName valueOf(UrlProperties url) {
        return new NacosServiceName(url);
    }

    /**
     * 是否是指定服务类型
     *
     * @return 如果指定的服务类型，返回<code>true</code>，或<code>false</code>
     */
    public boolean isConcrete() {
        return isConcrete(appGroup) && isConcrete(version) && isConcrete(nacosGroup);
    }

    public boolean isCompatible(NacosServiceName concreteServiceName) {

        if (!concreteServiceName.isConcrete()) {
            return false;
        }
        // Not match comparison
        if (!StringUtils.isEquals(this.category, concreteServiceName.category)
                && !matchRange(this.category, concreteServiceName.category)) {
            return false;
        }
        if (!StringUtils.isEquals(this.appGroup, concreteServiceName.appGroup)) {
            return false;
        }

        // wildcard condition
        if (isWildcard(this.version)) {
            return true;
        }

        if (isWildcard(this.nacosGroup)) {
            return true;
        }

        // range condition
        if (!StringUtils.isEquals(this.version, concreteServiceName.version)
                && !matchRange(this.version, concreteServiceName.version)) {
            return false;
        }

        if (!StringUtils.isEquals(this.nacosGroup, concreteServiceName.nacosGroup) &&
                !matchRange(this.nacosGroup, concreteServiceName.nacosGroup)) {
            return false;
        }

        return true;
    }

    private boolean matchRange(String range, String value) {
        if (StringUtils.isBlank(range)) {
            return true;
        }
        if (!isRange(range)) {
            return false;
        }
        String[] values = range.split(VALUE_SEPARATOR);
        return Arrays.asList(values).contains(value);
    }

    private boolean isConcrete(String value) {
        return !isWildcard(value) && !isRange(value);
    }

    private boolean isWildcard(String value) {
        return WILDCARD.equals(value);
    }

    private boolean isRange(String value) {
        return value != null && value.contains(VALUE_SEPARATOR) && value.split(VALUE_SEPARATOR).length > 1;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAppGroup() {
        return appGroup;
    }

    public NacosServiceName setAppGroup(String appGroup) {
        this.appGroup = appGroup;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public NacosServiceName setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getNacosGroup() {
        return nacosGroup;
    }

    public NacosServiceName setNacosGroup(String nacosGroup) {
        this.nacosGroup = nacosGroup;
        return this;
    }

    public String getValue() {
        if (value == null) {
            value = toValue();
        }
        return value;
    }

    private String toValue() {
        return category +
                NAME_SEPARATOR + appGroup +
                NAME_SEPARATOR + nacosGroup +
                NAME_SEPARATOR + version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NacosServiceName)) {
            return false;
        }
        NacosServiceName that = (NacosServiceName) o;
        return Objects.equals(getValue(), that.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getValue());
    }

    @Override
    public String toString() {
        return getValue();
    }
}
