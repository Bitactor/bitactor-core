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

import com.bitactor.framework.core.eventloop.inf.IEventExecutor;
import com.bitactor.framework.core.utils.assist.ValidateUtil;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

/**
 * Allow to retrieve the {@link IEventExecutor} for the calling {@link Thread}.
 */
public final class ThreadExecutorMap {


    private ThreadExecutorMap() {
    }
    public static Executor apply(final Executor executor, final IEventExecutor eventExecutor) {
        ValidateUtil.checkNotNull(executor, "executor");
        ValidateUtil.checkNotNull(eventExecutor, "eventExecutor");
        return new Executor() {
            @Override
            public void execute(final Runnable command) {
                executor.execute(apply(command, eventExecutor));
            }
        };
    }

    public static Runnable apply(final Runnable command, final IEventExecutor eventExecutor) {
        ValidateUtil.checkNotNull(command, "command");
        ValidateUtil.checkNotNull(eventExecutor, "eventExecutor");
        return new Runnable() {
            @Override
            public void run() {
                command.run();
            }
        };
    }
}
