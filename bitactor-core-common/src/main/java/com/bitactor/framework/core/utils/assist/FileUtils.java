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

import java.io.*;

/**
 * 文件操作工具
 *
 * @author WXH
 */
public class FileUtils {

    /**
     * 文件或者目录是否存在
     *
     * @param fullPath 绝对路径
     * @return true：存在 反之不存在
     */
    public static boolean isExist(String fullPath) {
        File file = new File(fullPath);
        return file.exists();
    }

    /**
     * 文件是否存在
     *
     * @param file
     * @return
     */
    public static boolean isExist(File file) {
        return file.exists();
    }

    /**
     * 创建文件及其父目录，如果这个文件存在，直接返回这个文件<br>
     * 此方法不对File对象类型做判断，如果File不存在，无法判断其类型
     *
     * @param file 文件对象
     * @return 文件，若路径为null，返回null
     * @throws RuntimeException IO异常
     */
    public static File touch(File file) {
        if (null == file) {
            return null;
        }
        if (!file.exists()) {
            mkParentDirs(file);
            try {
                file.createNewFile();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return file;
    }

    /**
     * 创建所给文件或目录的父目录
     *
     * @param file 文件或目录
     * @return 父目录
     */
    public static File mkParentDirs(File file) {
        if (null == file) {
            return null;
        }
        return mkdir(file.getParentFile());
    }

    /**
     * 创建文件夹，会递归自动创建其不存在的父文件夹，如果存在直接返回此文件夹<br>
     * 此方法不对File对象类型做判断，如果File不存在，无法判断其类型
     *
     * @param dir 目录
     * @return 创建的目录
     */
    public static File mkdir(File dir) {
        if (dir == null) {
            return null;
        }
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    /**
     * 读取文件中的一行，需要该文件存在
     *
     * @param file
     * @return
     */
    public static String readLine(File file) {
        if (!file.exists()) {
            throw new RuntimeException("File is not exists");
        }
        try (FileInputStream fileInputStream = new FileInputStream(file);
             InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
             BufferedReader reader = new BufferedReader(inputStreamReader)) {
            return reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("File ID readLine failed by IO");
        }
    }

    /**
     * 写入文件中的一行，需要该文件存在
     */
    public static void writeLine(File file, String line) {
        if (!file.exists()) {
            throw new RuntimeException("File is not exists");
        }
        try (FileOutputStream fileOutputStream = new FileOutputStream(file);
             OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
             BufferedWriter writer = new BufferedWriter(outputStreamWriter)) {
            writer.write(line);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("File write failed by IO");
        }
    }

    /**
     * 获取运行的Jar的目录
     *
     * @return
     */
    public static String getRunJarPath() {
        String path = FileUtils.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        if (System.getProperty("os.name").contains("dows")) {
            path = path.substring(1, path.length());
        }
        if (path.contains("jar")) {
            path = path.substring(0, path.lastIndexOf("."));
            return path.substring(0, path.lastIndexOf("/"));
        }
        return path.replace("target/classes/", "");
    }
}
