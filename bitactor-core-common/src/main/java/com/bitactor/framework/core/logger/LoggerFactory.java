
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

package com.bitactor.framework.core.logger;


import com.bitactor.framework.core.logger.jcl.JclLoggerAdapter;
import com.bitactor.framework.core.logger.jdk.JdkLoggerAdapter;
import com.bitactor.framework.core.logger.log4j.Log4jLoggerAdapter;
import com.bitactor.framework.core.logger.log4j2.Log4j2LoggerAdapter;
import com.bitactor.framework.core.logger.slf4j.Slf4jLoggerAdapter;
import com.bitactor.framework.core.logger.support.FailsafeLogger;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Logger factory
 */
public class LoggerFactory {

    private static final ConcurrentMap<String, FailsafeLogger> LOGGERS = new ConcurrentHashMap<String, FailsafeLogger>();
    private static volatile LoggerAdapter LOGGER_ADAPTER;

    static {
        String logger = System.getProperty("bitactor.application.logger", "");
        if ("slf4j".equals(logger)) {
            setLoggerAdapter(new Slf4jLoggerAdapter());
        } else if ("jcl".equals(logger)) {
            setLoggerAdapter(new JclLoggerAdapter());
        } else if ("log4j".equals(logger)) {
            setLoggerAdapter(new Log4jLoggerAdapter());
        } else if ("log4j2".equals(logger)) {
            setLoggerAdapter(new Log4j2LoggerAdapter());
        } else if ("jdk".equals(logger)) {
            setLoggerAdapter(new JdkLoggerAdapter());
        } else {
            try {
                setLoggerAdapter(new Log4j2LoggerAdapter());
            } catch (Throwable e0) {
                try {
                    setLoggerAdapter(new Log4jLoggerAdapter());
                } catch (Throwable e1) {
                    try {
                        setLoggerAdapter(new Slf4jLoggerAdapter());
                    } catch (Throwable e2) {
                        try {
                            setLoggerAdapter(new JclLoggerAdapter());
                        } catch (Throwable e3) {
                            setLoggerAdapter(new JdkLoggerAdapter());
                        }
                    }
                }
            }
        }
    }

    private LoggerFactory() {
    }

    /**
     * 设置日志记录器提供者
     *
     * @param loggerAdapter
     */
    public static void setLoggerAdapter(LoggerAdapter loggerAdapter) {
        if (loggerAdapter != null) {
            Logger logger = loggerAdapter.getLogger(LoggerFactory.class.getName());
            logger.info("using logger: " + loggerAdapter.getClass().getName());
            LoggerFactory.LOGGER_ADAPTER = loggerAdapter;
            for (Map.Entry<String, FailsafeLogger> entry : LOGGERS.entrySet()) {
                entry.getValue().setLogger(LOGGER_ADAPTER.getLogger(entry.getKey()));
            }
        }
    }

    /**
     * 获取记录器提供者
     *
     * @param key
     * @return logger
     */
    public static Logger getLogger(Class<?> key) {
        FailsafeLogger logger = LOGGERS.get(key.getName());
        if (logger == null) {
            LOGGERS.putIfAbsent(key.getName(), new FailsafeLogger(LOGGER_ADAPTER.getLogger(key)));
            logger = LOGGERS.get(key.getName());
        }
        return logger;
    }

    /**
     * 获取记录器提供者
     *
     * @param key
     * @return logger
     */
    public static Logger getLogger(String key) {
        FailsafeLogger logger = LOGGERS.get(key);
        if (logger == null) {
            LOGGERS.putIfAbsent(key, new FailsafeLogger(LOGGER_ADAPTER.getLogger(key)));
            logger = LOGGERS.get(key);
        }
        return logger;
    }

    /**
     * 获取日志级别
     *
     * @return logging level
     */
    public static Level getLevel() {
        return LOGGER_ADAPTER.getLevel();
    }

    /**
     * 设置当前日志记录级别
     *
     * @param level logging level
     */
    public static void setLevel(Level level) {
        LOGGER_ADAPTER.setLevel(level);
    }

    /**
     * 获取当前日志文件
     *
     * @return current logging file
     */
    public static File getFile() {
        return LOGGER_ADAPTER.getFile();
    }

}
