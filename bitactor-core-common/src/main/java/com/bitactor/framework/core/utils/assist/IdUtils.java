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

import com.bitactor.framework.core.logger.Logger;
import com.bitactor.framework.core.logger.LoggerFactory;
import com.bitactor.framework.core.utils.lang.StringUtils;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

/**
 * @author WXH
 */
public class IdUtils {
    private static final Logger logger = LoggerFactory.getLogger(IdUtils.class);

    /**
     * 获取一个持久化的id,保存该id到classpath下
     *
     * @param fileName
     * @return
     */
    public static String tryGetPersistID(String fileName) {
        String fullFilePath = System.getProperty("user.dir") + File.separator + fileName;
        logger.info("Do method tryGetPersistID full file path: " + fullFilePath);
        System.out.println(fullFilePath);
        File file = new File(fullFilePath);
        if (!FileUtils.isExist(file)) {
            FileUtils.touch(file);
        }
        String idStr = FileUtils.readLine(file);
        if (StringUtils.isBlank(idStr)) {
            idStr = createHostId();
            FileUtils.writeLine(file, idStr);
        }
        return idStr;
    }

    /**
     * 创建一个id host+ "@" + UUID
     *
     * @return
     */
    public static String createHostId() {
        String hostAddress = "UnknownHost";
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        String id = hostAddress + "@" + UUID.randomUUID().toString();
        return id.replace("-","_");
    }
}
