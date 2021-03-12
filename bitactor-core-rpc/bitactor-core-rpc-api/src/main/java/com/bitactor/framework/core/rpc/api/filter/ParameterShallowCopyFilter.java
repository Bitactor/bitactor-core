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

package com.bitactor.framework.core.rpc.api.filter;


import com.bitactor.framework.core.logger.Logger;
import com.bitactor.framework.core.rpc.api.RPCRequest;
import com.bitactor.framework.core.rpc.api.RPCResponse;
import com.bitactor.framework.core.utils.cglib.CglibBeanCopierUtils;
import com.bitactor.framework.core.utils.collection.CollectionUtils;
import com.bitactor.framework.core.utils.lang.ClassHelper;
import com.bitactor.framework.core.logger.LoggerFactory;
import com.bitactor.framework.core.utils.lang.ClassUtils;

import java.util.Collection;
import java.util.Map;

/**
 * @author WXH
 */
public class ParameterShallowCopyFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(ParameterShallowCopyFilter.class);

    @Override
    public void doFilterBefore(RPCRequest request) {
        // do nothing
    }

    @Override
    public void doFilterAfter(RPCRequest request, RPCResponse response) {
        if (request == null || response == null) {
            return;
        }
        Object[] originalArgs = request.getInvocation().getArguments();
        Object[] resultArgs = response.getRequest().getInvocation().getArguments();
        if (originalArgs.length != resultArgs.length) {
            logger.error(new RuntimeException("Do filter failed by originalArgs and resultArgs length not equality"));
        }
        for (int i = 0; i < originalArgs.length; i++) {
            if (originalArgs[i] == null) {
                continue;
            }
            // 指定的类型是基本类型或简单类型
            if (ClassUtils.isPrimitive(originalArgs[i].getClass())) {
                continue;
            }
            // 数组类型
            if (ClassHelper.isArrayType(originalArgs[i].getClass())) {
                Object[] o = (Object[]) originalArgs[i];
                Object[] r = (Object[]) resultArgs[i];
                ClassHelper.cloneArray(o, r);
                continue;
            }
            // map类型
            if (originalArgs[i] instanceof Map) {
                Map o = (Map) originalArgs[i];
                Map r = (Map) resultArgs[i];
                CollectionUtils.cloneMap(o, r);
                continue;
            }
            // list类型
            if (originalArgs[i] instanceof Collection) {
                Collection o = (Collection) originalArgs[i];
                Collection r = (Collection) resultArgs[i];
                CollectionUtils.cloneCollection(o, r);
                continue;
            }
            //枚举
            if (originalArgs[i] instanceof Enum) {
                continue;
            }
            // bean
            CglibBeanCopierUtils.copyProperties(originalArgs[i], resultArgs[i]);
        }
    }
}
