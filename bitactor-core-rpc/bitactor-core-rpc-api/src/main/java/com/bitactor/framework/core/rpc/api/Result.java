
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

/**
 * 结果接口
 *
 * @author WXH
 */
public interface Result {

    /**
     * 返回值
     *
     * @return result. 如果没有结果，返回null。
     */
    Object getValue();

    /**
     * 获取调用异常.
     *
     * @return exception. 如果没有异常，返回null。
     */
    Throwable getException();

    /**
     * 是否存在异常
     *
     * @return has exception.
     */
    boolean hasException();

    /**
     * 获取返回值，如果存在异常则会抛出异常
     *
     * @return
     * @throws Throwable
     */
    Object get() throws Throwable;

}
