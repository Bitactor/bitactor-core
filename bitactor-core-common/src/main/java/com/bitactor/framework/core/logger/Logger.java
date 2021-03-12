
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

/**
 * logger 接口
 */
public interface Logger {


    void trace(String msg);

    void trace(String msg, Object... objects);


    void trace(Throwable e);


    void trace(String msg, Throwable e);

    void debug(String msg);

    void debug(String msg, Object... objects);


    void debug(Throwable e);


    void debug(String msg, Throwable e);


    void info(String msg);

    void info(String msg, Object... objects);


    void info(Throwable e);


    void info(String msg, Throwable e);


    void warn(String msg);

    void warn(String msg, Object... objects);


    void warn(Throwable e);


    void warn(String msg, Throwable e);

    void error(String msg);

    void error(String msg, Object... objects);

    void error(Throwable e);


    void error(String msg, Throwable e);

    boolean isTraceEnabled();


    boolean isDebugEnabled();


    boolean isInfoEnabled();


    boolean isWarnEnabled();

    boolean isErrorEnabled();

}