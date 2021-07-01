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

import com.bitactor.framework.core.eventloop.inf.IEventExecutor;
import com.bitactor.framework.core.eventloop.thread.BitactorRejectedExecutionHandler;
import com.bitactor.framework.core.utils.assist.ValidateUtil;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * 公开创建不同{@link BitactorRejectedExecutionHandler}的helper方法。
 */
public final class BitactorRejectedExecutionHandlers {
    private static final BitactorRejectedExecutionHandler REJECT = new BitactorRejectedExecutionHandler() {
        @Override
        public void rejected(Runnable task, AbsSingleThreadEventExecutor executor) {
            throw new RejectedExecutionException();
        }
    };

    private BitactorRejectedExecutionHandlers() {
    }

    /**
     * 返回一个{@link BitactorRejectedExecutionHandler}，它总是抛出一个{@link RejectedExecutionException}。
     */
    public static BitactorRejectedExecutionHandler reject() {
        return REJECT;
    }

    /**
     * 当任务由于配置的时间限制无法添加时，尝试退出。这
     * 只在任务是从事件循环外部添加的情况下才会执行，这意味着
     * {@link IEventExecutor#inEventLoop()}返回{@code false}。
     */
    public static BitactorRejectedExecutionHandler backoff(final int retries, long backoffAmount, TimeUnit unit) {
        ValidateUtil.checkPositive(retries, "retries");
        final long backOffNanos = unit.toNanos(backoffAmount);
        return new BitactorRejectedExecutionHandler() {
            @Override
            public void rejected(Runnable task, AbsSingleThreadEventExecutor executor) {
                if (!executor.inEventLoop()) {
                    for (int i = 0; i < retries; i++) {
                        // Try to wake up the executor so it will empty its task queue.
                        executor.wakeup(false);

                        LockSupport.parkNanos(backOffNanos);
                        if (executor.offerTask(task)) {
                            return;
                        }
                    }
                }
                // Either we tried to add the task from within the EventLoop or we was not able to add it even with
                // backoff.
                throw new RejectedExecutionException();
            }
        };
    }
}
