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
import com.bitactor.framework.core.eventloop.choose.DefaultBitactorEventExecutorChooserFactory;
import com.bitactor.framework.core.eventloop.choose.IEventExecutorChooserFactory;
import com.bitactor.framework.core.eventloop.inf.IEventExecutor;
import com.bitactor.framework.core.eventloop.thread.BitactorThreadPerTaskExecutor;
import com.bitactor.framework.core.eventloop.thread.DefaultBitactorThreadFactory;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 */
public abstract class AbsMultiThreadEventExecutorGroup extends AbsEventExecutorGroup {
    private final IEventExecutor[] children;
    private final Set<IEventExecutor> readonlyChildren;
    private final AtomicInteger terminatedChildren = new AtomicInteger();
    private final IEventExecutorChooserFactory.IEventExecutorChooser chooser;
    private final CompletableFuture<?> terminationFuture = new CompletableFuture<Object>();

    /**
     * 创建一个新实例。
     *
     * @param nThreads      这个实例将使用的线程数。
     * @param threadFactory 使用的threadFactory，或者{@code null}如果应该使用默认值。
     * @param args          参数将传递给每个{@link #newChild(Executor, Object...)}调用
     */
    protected AbsMultiThreadEventExecutorGroup(int nThreads, ThreadFactory threadFactory, Object... args) {
        this(nThreads, threadFactory == null ? null : new BitactorThreadPerTaskExecutor(threadFactory), args);
    }

    /**
     * 创建一个新实例。
     *
     * @param nThreads 这个实例将使用的线程数。
     * @param executor {@code null} 则使用默认值
     * @param args     参数将传递给每个{@link #newChild(Executor, Object...)}调用
     */
    protected AbsMultiThreadEventExecutorGroup(int nThreads, Executor executor, Object... args) {
        this(nThreads, executor, DefaultBitactorEventExecutorChooserFactory.INSTANCE, args);
    }

    protected AbsMultiThreadEventExecutorGroup(int nThreads, Executor executor,
                                               IEventExecutorChooserFactory chooserFactory, Object... args) {
        if (nThreads <= 0) {
            throw new IllegalArgumentException(String.format("nThreads: %d (expected: > 0)", nThreads));
        }

        if (executor == null) {
            executor = new BitactorThreadPerTaskExecutor(newDefaultThreadFactory());
        }

        children = new IEventExecutor[nThreads];

        for (int i = 0; i < nThreads; i++) {
            boolean success = false;
            try {
                children[i] = newChild(executor, args);
                success = true;
            } catch (Exception e) {
                throw new IllegalStateException("failed to create a child event loop", e);
            } finally {
                if (!success) {
                    for (int j = 0; j < i; j++) {
                        children[j].shutdownGracefully();
                    }

                    for (int j = 0; j < i; j++) {
                        IEventExecutor e = children[j];
                        try {
                            while (!e.isTerminated()) {
                                e.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
                            }
                        } catch (InterruptedException interrupted) {
                            // 中断处理
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
        }

        chooser = chooserFactory.newChooser(children);

        for (IEventExecutor e : children) {
            e.terminationFuture().whenComplete((o, cause) -> {
                if (terminatedChildren.incrementAndGet() == children.length) {
                    terminationFuture.complete(null);
                }
            });
        }

        Set<IEventExecutor> childrenSet = new LinkedHashSet<IEventExecutor>(children.length);
        Collections.addAll(childrenSet, children);
        readonlyChildren = Collections.unmodifiableSet(childrenSet);
    }

    protected ThreadFactory newDefaultThreadFactory() {
        return new DefaultBitactorThreadFactory(getClass());
    }

    protected abstract IEventExecutor newChild(Executor executor, Object... args) throws Exception;

    public IEventExecutor next() {
        return chooser.next();
    }

    @Override
    public IEventExecutor next(CustomEventExecutorChooser chooser) {
        return chooser.choose(children);
    }

    public Iterator<IEventExecutor> iterator() {
        return readonlyChildren.iterator();
    }

    /**
     * 返回该实现使用的{@link IEventExecutor}的数量。这个数字是MAP
     * 1:1的线程使用。
     */
    public final int executorCount() {
        return children.length;
    }

    public CompletableFuture<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit) {
        for (IEventExecutor l : children) {
            l.shutdownGracefully(quietPeriod, timeout, unit);
        }
        return terminationFuture();
    }

    public CompletableFuture<?> terminationFuture() {
        return terminationFuture;
    }

    @Override
    public void shutdown() {
        for (IEventExecutor l : children) {
            l.shutdown();
        }
    }

    @Override
    public boolean isShuttingDown() {
        for (IEventExecutor l : children) {
            if (!l.isShuttingDown()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isShutdown() {
        for (IEventExecutor l : children) {
            if (!l.isShutdown()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isTerminated() {
        for (IEventExecutor l : children) {
            if (!l.isTerminated()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit)
            throws InterruptedException {
        long deadline = System.nanoTime() + unit.toNanos(timeout);
        loop:
        for (IEventExecutor l : children) {
            for (; ; ) {
                long timeLeft = deadline - System.nanoTime();
                if (timeLeft <= 0) {
                    break loop;
                }
                if (l.awaitTermination(timeLeft, TimeUnit.NANOSECONDS)) {
                    break;
                }
            }
        }
        return isTerminated();
    }
}
