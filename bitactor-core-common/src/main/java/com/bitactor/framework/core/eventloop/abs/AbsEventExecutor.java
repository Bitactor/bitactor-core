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

import com.bitactor.framework.core.eventloop.choose.CustomEventExecutorChooser;
import com.bitactor.framework.core.eventloop.inf.IEventExecutor;
import com.bitactor.framework.core.eventloop.inf.IEventExecutorGroup;
import com.bitactor.framework.core.logger.Logger;
import com.bitactor.framework.core.logger.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

public abstract class AbsEventExecutor extends AbstractExecutorService implements IEventExecutor {
    private static final Logger logger = LoggerFactory.getLogger(AbsEventExecutor.class);
    static final long DEFAULT_SHUTDOWN_QUIET_PERIOD = 2;
    static final long DEFAULT_SHUTDOWN_TIMEOUT = 15;

    private final IEventExecutorGroup parent;
    private final Collection<IEventExecutor> selfCollection = Collections.<IEventExecutor>singleton(this);

    protected AbsEventExecutor() {
        this(null);
    }

    protected AbsEventExecutor(IEventExecutorGroup parent) {
        this.parent = parent;
    }

    @Override
    public IEventExecutorGroup parent() {
        return parent;
    }

    @Override
    public IEventExecutor next() {
        return this;
    }

    @Override
    public IEventExecutor next(CustomEventExecutorChooser chooser) {
        IEventExecutor[] iEventExecutors = {this};
        return chooser.choose(iEventExecutors);
    }

    @Override
    public boolean inEventLoop() {
        return inEventLoop(Thread.currentThread());
    }

    @Override
    public Iterator<IEventExecutor> iterator() {
        return selfCollection.iterator();
    }

    @Override
    public CompletableFuture<?> shutdownGracefully() {
        return shutdownGracefully(DEFAULT_SHUTDOWN_QUIET_PERIOD, DEFAULT_SHUTDOWN_TIMEOUT, TimeUnit.SECONDS);
    }

    public abstract void shutdown();

    @Override
    public List<Runnable> shutdownNow() {
        shutdown();
        return Collections.emptyList();
    }

    @Override
    public Future<?> submit(Runnable task) {
        return (Future<?>) super.submit(task);
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return (Future<T>) super.submit(task, result);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return (Future<T>) super.submit(task);
    }

    protected static void safeExecute(Runnable task) {
        try {
            task.run();
        } catch (Throwable t) {
            logger.warn("A task raised an exception. Task: {}", task, t);
        }
    }
}
