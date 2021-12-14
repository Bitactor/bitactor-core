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
import com.bitactor.framework.core.logger.Logger;
import com.bitactor.framework.core.logger.LoggerFactory;
import com.bitactor.framework.core.net.api.ChannelBound;
import com.bitactor.framework.core.net.api.ChannelInit;
import com.bitactor.framework.core.net.api.type.NetworkProtocol;
import com.bitactor.framework.core.net.netty.channel.ChannelNettyOptions;
import com.bitactor.framework.core.utils.assist.UrlPropertiesUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;

import java.util.Objects;

/**
 * 服务端启动器抽象类
 *
 * @author WXH
 */
public abstract class AbstractNettyServerStarter<T> extends AbstractNettyStarter<T> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractNettyServerStarter.class);

    private String protocol;
    protected ChannelInit<ChannelNettyOptions> channelInit;

    public AbstractNettyServerStarter(ChannelBound channelBound, ChannelInit<ChannelNettyOptions> channelInit, String protocol) {
        super(channelBound);
        this.channelInit = channelInit;
        this.protocol = protocol;
    }

    protected void printStartLog() {
        logger.info(String.format("[Server has start on port                        ] : %d ", getUrl().getPort()));
        logger.info(String.format("[Server has start on byteOder                    ] : %s ", getChannelBound().getByteOrder()));
        logger.info(String.format("[Server has start network protocol               ] : %s ", protocol + "&" + getUrl().getParameter(RPCConstants.SERVER_DEFINITION_KEY)));
        if (NetworkProtocol.WS.equals(protocol)) {
            logger.info(String.format("[Server has start application ws open ssl        ] : %s ", getUrl().isOpenWsSsl()));
            logger.info(String.format("[Server has start application ws path            ] : %s ", getUrl().getParameter(NetConstants.WS_URL_PATH_KEY, NetConstants.DEFAULT_WS_URL_PATH)));
        }
        logger.info(String.format("[Server has start application protocol           ] : %s ", getUrl().getProtocol()));
        logger.info(String.format("[Server has start application server type id     ] : %s ", getUrl().getGroupAndId()));
        logger.info(String.format("[Server has start application server work thread ] : %s ", getUrl().getPositiveParameter(NetConstants.IO_THREADS_KEY, NetConstants.DEFAULT_IO_THREADS)));
        logger.info(String.format("[Server has start ip limit max                   ] : %s ", getUrl().getParameter(NetConstants.IP_LIMIT_NUM, 0)));
        if (getChannelBound().isOpenHeartbeat()) {
            logger.info(String.format("[Server open heartbeat period                    ] : %d ", getUrl().getParameter(NetConstants.HEARTBEAT_PERIOD_KEY, NetConstants.DEFAULT_HEARTBEAT_PERIOD)));
            logger.info(String.format("[Server open heartbeat timeout                   ] : %d ", getUrl().getParameter(NetConstants.HEARTBEAT_TIMEOUT_KEY, NetConstants.DEFAULT_HEARTBEAT_TIMEOUT)));
        }
        printProto();
    }

    protected void channelOptionInit(ServerBootstrap bootstrap) {
        if (Objects.nonNull(channelInit)) {
            channelInit.init(new ChannelNettyOptions() {
                @Override
                public <T> void option(ChannelOption<T> option, T value) {
                    bootstrap.childOption(option, value);
                }

                @Override
                public <T> T getV(ChannelOption<T> option) {
                    return (T) bootstrap.config().childOptions().get(option);
                }
            });
        }
    }

    private void printProto() {
        if (getUrl().getParameter(NetConstants.PRINT_PROTO_KEY, NetConstants.DEFAULT_PRINT_PROTO)) {
            int headLength = UrlPropertiesUtils.getPortoHeadLength(getUrl());
            String headText = headLength + "(" + ((headLength == 2) ? "short" : ((headLength == 4) ? "int  " : "byte ")) + ")";
            String protoInfo = "\n" +
                    "+----------+------------+-----------+---------+-------------+----------+\n" +
                    "| head     | packetType | protoType | msgId   |  commandId  |\tdata   |\n" +
                    "+----------+------------+-----------+---------+-------------+----------+\n" +
                    "| " + headText + " | 1(byte)    | 1(byte)   | 4(int)  | 4(int)      |  n(byte) |\n" +
                    "+----------+------------+-----------+---------+-------------+----------+";
            logger.info(protoInfo);
        }
    }


    @Override
    public void close() {
        logger.info(String.format("[Server has close on port                        ] : %d ", getUrl().getPort()));
        logger.info(String.format("[Server has close network protocol               ] : %s ", protocol + "&" + getUrl().getParameter(RPCConstants.SERVER_DEFINITION_KEY)));
        logger.info(String.format("[Server has close application protocol           ] : %s ", getUrl().getProtocol()));
        logger.info(String.format("[Server has close application server type id     ] : %s ", getUrl().getGroupAndId()));
        if (getFuture() != null) {
            getFuture().channel().close();
        }
    }

    public ChannelInit<ChannelNettyOptions> getChannelInit() {
        return channelInit;
    }

    protected abstract void addChannelHandler(ServerBootstrap bootstrap) throws Exception;
}
