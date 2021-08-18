
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

package com.bitactor.framework.core.rpc.api.invoker;

import com.bitactor.framework.core.rpc.api.async.AsyncResult;

import java.util.List;
import java.util.Map;

/**
 * Invocation. (API, Prototype, NonThreadSafe)
 */
public interface Invocation {

    /**
     * 获取方法名
     *
     * @return method name.
     */
    String getMethodName();

    /**
     * 获取参数类型
     *
     * @return parameter types.
     */
    Class<?>[] getParameterTypes();

    /**
     * 获取参数
     *
     * @return arguments.
     */
    Object[] getArguments();

    /**
     * 获取附加参数.
     *
     * @return attachments.
     */
    Map<String, String> getAttachments();

    /**
     * 通过Key 获取附加参数
     *
     * @return attachment value.
     */
    String getAttachment(String key);

    /**
     * 按key取附加参数，没有则赋默认值。
     *
     * @return attachment value.
     */
    String getAttachment(String key, String defaultValue);

    /**
     * 是否异步
     *
     * @return
     */
    boolean isAsync();

    /**
     * 异步回调
     *
     * @return
     */
    List<AsyncResult> getCallbacks();

}
