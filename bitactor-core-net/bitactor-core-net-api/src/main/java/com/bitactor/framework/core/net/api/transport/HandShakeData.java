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

package com.bitactor.framework.core.net.api.transport;

import java.util.HashMap;

/**
 * @author WXH
 */
public class HandShakeData {

    /**
     * 系统数据
     */
    private HashMap<String, String> systemParameter = new HashMap<String, String>();
    /**
     * 之定义数据
     */
    private HashMap<String, String> customParameter = new HashMap<String, String>();

    public HandShakeData() {

    }

    public HashMap<String, String> getSystemParameter() {
        return systemParameter;
    }

    public void setSystemParameter(HashMap<String, String> systemParameter) {
        this.systemParameter = systemParameter;
    }

    public HashMap<String, String> getCustomParameter() {
        return customParameter;
    }

    public void setCustomParameter(HashMap<String, String> customParameter) {
        this.customParameter = customParameter;
    }

    private void addParameter(HashMap<String, String> map, String key, String value) {
        map.put(key, value);
    }

    private String getParameter(HashMap<String, String> map, String key) {
        return map.get(key);
    }

    /************************************系统参数**************************************/
    public void addSystemParameter(String key, String value) {
        addParameter(systemParameter, key, value);
    }

    public void addSystemParameter(String key, int value) {
        addSystemParameter(key, String.valueOf(value));
    }

    public void addSystemParameter(String key, boolean value) {
        addSystemParameter(key, String.valueOf(value));
    }

    public void addSystemParameter(String key, long value) {
        addSystemParameter(key, String.valueOf(value));
    }

    public void addSystemParameter(String key, double value) {
        addSystemParameter(key, String.valueOf(value));
    }

    public void addSystemParameter(String key, float value) {
        addSystemParameter(key, String.valueOf(value));
    }

    public String getSystemParameterStr(String key) {
        return getParameter(systemParameter, key);
    }

    public Integer getSystemParameterInt(String key) {
        return Integer.valueOf(getSystemParameterStr(key));
    }

    public Boolean getSystemParameterBool(String key) {
        return Boolean.valueOf(getSystemParameterStr(key));
    }

    public Long getSystemParameterLong(String key) {
        return Long.valueOf(getSystemParameterStr(key));
    }

    public Double getSystemParameterDouble(String key) {
        return Double.valueOf(getSystemParameterStr(key));
    }

    public Float getSystemParameterFloat(String key) {
        return Float.valueOf(getSystemParameterStr(key));
    }

    /************************************自定义参数**************************************/

    public void addCustomParameter(String key, String value) {
        addParameter(customParameter, key, value);
    }

    public void addCustomParameter(String key, int value) {
        addCustomParameter(key, String.valueOf(value));
    }

    public void addCustomParameter(String key, boolean value) {
        addCustomParameter(key, String.valueOf(value));
    }

    public void addCustomParameter(String key, long value) {
        addCustomParameter(key, String.valueOf(value));
    }

    public void addCustomParameter(String key, double value) {
        addCustomParameter(key, String.valueOf(value));
    }

    public void addCustomParameter(String key, float value) {
        addCustomParameter(key, String.valueOf(value));
    }

    public String getCustomParameterStr(String key) {
        return getParameter(customParameter, key);
    }

    public Integer getCustomParameterInt(String key) {
        return Integer.valueOf(getCustomParameterStr(key));
    }

    public Boolean getCustomParameterBool(String key) {
        return Boolean.valueOf(getCustomParameterStr(key));
    }

    public Long getCustomParameterLong(String key) {
        return Long.valueOf(getCustomParameterStr(key));
    }

    public Double getCustomParameterDouble(String key) {
        return Double.valueOf(getCustomParameterStr(key));
    }

    public Float getCustomParameterFloat(String key) {
        return Float.valueOf(getCustomParameterStr(key));
    }

    @Override
    public String toString() {
        return "systemParameter: " + systemParameter + " ,customParameter: " + customParameter;
    }
}
