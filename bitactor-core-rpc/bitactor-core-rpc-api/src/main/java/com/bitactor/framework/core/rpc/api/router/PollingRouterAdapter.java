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

package com.bitactor.framework.core.rpc.api.router;


import com.bitactor.framework.core.rpc.api.RPCRequest;
import com.bitactor.framework.core.utils.collection.CollectionUtils;
import com.bitactor.framework.core.utils.lang.CycleAtomicInteger;
import com.bitactor.framework.core.net.api.Channel;
import com.bitactor.framework.core.net.api.transport.AbstractClient;

import java.util.List;

/**
 * @author WXH
 */
public class PollingRouterAdapter<CF> implements RouterAdapter<CF> {

    private CycleAtomicInteger lastIndex = new CycleAtomicInteger();

    @Override
    public Channel<CF> routerAdapter(List<AbstractClient<CF>> clients, RPCRequest request) {
        if (CollectionUtils.isEmpty(clients)) {
            return null;
        }
        int nowIndex = lastIndex.next(clients.size());
        AbstractClient<CF> nettyClient = clients.get(nowIndex);
        return nettyClient.getChannel();
    }
}
