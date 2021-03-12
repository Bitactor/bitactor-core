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

import com.bitactor.framework.core.config.UrlProperties;
import com.bitactor.framework.core.constant.NetConstants;

import java.util.concurrent.*;

public class ThreadPoolFactory {
    public static ExecutorService getJdkTheadPool(UrlProperties url, String threadName) {
        String threadPoolName = url.getParameter(NetConstants.THREAD_POOL_KEY, NetConstants.DEFAULT_THREAD_POOL);
        int threads = url.getParameter(NetConstants.THREADS_KEY, NetConstants.DEFAULT_THREADS);
        if (NetConstants.THREAD_POOL_CACHED.equals(threadPoolName)) {
            return new ThreadPoolExecutor(0, threads,
                    60L, TimeUnit.SECONDS,
                    new SynchronousQueue<Runnable>(),
                    new NamedThreadFactory(threadName));
        } else if (NetConstants.THREAD_POOL_FIXED.equals(threadPoolName)) {
            return new ThreadPoolExecutor(0, threads,
                    60L, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>(),
                    new NamedThreadFactory(threadName));
        } else {
            return Executors.newSingleThreadExecutor(new NamedThreadFactory(threadName));
        }
    }
}
