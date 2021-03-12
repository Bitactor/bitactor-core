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

import com.bitactor.framework.core.logger.support.LoggerFormatter;

/**
 * @author WXH
 */
public abstract class LoggerFormat implements Logger {
    @Override
    public void trace(String msg, Object... objects) {
        String formatMsg = LoggerFormatter.arrayFormat(msg, objects);
        trace(formatMsg);
    }

    @Override
    public void debug(String msg, Object... objects) {
        String formatMsg = LoggerFormatter.arrayFormat(msg, objects);
        debug(formatMsg);
    }

    @Override
    public void info(String msg, Object... objects) {
        String formatMsg = LoggerFormatter.arrayFormat(msg, objects);
        info(formatMsg);
    }

    @Override
    public void warn(String msg, Object... objects) {
        String formatMsg = LoggerFormatter.arrayFormat(msg, objects);
        warn(formatMsg);
    }

    @Override
    public void error(String msg, Object... objects) {
        String formatMsg = LoggerFormatter.arrayFormat(msg, objects);
        error(formatMsg);
    }

}
