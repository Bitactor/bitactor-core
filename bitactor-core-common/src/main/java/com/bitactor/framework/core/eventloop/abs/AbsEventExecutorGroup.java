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

package com.bitactor.framework.core.eventloop.abs;

import com.bitactor.framework.core.eventloop.inf.IEventExecutorGroup;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import static com.bitactor.framework.core.eventloop.abs.AbsEventExecutor.*;

/**
 * 抽象执行器组
 */
public abstract class AbsEventExecutorGroup implements IEventExecutorGroup {

    public abstract void shutdown();

    public List<Runnable> shutdownNow() {
        shutdown();
        return Collections.emptyList();
    }
    @Override
    public CompletableFuture<?> shutdownGracefully() {
        return shutdownGracefully(DEFAULT_SHUTDOWN_QUIET_PERIOD, DEFAULT_SHUTDOWN_TIMEOUT, TimeUnit.SECONDS);
    }

    public <T> Future<T> submit(Callable<T> task) {
        return next().submit(task);
    }

    public <T> Future<T> submit(Runnable task, T result) {
        return next().submit(task,result);
    }

    public Future<?> submit(Runnable task) {
        return next().submit(task);
    }

    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return next().invokeAll(tasks);
    }

    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return next().invokeAll(tasks, timeout, unit);
    }

    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return next().invokeAny(tasks);
    }

    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return next().invokeAny(tasks, timeout, unit);
    }

    public void execute(Runnable command) {
        next().execute(command);
    }
}
