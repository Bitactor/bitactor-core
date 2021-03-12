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

package com.bitactor.framework.core.utils.lang;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 原子循环int,当走到最大值是，重新回到初始值，默认初始值0
 *
 * @author WXH
 */
public class CycleAtomicInteger {

    private final AtomicInteger counter;

    private final int initialValue;

    public CycleAtomicInteger() {
        counter = new AtomicInteger(0);
        initialValue = 0;
    }

    public CycleAtomicInteger(int initialValue) {
        counter = new AtomicInteger(initialValue);
        this.initialValue = initialValue;
    }

    /**
     * 获取下个原子值,并根据 range 取模
     *
     * @return
     */
    public int next(int range) {
        if (range < 1)
            throw new IllegalArgumentException();
        counter.compareAndSet(Integer.MAX_VALUE, initialValue);
        int v = Math.abs(counter.getAndIncrement());
        return v % range;
    }

    /**
     * 获取下个原子值，达到最大值后重置到初始值
     *
     * @return
     */
    public int next() {
        counter.compareAndSet(Integer.MAX_VALUE, initialValue);
        return counter.getAndIncrement();
    }
}
