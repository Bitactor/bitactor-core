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

package com.bitactor.framework.core.eventloop.inf;

import com.bitactor.framework.core.eventloop.choose.CustomEventExecutorChooser;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author WXH
 */
public interface IEventExecutorGroup extends ExecutorService, Iterable<IEventExecutor> {
    /**
     * 返回{@code true}，当且仅当由这个{@link IEventExecutor group}管理的所有{@link IEventExecutor}
     * 是{@linkplain #shutdownGracefully() 优雅的关闭 }或者是{@linkplain #isShutdown() 关闭}。
     */
    boolean isShuttingDown();

    /**
     * 返回一个由 {@link IEventExecutorGroup} 管理的 {@link IEventExecutor}
     *
     * @return
     */
    IEventExecutor next();

    /**
     * 自定义规则返回一个由 {@link IEventExecutorGroup} 管理的 {@link IEventExecutor}
     *
     * @return
     */
    IEventExecutor next(CustomEventExecutorChooser chooser);

    /**
     * 使用合理的默认值的{@link #shutdownGracefully(long, long, TimeUnit)}的快捷方法。
     *
     * @return {@link #terminationFuture()}
     */
    CompletableFuture<?> shutdownGracefully();

    /**
     * 向这个执行程序发出信号，表示调用者希望关闭执行程序。一旦这个方法被调用，
     * {@link #isShuttingDown()}开始返回{@code true}，执行器准备关闭自己。
     * 与{@link #shutdown()}不同的是《优雅关闭》会确保再没有任务提交的静默期阶段(通常是几秒钟)自动关闭。
     * 如果一个任务在静默期间提交保证被接受，静默期结束后重新执行结束任务。
     *
     * @param quietPeriod 文档中描述的安静周期
     * @param timeout     等待执行器为{@linkplain #shutdown()}的最大时无论任务是否在安静期间提交
     * @param unit        {@code quietPeriod}和{@code timeout}的单位
     * @return {@link #terminationFuture()}
     */
    CompletableFuture<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit);

    /**
     * 返回{@link Future}，当所有的{@link IEventExecutor}被此管理时，它会被通知
     * {@link IEventExecutorGroup}被终止。
     */
    CompletableFuture<?> terminationFuture();
}
