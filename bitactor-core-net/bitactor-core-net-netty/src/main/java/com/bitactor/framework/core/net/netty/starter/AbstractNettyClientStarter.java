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

package com.bitactor.framework.core.net.netty.starter;


import com.bitactor.framework.core.constant.NetConstants;
import com.bitactor.framework.core.constant.RPCConstants;
import com.bitactor.framework.core.utils.lang.StringUtils;
import com.bitactor.framework.core.net.api.ChannelBound;
import com.bitactor.framework.core.net.api.type.NetworkProtocol;
import com.bitactor.framework.core.logger.Logger;
import com.bitactor.framework.core.logger.LoggerFactory;

/**
 * 客户端启动器抽象类
 *
 * @author WXH
 */
public abstract class AbstractNettyClientStarter<T> extends AbstractNettyStarter<T> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractNettyClientStarter.class);

    private String protocol;

    public AbstractNettyClientStarter(ChannelBound channelBound, String protocol) {
        super(channelBound);
        this.protocol = protocol;
    }

    protected void printStartLog() {
        logger.info(String.format("[Client has connect on address                   ] %s ", getUrl().getAddress()));
        logger.info(String.format("[Client has connect on byteOder                  ] %s ", getChannelBound().getByteOrder()));
        String definition = getUrl().getParameter(RPCConstants.SERVER_DEFINITION_KEY);
        if (StringUtils.isNotEmpty(definition)) {
            logger.info(String.format("[Client has connect network protocol             ] %s ", protocol + "[" + definition + "]"));
        } else {
            logger.info(String.format("[Client has connect network protocol             ] %s ", protocol));
        }
        if (NetworkProtocol.WS.equals(protocol)) {
            logger.info(String.format("[Client has connect network ws path              ] %s ", getUrl().getParameter(NetConstants.WS_URL_PATH_KEY, NetConstants.DEFAULT_WS_URL_PATH)));
            logger.info(String.format("[Client has connect network ws is open ssl       ] %s ", getUrl().isOpenWsSsl()));
        }
        if (StringUtils.isNotEmpty(getUrl().getGroup())) {
            logger.info(String.format("[Client has connect application server type id   ] %s ", getUrl().getGroupAndId()));
        }
    }

    @Override
    public void close() {
        logger.info(String.format("[Client has close on address                     ] %s ", getUrl().getAddress()));
        logger.info(String.format("[Client has close network protocol               ] %s ", protocol + "[" + getUrl().getParameter(RPCConstants.SERVER_DEFINITION_KEY) + "]"));
        logger.info(String.format("[Client has close application server type id     ] %s ", getUrl().getGroupAndId()));
        if (getFuture() != null) {
            getFuture().channel().close();
        }
    }

    protected abstract Class<? extends T> getChannelClass();
}
