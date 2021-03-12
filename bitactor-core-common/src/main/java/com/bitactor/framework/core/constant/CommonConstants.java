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

package com.bitactor.framework.core.constant;

import java.util.regex.Pattern;

/**
 * 公共常量
 *
 * @author WXH
 */
public class CommonConstants {

    public static final String DEF_UNKNOWN = "unknown";

    public static final String APP_ID_KEY = "app.id";

    public static final String APPLICATION_KEY = "application";

    public static final String COMMA_SEPARATOR = ",";

    public static final String VERSION_KEY = "version";

    public static final String DEFAULT_KEY_PREFIX = "default.";

    public static final String ANY_VALUE = "*";

    public static final String SUB_SYMBOL = "-";

    public static final String ENABLED_KEY = "enabled";

    public static final String DISABLED_KEY = "disabled";

    public static final String CLASSIFIER_KEY = "classifier";

    public static final String TIMESTAMP_KEY = "timestamp";

    public static final Pattern COMMA_SPLIT_PATTERN = Pattern.compile("\\s*[,]+\\s*");

    public static final String EMPTY_PROTOCOL = "empty";

    public static final String PROTOCOL_KEY = "protocol";

    public static final String DYNAMIC_KEY = "dynamic";

    public static final String GROUP_KEY = "group";

    public static final String NACOS_GROUP_KEY = "nacos.group";

    public static final String BACKUP_KEY = "backup";

    public static final String PID_KEY = "pid";

    public static final int RUN_THREADS = Math.min(Runtime.getRuntime().availableProcessors() + 1, 16);

    public static final int RUN_IO_THREADS = Math.min(2 * Runtime.getRuntime().availableProcessors() + 1, 32);


}
