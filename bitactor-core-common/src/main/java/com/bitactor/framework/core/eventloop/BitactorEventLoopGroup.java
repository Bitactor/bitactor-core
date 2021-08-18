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

package com.bitactor.framework.core.eventloop;

import com.bitactor.framework.core.eventloop.abs.AbsMultiThreadEventLoopGroup;
import com.bitactor.framework.core.eventloop.inf.IEventLoop;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

/**
 * 事件循环组
 *
 * @author WXH
 */
public class BitactorEventLoopGroup extends AbsMultiThreadEventLoopGroup {
    /**
     * Create a new instance with the default number of threads.
     */
    public BitactorEventLoopGroup() {
        this(0);
    }

    /**
     * Create a new instance
     *
     * @param nThreads the number of threads to use
     */
    public BitactorEventLoopGroup(int nThreads) {
        this(nThreads, (ThreadFactory) null);
    }

    /**
     * Create a new instance
     *
     * @param nThreads      the number of threads to use
     * @param threadFactory the {@link ThreadFactory} or {@code null} to use the default
     */
    public BitactorEventLoopGroup(int nThreads, ThreadFactory threadFactory) {
        super(nThreads, threadFactory);
    }

    /**
     * Create a new instance
     *
     * @param nThreads the number of threads to use
     * @param executor the Executor to use, or {@code null} if the default should be used.
     */
    public BitactorEventLoopGroup(int nThreads, Executor executor) {
        super(nThreads, executor);
    }

    @Override
    protected IEventLoop newChild(Executor executor, Object... args) throws Exception {
        return new BitactorEventLoop(this, executor);
    }
}
