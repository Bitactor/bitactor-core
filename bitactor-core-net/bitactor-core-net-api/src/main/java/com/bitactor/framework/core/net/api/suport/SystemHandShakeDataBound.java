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

package com.bitactor.framework.core.net.api.suport;


import com.bitactor.framework.core.Version;
import com.bitactor.framework.core.config.UrlProperties;
import com.bitactor.framework.core.constant.CommonConstants;
import com.bitactor.framework.core.constant.NetConstants;
import com.bitactor.framework.core.net.api.HandShakeDataBound;
import com.bitactor.framework.core.net.api.transport.HandShakeData;

/**
 * @author WXH
 */
public class SystemHandShakeDataBound implements HandShakeDataBound {


    @Override
    public void buildCustomHandShakeData(HandShakeData handShakeData, UrlProperties url) {
        handShakeData.addSystemParameter(CommonConstants.VERSION_KEY, Version.getVersion());
        handShakeData.addSystemParameter(NetConstants.HEARTBEAT_OPEN_KEY, url.getParameter(NetConstants.HEARTBEAT_OPEN_KEY, NetConstants.DEFAULT_HEARTBEAT_OPEN));
        handShakeData.addSystemParameter(NetConstants.HEARTBEAT_PERIOD_KEY, url.getParameter(NetConstants.HEARTBEAT_PERIOD_KEY, NetConstants.DEFAULT_HEARTBEAT_PERIOD));
        handShakeData.addSystemParameter(NetConstants.HEARTBEAT_TIMEOUT_KEY, url.getParameter(NetConstants.HEARTBEAT_TIMEOUT_KEY, NetConstants.DEFAULT_HEARTBEAT_TIMEOUT));
    }
}
