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

package com.bitactor.framework.core.config;

import com.bitactor.framework.core.constant.CommonConstants;
import com.bitactor.framework.core.constant.NetConstants;
import com.bitactor.framework.core.constant.RPCConstants;
import com.bitactor.framework.core.utils.collection.CollectionUtils;
import com.bitactor.framework.core.utils.lang.StringUtils;
import com.bitactor.framework.core.utils.net.NetUtils;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * UrlProperties属性配置
 *
 * @author WXH
 */
public final class UrlProperties implements Serializable {

    private static final long serialVersionUID = 7168720249022303225L;
    private final String protocol;

    private final String username;

    private final String password;

    private final String host;

    private final int port;

    private final String group;

    private final Map<String, String> parameters;
    // 临时缓存 不会被序列化
    private volatile transient String ip;
    private volatile transient String cacheStr;
    private volatile transient String parametersStr;
    private volatile transient String fullProperties;
    private volatile transient Map<String, Number> numbers;

    public UrlProperties() {
        this.protocol = null;
        this.username = null;
        this.password = null;
        this.host = null;
        this.port = 0;
        this.group = null;
        this.parameters = null;
    }

    public UrlProperties(String protocol, String host, int port) {
        this(protocol, null, null, host, port, null, (Map<String, String>) null);
    }

    public UrlProperties(String protocol, String host, int port, String[] pairs) {
        this(protocol, null, null, host, port, null, CollectionUtils.toStringMap(pairs));
    }

    public UrlProperties(String protocol, String host, int port, Map<String, String> parameters) {
        this(protocol, null, null, host, port, null, parameters);
    }

    public UrlProperties(String protocol, String host, int port, String group) {
        this(protocol, null, null, host, port, group, (Map<String, String>) null);
    }

    public UrlProperties(String protocol, String host, int port, String group, String... pairs) {
        this(protocol, null, null, host, port, group, CollectionUtils.toMap(pairs));
    }

    public UrlProperties(String protocol, String host, int port, String group, Map<String, String> parameters) {
        this(protocol, null, null, host, port, group, parameters);
    }

    public UrlProperties(String protocol, String username, String password, String host, int port, String group) {
        this(protocol, username, password, host, port, group, (Map<String, String>) null);
    }

    public UrlProperties(String protocol, String username, String password, String host, int port, Map<String, String> parameters) {
        this(protocol, username, password, host, port, null, (Map<String, String>) parameters);
    }

    public UrlProperties(String protocol, String username, String password, String host, int port, String group, String... pairs) {
        this(protocol, username, password, host, port, group, CollectionUtils.toMap(pairs));
    }

    public UrlProperties(String protocol, String username, String password, String host, int port, String group, Map<String, String> parameters) {
        if (StringUtils.isEmpty(username) && StringUtils.isNotEmpty(password)) {
            throw new IllegalArgumentException("Invalid properties, password without username!");
        }
        this.protocol = protocol;
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = Math.max(port, 0);
        while (group != null && group.startsWith("/")) {
            group = group.substring(1);
        }
        this.group = group;
        if (parameters == null) {
            parameters = new HashMap<String, String>();
        } else {
            parameters = new HashMap<String, String>(parameters);
        }
        this.parameters = Collections.unmodifiableMap(parameters);
    }

    /**
     * 解析 properties string
     */
    public static UrlProperties valueOf(String urlProperties) {
        urlProperties = urlProperties.trim();
        if (StringUtils.isEmpty(urlProperties)) {
            throw new IllegalArgumentException("urlProperties == null");
        }
        String protocol = null;
        String username = null;
        String password = null;
        String host = null;
        int port = 0;
        String group = null;
        Map<String, String> parameters = null;
        int i = urlProperties.indexOf("?");
        if (i >= 0) {
            String[] parts = urlProperties.substring(i + 1).split("\\&");
            parameters = new HashMap<String, String>();
            for (String part : parts) {
                part = part.trim();
                if (part.length() > 0) {
                    int j = part.indexOf('=');
                    if (j >= 0) {
                        parameters.put(part.substring(0, j), part.substring(j + 1));
                    } else {
                        parameters.put(part, part);
                    }
                }
            }
            urlProperties = urlProperties.substring(0, i);
        }
        i = urlProperties.indexOf("://");
        if (i >= 0) {
            if (i == 0) throw new IllegalStateException("url missing protocol: \"" + urlProperties + "\"");
            protocol = urlProperties.substring(0, i);
            urlProperties = urlProperties.substring(i + 3);
        } else {
            i = urlProperties.indexOf(":/");
            if (i >= 0) {
                if (i == 0) throw new IllegalStateException("url missing protocol: \"" + urlProperties + "\"");
                protocol = urlProperties.substring(0, i);
                urlProperties = urlProperties.substring(i + 1);
            }
        }

        i = urlProperties.indexOf("/");
        if (i >= 0) {
            group = urlProperties.substring(i + 1);
            urlProperties = urlProperties.substring(0, i);
        }
        i = urlProperties.indexOf("@");
        if (i >= 0) {
            username = urlProperties.substring(0, i);
            int j = username.indexOf(":");
            if (j >= 0) {
                password = username.substring(j + 1);
                username = username.substring(0, j);
            }
            urlProperties = urlProperties.substring(i + 1);
        }
        i = urlProperties.indexOf(":");
        if (i >= 0 && i < urlProperties.length() - 1) {
            port = Integer.parseInt(urlProperties.substring(i + 1));
            urlProperties = urlProperties.substring(0, i);
        }
        if (urlProperties.length() > 0) host = urlProperties;
        return new UrlProperties(protocol, username, password, host, port, group, parameters);
    }

    public String getProtocol() {
        return protocol;
    }

    public UrlProperties setProtocol(String protocol) {
        return new UrlProperties(protocol, username, password, host, port, group, getParameters());
    }

    public String getUsername() {
        return username;
    }

    public UrlProperties setUsername(String username) {
        return new UrlProperties(protocol, username, password, host, port, group, getParameters());
    }

    public String getPassword() {
        return password;
    }

    public UrlProperties setPassword(String password) {
        return new UrlProperties(protocol, username, password, host, port, group, getParameters());
    }

    public String getHost() {
        return host;
    }

    public UrlProperties setHost(String host) {
        return new UrlProperties(protocol, username, password, host, port, group, getParameters());
    }

    public UrlProperties setPort(int port) {
        return new UrlProperties(protocol, username, password, host, port, group, getParameters());
    }

    public String getIp() {
        if (ip == null) {
            ip = NetUtils.getIpByHost(host);
        }
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getGroupAndId() {
        if (StringUtils.isEmpty(getGroup()) || StringUtils.isEmpty(getAppId())) {
            return "server";
        }
        return getGroup() + "-" + getAppId();
    }

    public String getGroup() {
        return group;
    }

    public UrlProperties setGroup(String group) {
        return new UrlProperties(protocol, username, password, host, port, group, getParameters());
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public String getAppId() {
        return getParameter(CommonConstants.APP_ID_KEY, RPCConstants.DEFAULT_UNDEFINED_VALUE);
    }

    public UrlProperties setAppId(String groupId) {
        return addParameter(CommonConstants.APP_ID_KEY, groupId);
    }

    public boolean isBigEndian() {
        return getParameter(NetConstants.BYTE_ODER_BIG_ENDIAN_KEY, NetConstants.DEFAULT_BYTE_ODER_BIG);
    }


    public UrlProperties setProtocolsMode(String protocolsMode) {
        return addParameter(NetConstants.PROTOCOL_MODE_KEY, protocolsMode);
    }

    public boolean isOpenWsSsl() {
        return getParameter(NetConstants.WS_OPEN_SSL, NetConstants.DEFAULT_WS_OPEN_SSL);
    }

    public String getProtocolsMode() {
        return getParameter(NetConstants.PROTOCOL_MODE_KEY, NetConstants.DEFAULT_PROTOCOL_MODE);
    }

    public String getAddress() {
        return port <= 0 ? host : host + ":" + port;
    }

    public UrlProperties setAddress(String address) {
        int i = address.lastIndexOf(':');
        String host;
        int port = this.port;
        if (i >= 0) {
            host = address.substring(0, i);
            port = Integer.parseInt(address.substring(i + 1));
        } else {
            host = address;
        }
        return new UrlProperties(protocol, username, password, host, port, group, getParameters());
    }

    public List<String> getServiceInterface() {
        String value = getParameter(RPCConstants.INTERFACE_KEY);
        if (value == null || value.length() == 0) {
            return Collections.emptyList();
        }
        return Arrays.asList(CommonConstants.COMMA_SPLIT_PATTERN.split(value));
    }

    public UrlProperties setServiceInterface(List<String> services) {
        return addParameter(RPCConstants.INTERFACE_KEY, StringUtils.join(services, ","));
    }

    public InetSocketAddress toInetSocketAddress() {
        return new InetSocketAddress(host, port);
    }

    public UrlProperties addParameter(String key, boolean value) {
        return addParameter(key, String.valueOf(value));
    }

    public UrlProperties addParameter(String key, char value) {
        return addParameter(key, String.valueOf(value));
    }

    public UrlProperties addParameter(String key, byte value) {
        return addParameter(key, String.valueOf(value));
    }

    public UrlProperties addParameter(String key, short value) {
        return addParameter(key, String.valueOf(value));
    }

    public UrlProperties addParameter(String key, int value) {
        return addParameter(key, String.valueOf(value));
    }

    public UrlProperties addParameter(String key, long value) {
        return addParameter(key, String.valueOf(value));
    }

    public UrlProperties addParameter(String key, float value) {
        return addParameter(key, String.valueOf(value));
    }

    public UrlProperties addParameter(String key, double value) {
        return addParameter(key, String.valueOf(value));
    }

    public UrlProperties addParameter(String key, Enum<?> value) {
        if (value == null) return this;
        return addParameter(key, String.valueOf(value));
    }

    public UrlProperties addParameter(String key, Number value) {
        if (value == null) return this;
        return addParameter(key, String.valueOf(value));
    }

    public UrlProperties addParameter(String key, CharSequence value) {
        if (value == null || value.length() == 0) return this;
        return addParameter(key, String.valueOf(value));
    }

    public UrlProperties addParameter(String key, String value) {
        if (key == null || key.length() == 0
                || value == null || value.length() == 0) {
            return this;
        }
        if (value.equals(getParameters().get(key))) {
            return this;
        }

        Map<String, String> map = new HashMap<String, String>(getParameters());
        map.put(key, value);
        return new UrlProperties(protocol, username, password, host, port, group, map);
    }

    public UrlProperties addParameter(String key, Collection<String> value) {
        return addParameter(key, StringUtils.join(value, ","));
    }

    public UrlProperties addParameterIfAbsent(String key, String value) {
        if (key == null || key.length() == 0
                || value == null || value.length() == 0) {
            return this;
        }
        if (hasParameter(key)) {
            return this;
        }
        Map<String, String> map = new HashMap<String, String>(getParameters());
        map.put(key, value);
        return new UrlProperties(protocol, username, password, host, port, group, map);
    }

    public boolean hasParameter(String key) {
        String value = getParameter(key);
        return value != null && value.length() > 0;
    }

    private Map<String, Number> getNumbers() {
        if (numbers == null) {
            numbers = new ConcurrentHashMap<String, Number>();
        }
        return numbers;
    }

    public String getParameter(String key) {
        String value = parameters.get(key);
        if (value == null || value.length() == 0) {
            value = parameters.get(CommonConstants.DEFAULT_KEY_PREFIX + key);
        }
        return value;
    }

    public String getParameter(String key, String defaultValue) {
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return value;
    }

    public String[] getParameter(String key, String[] defaultValue) {
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return CommonConstants.COMMA_SPLIT_PATTERN.split(value);
    }

    public double getParameter(String key, double defaultValue) {
        Number n = getNumbers().get(key);
        if (n != null) {
            return n.doubleValue();
        }
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        double d = Double.parseDouble(value);
        getNumbers().put(key, d);
        return d;
    }

    public float getParameter(String key, float defaultValue) {
        Number n = getNumbers().get(key);
        if (n != null) {
            return n.floatValue();
        }
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        float f = Float.parseFloat(value);
        getNumbers().put(key, f);
        return f;
    }

    public long getParameter(String key, long defaultValue) {
        Number n = getNumbers().get(key);
        if (n != null) {
            return n.longValue();
        }
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        long l = Long.parseLong(value);
        getNumbers().put(key, l);
        return l;
    }

    public int getParameter(String key, int defaultValue) {
        Number n = getNumbers().get(key);
        if (n != null) {
            return n.intValue();
        }
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        int i = Integer.parseInt(value);
        getNumbers().put(key, i);
        return i;
    }

    public short getParameter(String key, short defaultValue) {
        Number n = getNumbers().get(key);
        if (n != null) {
            return n.shortValue();
        }
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        short s = Short.parseShort(value);
        getNumbers().put(key, s);
        return s;
    }

    public byte getParameter(String key, byte defaultValue) {
        Number n = getNumbers().get(key);
        if (n != null) {
            return n.byteValue();
        }
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        byte b = Byte.parseByte(value);
        getNumbers().put(key, b);
        return b;
    }

    public Float getPositiveParameter(String key, float defaultValue) {
        if (defaultValue <= 0) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        float value = getParameter(key, defaultValue);
        if (value <= 0) {
            return defaultValue;
        }
        return value;
    }

    public double getPositiveParameter(String key, double defaultValue) {
        if (defaultValue <= 0) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        double value = getParameter(key, defaultValue);
        if (value <= 0) {
            return defaultValue;
        }
        return value;
    }

    public long getPositiveParameter(String key, long defaultValue) {
        if (defaultValue <= 0) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        long value = getParameter(key, defaultValue);
        if (value <= 0) {
            return defaultValue;
        }
        return value;
    }

    public int getPositiveParameter(String key, int defaultValue) {
        if (defaultValue <= 0) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        int value = getParameter(key, defaultValue);
        if (value <= 0) {
            return defaultValue;
        }
        return value;
    }

    public short getPositiveParameter(String key, short defaultValue) {
        if (defaultValue <= 0) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        short value = getParameter(key, defaultValue);
        if (value <= 0) {
            return defaultValue;
        }
        return value;
    }

    public byte getPositiveParameter(String key, byte defaultValue) {
        if (defaultValue <= 0) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        byte value = getParameter(key, defaultValue);
        if (value <= 0) {
            return defaultValue;
        }
        return value;
    }

    public char getParameter(String key, char defaultValue) {
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return value.charAt(0);
    }

    public boolean getParameter(String key, boolean defaultValue) {
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    public String toFullString() {
        if (fullProperties != null) {
            return fullProperties;
        }
        return fullProperties = buildString(true, true);
    }

    public String toFullString(String... parameters) {
        return buildString(true, true, parameters);
    }

    public String toParameterString() {
        if (parametersStr != null) {
            return parametersStr;
        }
        return parametersStr = toParameterString(new String[0]);
    }

    private String buildString(boolean appendUser, boolean appendParameter, String... parameters) {
        return buildString(appendUser, appendParameter, false, false, parameters);
    }

    private String buildString(boolean appendUser, boolean appendParameter, boolean useIP, boolean useService, String... parameters) {
        StringBuilder buf = new StringBuilder();
        if (protocol != null && protocol.length() > 0) {
            buf.append(protocol);
            buf.append("://");
        }
        if (appendUser && username != null && username.length() > 0) {
            buf.append(username);
            if (password != null && password.length() > 0) {
                buf.append(":");
                buf.append(password);
            }
            buf.append("@");
        }
        String host;
        if (useIP) {
            host = getIp();
        } else {
            host = getHost();
        }
        if (host != null && host.length() > 0) {
            buf.append(host);
            if (port > 0) {
                buf.append(":");
                buf.append(port);
            }
        }
        String group = getGroup();
        if (group != null && group.length() > 0) {
            buf.append("/");
            buf.append(group);
        }
        if (appendParameter) {
            buildParameters(buf, true, parameters);
        }

        return buf.toString();
    }

    public String toParameterString(String... parameters) {
        StringBuilder buf = new StringBuilder();
        buildParameters(buf, false, parameters);
        return buf.toString();
    }

    private void buildParameters(StringBuilder buf, boolean concat, String[] parameters) {
        if (getParameters() != null && getParameters().size() > 0) {
            List<String> includes = (parameters == null || parameters.length == 0 ? null : Arrays.asList(parameters));
            boolean first = true;
            for (Map.Entry<String, String> entry : new TreeMap<String, String>(getParameters()).entrySet()) {
                if (entry.getKey() != null && entry.getKey().length() > 0
                        && (includes == null || includes.contains(entry.getKey()))) {
                    if (first) {
                        if (concat) {
                            buf.append("?");
                        }
                        first = false;
                    } else {
                        buf.append("&");
                    }
                    buf.append(entry.getKey());
                    buf.append("=");
                    buf.append(entry.getValue() == null ? "" : entry.getValue().trim());
                }
            }
        }
    }

    public String toServiceString() {
        return buildString(true, false, true, true);
    }

    public String toString() {
        if (cacheStr != null) {
            return cacheStr;
        }
        return cacheStr = buildString(false, true);
    }

    public java.net.URL toJavaURL() {
        try {
            return new java.net.URL(toString());
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public UrlProperties removeParameter(String key) {
        if (key == null || key.length() == 0) {
            return this;
        }
        return removeParameters(key);
    }

    public UrlProperties removeParameters(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return this;
        }
        return removeParameters(keys.toArray(new String[0]));
    }

    public UrlProperties removeParameters(String... keys) {
        if (keys == null || keys.length == 0) {
            return this;
        }
        Map<String, String> map = new HashMap<String, String>(getParameters());
        for (String key : keys) {
            map.remove(key);
        }
        if (map.size() == getParameters().size()) {
            return this;
        }
        return new UrlProperties(protocol, username, password, host, port, group, map);
    }

    public UrlProperties addParameters(Map<String, String> parameters) {
        if (parameters == null || parameters.size() == 0) {
            return this;
        }

        boolean hasAndEqual = true;
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            String value = getParameters().get(entry.getKey());
            if (value == null) {
                if (entry.getValue() != null) {
                    hasAndEqual = false;
                    break;
                }
            } else {
                if (!value.equals(entry.getValue())) {
                    hasAndEqual = false;
                    break;
                }
            }
        }
        if (hasAndEqual) return this;

        Map<String, String> map = new HashMap<String, String>(getParameters());
        map.putAll(parameters);
        return new UrlProperties(protocol, username, password, host, port, getGroup(), map);
    }

    public List<String> getParameterList(String key) {
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return Collections.emptyList();
        }
        return Arrays.asList(CommonConstants.COMMA_SPLIT_PATTERN.split(value));
    }

    public UrlProperties setParameterList(String key, List<String> list) {
        if (CollectionUtils.isEmpty(list)) {
            return this;
        }
        return addParameter(key, StringUtils.join(list, ","));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UrlProperties that = (UrlProperties) o;
        return port == that.port &&
                Objects.equals(protocol, that.protocol) &&
                Objects.equals(username, that.username) &&
                Objects.equals(password, that.password) &&
                Objects.equals(host, that.host) &&
                Objects.equals(group, that.group) &&
                Objects.equals(parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(protocol, username, password, host, port, group, parameters);
    }
}
