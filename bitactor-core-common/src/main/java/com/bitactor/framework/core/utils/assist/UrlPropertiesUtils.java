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

package com.bitactor.framework.core.utils.assist;

import com.bitactor.framework.core.config.UrlProperties;
import com.bitactor.framework.core.constant.CommonConstants;
import com.bitactor.framework.core.constant.NetConstants;
import com.bitactor.framework.core.exception.ErrorCodecException;
import com.bitactor.framework.core.utils.lang.StringUtils;

/**
 * @author WXH
 */
public class UrlPropertiesUtils {
    /**
     * 获取协议头长度
     *
     * @param url
     * @return
     */
    public static int getPortoHeadLength(UrlProperties url) {
        int headLength = url.getParameter(NetConstants.MSG_HEADER_KEY, NetConstants.DEFAULT_MSG_HEADER_SIZE);
        if (!(headLength == NetConstants.BYTES_2_LENGTH || headLength == NetConstants.BYTES_4_LENGTH)) {
            throw new ErrorCodecException("illegality head length: " + headLength + ", make sure byte size support int or short");
        }
        return headLength;
    }

    /**
     * url属性怕配置是否匹配
     *
     * @param consumerUrl
     * @param providerUrl
     * @return
     */
    public static boolean isMatch(UrlProperties consumerUrl, UrlProperties providerUrl) {
        String consumerGroup = consumerUrl.getGroup();
        String providerGroup = providerUrl.getGroup();
        if (!(CommonConstants.ANY_VALUE.equals(consumerGroup) || StringUtils.isEquals(consumerGroup, providerGroup)))
            return false;

        if (!providerUrl.getParameter(CommonConstants.ENABLED_KEY, true)
                && !CommonConstants.ANY_VALUE.equals(consumerUrl.getParameter(CommonConstants.ENABLED_KEY))) {
            return false;
        }

        String consumerVersion = consumerUrl.getParameter(CommonConstants.VERSION_KEY);
        String consumerClassifier = consumerUrl.getParameter(CommonConstants.CLASSIFIER_KEY, CommonConstants.ANY_VALUE);

        String providerVersion = providerUrl.getParameter(CommonConstants.VERSION_KEY);
        String providerClassifier = providerUrl.getParameter(CommonConstants.CLASSIFIER_KEY, CommonConstants.ANY_VALUE);
        return (CommonConstants.ANY_VALUE.equals(consumerGroup) || StringUtils.isEquals(consumerGroup, providerGroup) || StringUtils.isContains(consumerGroup, providerGroup))
                && (CommonConstants.ANY_VALUE.equals(consumerVersion) || StringUtils.isEquals(consumerVersion, providerVersion))
                && (consumerClassifier == null || CommonConstants.ANY_VALUE.equals(consumerClassifier) || StringUtils.isEquals(consumerClassifier, providerClassifier));
    }
}
