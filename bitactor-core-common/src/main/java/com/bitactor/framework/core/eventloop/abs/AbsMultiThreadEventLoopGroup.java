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
import com.bitactor.framework.core.eventloop.choose.IEventExecutorChooserFactory;
import com.bitactor.framework.core.eventloop.inf.IEventLoop;
import com.bitactor.framework.core.eventloop.inf.IEventLoopGroup;
import com.bitactor.framework.core.eventloop.thread.DefaultBitactorThreadFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

public abstract class AbsMultiThreadEventLoopGroup extends AbsMultiThreadEventExecutorGroup implements IEventLoopGroup {
    private static final int DEFAULT_EVENT_LOOP_THREADS;

    static {
        DEFAULT_EVENT_LOOP_THREADS = Math.max(1, Integer.parseInt(System.getProperty(
                "io.netty.eventloopThreads",
                (Runtime.getRuntime().availableProcessors() * 2) + "")));
    }

    protected AbsMultiThreadEventLoopGroup(int nThreads, Executor executor, Object... args) {
        super(nThreads == 0 ? DEFAULT_EVENT_LOOP_THREADS : nThreads, executor, args);
    }

    protected AbsMultiThreadEventLoopGroup(int nThreads, ThreadFactory threadFactory, Object... args) {
        super(nThreads == 0 ? DEFAULT_EVENT_LOOP_THREADS : nThreads, threadFactory, args);
    }

    protected AbsMultiThreadEventLoopGroup(int nThreads, Executor executor, IEventExecutorChooserFactory chooserFactory,
                                           Object... args) {
        super(nThreads == 0 ? DEFAULT_EVENT_LOOP_THREADS : nThreads, executor, chooserFactory, args);
    }

    @Override
    protected ThreadFactory newDefaultThreadFactory() {
        return new DefaultBitactorThreadFactory(getClass(), Thread.MAX_PRIORITY);
    }

    @Override
    public IEventLoop next() {
        return (IEventLoop) super.next();
    }

    @Override
    public IEventLoop next(CustomEventExecutorChooser chooser) {
        return (IEventLoop) super.next(chooser);
    }

    @Override
    protected abstract IEventLoop newChild(Executor executor, Object... args) throws Exception;
}
