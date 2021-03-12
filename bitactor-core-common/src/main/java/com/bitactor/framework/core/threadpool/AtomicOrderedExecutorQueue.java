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

package com.bitactor.framework.core.threadpool;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 原子的有序执行队列
 *
 * @author WXH
 */
public class AtomicOrderedExecutorQueue implements OrderedExecutor {
    private final ExecutorService executorService;
    private Queue<Runnable> orderedQueue = new ConcurrentLinkedQueue<>();
    private AtomicBoolean runState = new AtomicBoolean(false);

    public AtomicOrderedExecutorQueue(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public void add(Runnable runnable) {
        this.orderedQueue.add(runnable);
        if (runState.compareAndSet(false, true)) {
            executorService.execute(this);
        }
    }

    public void run() {
        Runnable runnable = this.orderedQueue.poll();
        if (Objects.isNull(runnable)) {
            runState.getAndSet(false);
            return;
        }
        try {
            runnable.run();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.complete();
        }
    }

    private void complete() {
        executorService.execute(this);
    }
}

