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

import com.bitactor.framework.core.eventloop.inf.IEventLoop;
import com.bitactor.framework.core.threadpool.NamedThreadFactory;
import com.bitactor.framework.core.utils.lang.ConcurrentHashSet;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author WXH
 */
public class Test {
    public static void main(String[] args) {
        BitactorEventLoopGroup eventLoopGroup = new BitactorEventLoopGroup(5, new NamedThreadFactory("xxx"));
        ConcurrentHashMap<Integer, IEventLoop> bound = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, Integer> counter = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, Set<Integer>> counterId = new ConcurrentHashMap<>();
        for (int i = 1; i <= 100; i++) {
            int finalI = i;
            bound.put(i, eventLoopGroup.next((loops)->{
                return loops[finalI %loops.length];
            }));
        }
        eventLoopGroup.shutdownGracefully(2, 3, TimeUnit.SECONDS);
        for (Map.Entry<Integer, IEventLoop> entry : bound.entrySet()) {
            IEventLoop eventLoop = entry.getValue();
            Integer id = entry.getKey();
            for (int i = 0; i < 10; i++) {
                eventLoop.execute(() -> {
                    String name = Thread.currentThread().getName();
                    System.out.println("线程: " + name + " id: " + id);
                    counter.compute(name, (k, v) -> {
                        if (Objects.isNull(v)) {
                            return 1;
                        }
                        return v + 1;
                    });
                    counterId.compute(name, (k, v) -> {
                        if (Objects.isNull(v)) {
                            v = new ConcurrentHashSet<>();
                        }
                        v.add(id);
                        return v;
                    });
                });
            }
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            bound.get(1).execute(() -> {
                String name = Thread.currentThread().getName();
                System.out.println("Sleep线程: " + Thread.currentThread().getName() + " id: " + 1);
                counter.compute(name, (k, v) -> {
                    if (Objects.isNull(v)) {
                        return 1;
                    }
                    return v + 1;
                });
                counterId.compute(name, (k, v) -> {
                    if (Objects.isNull(v)) {
                        v = new ConcurrentHashSet<>();
                    }
                    v.add(1);
                    return v;
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(counter);
        System.out.println(counterId);

    }
}
