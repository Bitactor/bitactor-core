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

package com.bitactor.framework.core.utils.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

public class ConsoleInputReader implements Callable<String> {
    public String call() throws IOException {
        // 不直接使用 scanner 防止在调用nextLine() 时被阻塞
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String input;
        do {
            try {
                // 等到我们有数据完成readLine（）
                while (!br.ready()) {
                    // 添加停机检查
                    Thread.sleep(200);
                }
                input = br.readLine();
            } catch (InterruptedException e) {
                return null;
            }
        } while ("".equals(input));
        return input;
    }
}
