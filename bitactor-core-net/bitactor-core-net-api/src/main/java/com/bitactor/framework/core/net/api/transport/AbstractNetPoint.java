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

package com.bitactor.framework.core.net.api.transport;

import com.bitactor.framework.core.constant.NetConstants;
import com.bitactor.framework.core.exception.NetStartFailureException;
import com.bitactor.framework.core.logger.Logger;
import com.bitactor.framework.core.logger.LoggerFactory;
import com.bitactor.framework.core.net.api.ChannelBound;
import com.bitactor.framework.core.net.api.Endpoint;
import com.bitactor.framework.core.threadpool.NamedThreadFactory;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author WXH
 */
public abstract class AbstractNetPoint implements Endpoint, ChannelBound {
    private static final Logger logger = LoggerFactory.getLogger(AbstractNetPoint.class);
    private final Lock startLock = new ReentrantLock();

    private final Condition done = startLock.newCondition();

    private static final ExecutorService commonPool = Executors.newSingleThreadExecutor(new NamedThreadFactory("Net-Common", true));

    @Override
    public void sync() throws InterruptedException {
        startLock.lock();
        Timer timer = new Timer();
        try {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    startLock.lock();
                    try {
                        if (isStart()) {
                            done.signal();
                        }
                    } finally {
                        startLock.unlock();
                    }

                }
            }, 0, 200);
            if (!isStart()) {
                done.await(NetConstants.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
            }
        } finally {
            startLock.unlock();
            timer.cancel();
        }
        if (!isStart()) {
            throw new NetStartFailureException("net start failure...");
        }
    }

    public ExecutorService getCommonPool() {
        return commonPool;
    }

    protected void signal() {
        startLock.lock();
        try {
            done.signal();
        } finally {
            startLock.unlock();
        }
    }
}
