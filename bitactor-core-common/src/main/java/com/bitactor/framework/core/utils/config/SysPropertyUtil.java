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

package com.bitactor.framework.core.utils.config;

import com.bitactor.framework.core.logger.Logger;
import com.bitactor.framework.core.logger.LoggerFactory;

import java.security.AccessController;
import java.security.PrivilegedAction;

public final class SysPropertyUtil {

    private static final Logger logger = LoggerFactory.getLogger(SysPropertyUtil.class);

    public static boolean contains(String key) {
        return get(key) != null;
    }

    public static String get(String key) {
        return get(key, null);
    }

    public static String get(final String key, String def) {
        if (key == null) {
            throw new NullPointerException("key");
        }
        if (key.isEmpty()) {
            throw new IllegalArgumentException("key must not be empty.");
        }

        String value = null;
        try {
            if (System.getSecurityManager() == null) {
                value = System.getProperty(key);
            } else {
                value = AccessController.doPrivileged(new PrivilegedAction<String>() {
                    @Override
                    public String run() {
                        return System.getProperty(key);
                    }
                });
            }
        } catch (SecurityException e) {
            logger.warn("Unable to retrieve a system property '{}'; default values will be used.", key, e);
        }

        if (value == null) {
            return def;
        }

        return value;
    }


    public static boolean getBoolean(String key, boolean def) {
        String value = get(key);
        if (value == null) {
            return def;
        }

        value = value.trim().toLowerCase();
        if (value.isEmpty()) {
            return def;
        }

        if ("true".equals(value) || "yes".equals(value) || "1".equals(value)) {
            return true;
        }

        if ("false".equals(value) || "no".equals(value) || "0".equals(value)) {
            return false;
        }

        logger.warn(
                "Unable to parse the boolean system property '{}':{} - using the default value: {}",
                key, value, def
        );

        return def;
    }


    public static int getInt(String key, int def) {
        String value = get(key);
        if (value == null) {
            return def;
        }

        value = value.trim();
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            // Ignore
        }

        logger.warn(
                "Unable to parse the integer system property '{}':{} - using the default value: {}",
                key, value, def
        );

        return def;
    }


    public static long getLong(String key, long def) {
        String value = get(key);
        if (value == null) {
            return def;
        }

        value = value.trim();
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            // Ignore
        }

        logger.warn(
                "Unable to parse the long integer system property '{}':{} - using the default value: {}",
                key, value, def
        );

        return def;
    }

    private SysPropertyUtil() {
        // Unused
    }
}
