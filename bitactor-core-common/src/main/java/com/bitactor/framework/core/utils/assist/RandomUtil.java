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

import com.bitactor.framework.core.utils.collection.CollectionUtils;

import java.util.*;

/**
 * 随机工具类
 */
public class RandomUtil {
    /**
     * 随机数产生器
     */
    private static final Random _randomGenerator = new Random();
    /**
     * 随机基数产生器
     */
    private static Random _baseGenerator = null;

    /**
     * 得到一个[min, max]区间内均匀分布的随机整数
     */
    public static int getRandomInt(int min, int max) {
        if (min == max) {
            return min;
        } else if (min > max) {
            int v = max;
            max = min;
            min = v;
        }
        synchronized (_randomGenerator) {
            return min + _randomGenerator.nextInt(max - min + 1);
        }
    }

    /**
     * 得到一个[min, max]区间内均匀分布的随机浮点数
     */
    public static float getRandomFloat(float min, float max) {
        if (min == max) {
            return min;
        } else if (min > max) {
            float v = max;
            max = min;
            min = v;
        }
        synchronized (_randomGenerator) {
            return min + _randomGenerator.nextFloat() * (max - min);
        }
    }

    /**
     * 得到一个[min, max]区间内均匀分布的随机浮点数
     */
    public static Double getRandomDouble(Double min, Double max) {
        if (min == max) {
            return min;
        } else if (min > max) {
            Double v = max;
            max = min;
            min = v;
        }
        synchronized (_randomGenerator) {
            return min + _randomGenerator.nextDouble() * (max - min);
        }
    }

    /**
     * 得到一个[min, max]区间内均匀分布的随机浮点数
     */
    public static long getRandomLong(long min, long max) {
        if (min == max) {
            return min;
        } else if (min > max) {
            long v = max;
            max = min;
            min = v;
        }
        synchronized (_randomGenerator) {
            return min + _randomGenerator.nextLong() * (max - min);
        }
    }

    /**
     * 得到一个以normal为中心，在[min,max]区间内呈伪正态分布的随机整数
     *
     * @param normal      中心值，出现概率最大，normal值不一定是(min+max)/2，介于[min,max]之间即可
     * @param min         最小值
     * @param max         最大值
     * @param randomTimes 随机次数，必须大于0，次数越大，正态分布效果越好，一般取12即可
     * @return 伪正态分布的随机整数，包括min和max
     */
    public static int getNormalRandomInt(int normal, int min, int max, int randomTimes) {
        //获得多次随机结果
        double value = 0;
        for (int i = 0; i < randomTimes; i++) {
            value += _randomGenerator.nextDouble();
        }
        value = value / randomTimes;
        //按分布获得对应的值
        if (value > 0.5) {
            return normal + (int) Math.round((max - normal) * ((value - 0.5) / 0.5));
        } else if (value < 0.5) {
            return normal - (int) Math.round((normal - min) * ((0.5 - value) / 0.5));
        } else {
            return normal;
        }
    }

    /**
     * 得到一个以normal为中心，在[min,max]区间内呈伪正态分布的随机整数
     *
     * @param normal 中心值，出现概率最大，normal值不一定是(min+max)/2，介于[min,max]之间即可
     * @param min    最小值
     * @param max    最大值
     * @return 伪正态分布的随机整数，包括min和max
     */
    public static int getNormalRandomInt(int normal, int min, int max) {
        return getNormalRandomInt(normal, min, max, 7);
    }

    /**
     * 按机率数组设定的机率进行随机，返回随机到的数组索引<br>
     * 总机率为1，即：chanceArray中的所有机率之和不能超过1<br>
     * 如有3个数a,b,c，机率分别为0.15、0.1、0.2，先对abc进行概率区间分布，a为[0.3,0.45)，b为[0.45,0.55)，c为[0.55,0.75)<br>
     * 然后生成一个[0,1)间的随机数，判断其在哪个数的区间内，并返回该数字，如果不在区间内，返回-1
     *
     * @param chanceArray
     * @return
     */
    public static int getRandomIndexByChance(double[] chanceArray) {
        return -1;
    }

    /**
     * 返回一个不超过maxValue的随机基数
     */
    public static double getRandomBase(double maxValue) {
        if (_baseGenerator == null) {
            _baseGenerator = new Random(System.currentTimeMillis());
        }
        synchronized (_baseGenerator) {
            return _baseGenerator.nextDouble() * maxValue;
        }
    }

    /**
     * 随机判断是否出现了指定的几率，采用双重随机进行计算<br>
     * 即先随机计算一个基数与chance相加，然后随机一个数判断是否在几率区间内<br>
     * 如：chance=0.35，第一次随机[0,0.65]出0.25，那么现在的几率区间为[0.25,0.6]<br>
     * 再进行第二次随机[0,1]，如果出0.5，返回true，出0.8返回false
     *
     * @param chance 出现几率，[0,1]，chance<=0固定返回false，chance>=1固定返回true
     * @return 如果出现该几率返回true，否则返回false
     */
    public static boolean isHitRandomChance(double chance) {
        if (chance <= 0) return false;
        if (chance >= 1) return true;
//		double baseValue = getRandomBase(1 - chance);
//		double randomValue = _randomGenerator.nextDouble();
//		return randomValue >= baseValue && randomValue <= (baseValue + chance);
        return _randomGenerator.nextDouble() >= (1.0 - chance);
    }

    /**
     * 是否命中受统计概率控制的几率
     *
     * @param chance   原始几率，即curTimes=minTimes时的几率，受curTimes影响，curTimes越大，几率越高
     * @param minTimes 命中几率的最小次数，小于此次数肯定不成功
     * @param maxTimes 命中几率的最大次数，达到此次数肯定成功
     * @param curTimes 当前次数，即：已随机过的次数 + 1
     * @return 是否命中几率
     */
    public static boolean isHitStatChance(float chance, int minTimes, int maxTimes, int curTimes) {
        if (minTimes < 1) minTimes = 1;
        if (maxTimes < minTimes) maxTimes = minTimes;
        if (curTimes < minTimes) return false;
        if (curTimes >= maxTimes) return true;
        chance = chance + (float) (curTimes - minTimes) / (maxTimes - minTimes + 1) * (1f - chance) * 0.3f;
//		System.out.print(Math.round(chance * 100) + " ");
        return isHitRandomChance(chance);
    }

    /**
     * 获得集合c中的一个随机元素，如果c为null或者内容为空，则返回null
     */
    public static <E> E getRandomElement(Collection<E> c) {
        if (c == null) return null;
        int size = c.size();
        if (size == 0) return null;
        int idx = getRandomInt(0, size - 1);
        int i = 0;
        for (E e : c) {
            if (i++ == idx) return e;
        }
        return null;
    }

    /**
     * 从原始数组list中随机抽取randomSize个不重复索引处的元素，并返回随机数组
     *
     * @param list       元素数组，不能为null，该数组内容不会被改变
     * @param randomSize 随机抽取的个数，不能为负数。
     * @return 返回已抽取的随机元素数组，肯定不为null。如果list.size<=randomSize，那么返回原始数组list
     */
    public static <E> List<E> getRandomList(List<E> list, int randomSize) {
        if (randomSize <= 0) return new ArrayList<E>(0);
        if (randomSize >= list.size()) return list;
        ArrayList<E> resultList = new ArrayList<E>(randomSize);
        int[] indexList = getRandomIntArray(0, list.size() - 1, randomSize);
        for (int idx : indexList) {
            resultList.add(list.get(idx));
        }
        return resultList;
    }

    /**
     * 从原始数组list中随机抽取randomSize个不重复索引处的元素，并返回随机数组
     *
     * @param list 元素数组，不能为null，该数组内容不会被改变
     * @return 返回已抽取的随机元素数组，肯定不为null。如果list.size<=randomSize，那么返回原始数组null
     */
    public static <E> E getRandomList(List<E> list) {
        List<E> rList = getRandomList(list, 1);
        if (!CollectionUtils.isEmpty(rList)) {
            return rList.get(0);
        }
        return null;
    }

    /**
     * 生成范围在[minValue,maxValue]间的不重复随机整数数组，数组长度为listSize。
     *
     * @param minValue 最小值，结果可能包括本值
     * @param maxValue 最大值，结果可能包括本值
     * @param listSize 数组长度，不能为负数。
     * @return 生成的随机整数数组，肯定不为null。如果listSize大于等于(maxValue-minValue+1)，那么返回minValue到maxValue的所有数字。
     */
    public static ArrayList<Integer> getRandomIntList(int minValue, int maxValue, int listSize) {
        if (listSize <= 0) return new ArrayList<Integer>(0);
        if (minValue >= maxValue) return new ArrayList<Integer>(0);
        //生成原始数组
        ArrayList<Integer> rawList = new ArrayList<Integer>(maxValue - minValue + 1);
        for (int i = minValue; i <= maxValue; i++) {
            rawList.add(i);
        }
        if (listSize >= rawList.size()) return rawList;
        //生成结果数组
        ArrayList<Integer> resultList = new ArrayList<Integer>(listSize);
        int selectedNum = 0;
        int maxIndex = rawList.size() - 1;
        while (selectedNum < listSize) {
            //随机抽取一个位置
            int idx = getRandomInt(0, maxIndex - selectedNum);
            resultList.add(rawList.get(idx));
            //用最后一个来填充当前数据，最后一个变为无效
            rawList.set(idx, rawList.get(maxIndex - selectedNum));
            selectedNum++;
        }
        return resultList;
    }

    /**
     * 生成范围在[minValue,maxValue]间的不重复随机整数数组，数组长度为listSize。
     *
     * @param minValue  最小值，结果可能包括本值
     * @param maxValue  最大值，结果可能包括本值
     * @param arraySize 数组长度，不能为负数。
     * @return 生成的随机整数数组，肯定不为null。如果listSize大于等于(maxValue-minValue+1)，那么返回minValue到maxValue的所有数字。
     */
    public static int[] getRandomIntArray(int minValue, int maxValue, int arraySize) {
        if (arraySize <= 0) return new int[0];
        if (minValue >= maxValue) return new int[0];
        //生成原始数组
        int[] rawList = new int[maxValue - minValue + 1];
        int rawIdx = 0;
        for (int i = minValue; i <= maxValue; i++) {
            rawList[rawIdx++] = i;
        }
        if (arraySize >= rawList.length) return rawList;
        //生成结果数组
        int[] resultList = new int[arraySize];
        int selectedNum = 0;
        int maxIndex = rawList.length - 1;
        while (selectedNum < arraySize) {
            //随机抽取一个位置
            int idx = getRandomInt(0, maxIndex - selectedNum);
            resultList[selectedNum] = rawList[idx];
            //用最后一个来填充当前数据，最后一个变为无效
            rawList[idx] = rawList[maxIndex - selectedNum];
            selectedNum++;
        }
        return resultList;
    }

    /**
     * 得到一个按加权进行随机的数组索引<br>
     * 如有3个数，权重分别为a、b、c，a+b+c=d，那么有a/d概率得到a，有b/d概率得到b，有c/d概率得到c<br>
     * 即必有一个数字被选中
     *
     * @param weightedArray 随机数的权重数组
     * @return 随机到的数组索引，必为一个有效的数组索引
     */
    public static int getWeightedRandomIndex(int[] weightedArray) {
        if (weightedArray == null) return 0;
        int totalWeight = 0;
        for (int i = 0; i < weightedArray.length; i++) {
            totalWeight += weightedArray[i];
        }
        int randomWeight = RandomUtil.getRandomInt(1, totalWeight);
        for (int i = 0; i < weightedArray.length; i++) {
            randomWeight -= weightedArray[i];
            if (randomWeight <= 0) {
                return i;
            }
        }
        return 0;
    }

    /**
     * 得到一个按加权进行随机的数组索引<br>
     * 如有3个数，权重分别为a、b、c，a+b+c=d，那么有a/d概率得到a，有b/d概率得到b，有c/d概率得到c<br>
     * 即必有一个数字被选中
     *
     * @param weightedMap 随机数的权重数组
     * @return 随机到的Map索引，有可能返回NUll
     */
    public static <T> T getWeightedRandomIndexMap(Map<T, Integer> weightedMap) {
        if (CollectionUtils.isEmpty(weightedMap)) return null;
        int totalWeight = 0;
        for (int i : weightedMap.values()) {
            totalWeight += i;
        }
        int randomWeight = RandomUtil.getRandomInt(1, totalWeight);
        for (T i : weightedMap.keySet()) {
            randomWeight -= weightedMap.get(i);
            if (randomWeight <= 0) {
                return i;
            }
        }
        return null;
    }

    /**
     * 从一个map中按加权取得指定数量的索引数组
     *
     * @param weightedMap value为权重
     * @param size        指定从map中随机的个数
     */
    public static <T> ArrayList<T> getWeightedRandomArray(Map<T, Integer> weightedMap, int size) {
        if (CollectionUtils.isEmpty(weightedMap)) {                    // 检查集合是否为空
            return null;
        }

        Map<T, Integer> cloneMap = CollectionUtils.cloneMap(weightedMap);
        ArrayList<T> result = new ArrayList<T>();
        int i = 0;
        /*
         * 数量必须小于源map中的元素个数
         */
        while (i < size) {
            T obj = getWeightedRandomIndexMap(cloneMap);    // 取得随机出的对象
            result.add(obj);                                // 将对象添加到结果集中
            cloneMap.remove(obj);                            // 从源map中移除
            i++;
            if (CollectionUtils.isEmpty(cloneMap)) {
                break;
            }
        }
        return result;
    }

    /**
     * 取得一个随机的字符串.
     *
     * @param stringLength 字符串的长度
     * @return 长为stringLength的随机的字符串
     */
    public static String getRandomString(int stringLength) {
        return getRandomString("abcdefghijklmnopqrstuvwxyz1234567890", stringLength);
    }

    /**
     * 在指定符chars中生成随机字符串
     */
    public static String getRandomString(int stringLength, String Chars) {
        return getRandomString(Chars, stringLength);
    }

    public static String getRandomString(String chars, int stringLength) {
        if (stringLength <= 0)
            return null;
        Date d = new Date();
        char[] b = new char[stringLength];
        String result = null;
        long l = (long) (Math.random() * d.getTime());
        Random rd = new Random(d.getTime() + l);
        for (int i = 0; i < stringLength; i++) {
            int index = (int) (rd.nextDouble() * chars.length());
            b[i] = chars.charAt(index);
        }
        result = new String(b);
        return result;
    }
}
