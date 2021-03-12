
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

import java.io.File;

/**
 * Logger 提供者
 */
public interface LoggerAdapter {

    /**
     * 获取 Logger
     *
     * @param key
     * @return logger
     */
    Logger getLogger(Class<?> key);

    /**
     * 获取 logger
     *
     * @param key
     * @return logger
     */
    Logger getLogger(String key);

    /**
     * 获取当前日志级别
     *
     * @return current
     */
    Level getLevel();

    /**
     * 设置当前日志记录级别
     *
     * @param level
     */
    void setLevel(Level level);

    /**
     * 获取当前日志文件
     *
     * @return current
     */
    File getFile();

    /**
     * 设置当前日志文件
     *
     * @param file
     */
    void setFile(File file);
}