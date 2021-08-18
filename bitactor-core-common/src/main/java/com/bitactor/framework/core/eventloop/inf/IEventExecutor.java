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

/**
 * @author WXH
 */
public interface IEventExecutor extends IEventExecutorGroup {
    /**
     * 返回对自身的引用。
     */
    @Override
    IEventExecutor next();

    @Override
    IEventExecutor next(CustomEventExecutorChooser chooser);

    /**
     * 返回{@link IEventExecutor}的父类{@link IEventExecutor}，
     */
    IEventExecutorGroup parent();

    /**
     * 使用参数{@link Thread#currentThread()}调用{@link #inEventLoop(Thread)}
     */
    boolean inEventLoop();

    /**
     * 如果给定的{@link Thread}在事件循环中执行，则返回{@code true}。
     * {@code false}否则。
     */
    boolean inEventLoop(Thread thread);
}
