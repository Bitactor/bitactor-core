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

package com.bitactor.framework.core.eventloop.thread;

import com.bitactor.framework.core.eventloop.abs.AbsSingleThreadEventExecutor;

/**
 * 类似于{@link java.util.concurrent。RejectedExecutionHandler}但是特定于{@link AbsSingleThreadEventExecutor}。
 */
public interface BitactorRejectedExecutionHandler {

    /**
     * 当有人试图向{@link AbsSingleThreadEventExecutor}添加任务时调用，但由于容量不足而失败的限制。
     */
    void rejected(Runnable task, AbsSingleThreadEventExecutor executor);
}
