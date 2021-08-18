
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
import com.bitactor.framework.core.eventloop.inf.IEventLoop;
import com.bitactor.framework.core.eventloop.inf.IEventLoopGroup;
import com.bitactor.framework.core.eventloop.thread.BitactorRejectedExecutionHandler;
import com.bitactor.framework.core.utils.assist.ValidateUtil;
import com.bitactor.framework.core.utils.config.SysPropertyUtil;

import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

/**
 * Abstract base class for {@link IEventLoop}s that execute all its submitted tasks in a single thread.
 *
 */
public abstract class AbsSingleThreadEventLoop extends AbsSingleThreadEventExecutor implements IEventLoop {

    protected static final int DEFAULT_MAX_PENDING_TASKS = Math.max(16,
            SysPropertyUtil.getInt("icom.bitactor.framework.maxPendingTasks", Integer.MAX_VALUE));

    private final Queue<Runnable> tailTasks;

    protected AbsSingleThreadEventLoop(IEventLoopGroup parent, ThreadFactory threadFactory, boolean addTaskWakesUp) {
        this(parent, threadFactory, addTaskWakesUp, DEFAULT_MAX_PENDING_TASKS, BitactorRejectedExecutionHandlers.reject());
    }

    protected AbsSingleThreadEventLoop(IEventLoopGroup parent, Executor executor, boolean addTaskWakesUp) {
        this(parent, executor, addTaskWakesUp, DEFAULT_MAX_PENDING_TASKS, BitactorRejectedExecutionHandlers.reject());
    }

    protected AbsSingleThreadEventLoop(IEventLoopGroup parent, ThreadFactory threadFactory,
                                       boolean addTaskWakesUp, int maxPendingTasks,
                                       BitactorRejectedExecutionHandler rejectedExecutionHandler) {
        super(parent, threadFactory, addTaskWakesUp, maxPendingTasks, rejectedExecutionHandler);
        tailTasks = newTaskQueue(maxPendingTasks);
    }

    protected AbsSingleThreadEventLoop(IEventLoopGroup parent, Executor executor,
                                       boolean addTaskWakesUp, int maxPendingTasks,
                                       BitactorRejectedExecutionHandler rejectedExecutionHandler) {
        super(parent, executor, addTaskWakesUp, maxPendingTasks, rejectedExecutionHandler);
        tailTasks = newTaskQueue(maxPendingTasks);
    }

    protected AbsSingleThreadEventLoop(IEventLoopGroup parent, Executor executor,
                                       boolean addTaskWakesUp, Queue<Runnable> taskQueue, Queue<Runnable> tailTaskQueue,
                                       BitactorRejectedExecutionHandler rejectedExecutionHandler) {
        super(parent, executor, addTaskWakesUp, taskQueue, rejectedExecutionHandler);
        tailTasks = ValidateUtil.checkNotNull(tailTaskQueue, "tailTaskQueue");
    }

    @Override
    public IEventLoopGroup parent() {
        return (IEventLoopGroup) super.parent();
    }

    @Override
    public IEventLoop next() {
        return (IEventLoop) super.next();
    }

    @Override
    public IEventExecutor next(CustomEventExecutorChooser chooser) {
        return super.next(chooser);
    }

    @Override
    protected void afterRunningAllTasks() {
        runAllTasksFrom(tailTasks);
    }

    @Override
    protected boolean hasTasks() {
        return super.hasTasks() || !tailTasks.isEmpty();
    }

    @Override
    public int pendingTasks() {
        return super.pendingTasks() + tailTasks.size();
    }

}
