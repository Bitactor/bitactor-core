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

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 阻塞的有序执行队列
 *
 * @author WXH
 */
public class BlockOrderedExecutorQueue<T extends Runnable> implements OrderedExecutor<T> {
    private final ExecutorService executorService;
    private Queue<T> orderedQueue = new LinkedList<>();
    private ReentrantLock lock = new ReentrantLock(true);
    private AtomicBoolean cancelState = new AtomicBoolean(false);

    public BlockOrderedExecutorQueue(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public void add(T runnable) {
        if (cancelState.get()) {
            return;
        }
        try {
            this.lock.lock();
            if (this.orderedQueue.isEmpty()) {
                this.orderedQueue.add(runnable);
                executorService.execute(this::run);
            } else {
                this.orderedQueue.add(runnable);
            }
        } finally {
            this.lock.unlock();
        }

    }

    @Override
    public void cancel() {
        cancelState.getAndSet(true);
    }

    public void run() {
        if (!this.orderedQueue.isEmpty()) {
            Runnable runnable = this.orderedQueue.peek();
            try {
                runnable.run();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (!cancelState.get()) {
                    this.complete();
                }
            }
        }

    }

    private void complete() {
        try {
            this.lock.lock();
            this.orderedQueue.poll();
            if (!this.orderedQueue.isEmpty()) {
                executorService.execute(this::run);
            }
        } finally {
            this.lock.unlock();
        }
    }
}

