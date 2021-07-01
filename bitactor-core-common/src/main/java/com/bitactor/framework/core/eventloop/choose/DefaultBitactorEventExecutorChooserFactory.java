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

package com.bitactor.framework.core.eventloop.choose;


import com.bitactor.framework.core.eventloop.inf.IEventExecutor;

import java.util.concurrent.atomic.AtomicInteger;

public final class DefaultBitactorEventExecutorChooserFactory implements IEventExecutorChooserFactory {

    public static final DefaultBitactorEventExecutorChooserFactory INSTANCE = new DefaultBitactorEventExecutorChooserFactory();

    private DefaultBitactorEventExecutorChooserFactory() { }


    public IEventExecutorChooser newChooser(IEventExecutor[] executors) {
        if (isPowerOfTwo(executors.length)) {
            return new PowerOfTwoEventExecutorChooser(executors);
        } else {
            return new GenericEventExecutorChooser(executors);
        }
    }

    private static boolean isPowerOfTwo(int val) {
        return (val & -val) == val;
    }

    private static final class PowerOfTwoEventExecutorChooser implements IEventExecutorChooser {
        private final AtomicInteger idx = new AtomicInteger();
        private final IEventExecutor[] executors;

        PowerOfTwoEventExecutorChooser(IEventExecutor[] executors) {
            this.executors = executors;
        }

        public IEventExecutor next() {
            return executors[idx.getAndIncrement() & executors.length - 1];
        }
    }

    private static final class GenericEventExecutorChooser implements IEventExecutorChooser {
        private final AtomicInteger idx = new AtomicInteger();
        private final IEventExecutor[] executors;

        GenericEventExecutorChooser(IEventExecutor[] executors) {
            this.executors = executors;
        }

        public IEventExecutor next() {
            return executors[Math.abs(idx.getAndIncrement() % executors.length)];
        }
    }
}
