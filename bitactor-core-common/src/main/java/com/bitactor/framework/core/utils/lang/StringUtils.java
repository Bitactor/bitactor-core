
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

package com.bitactor.framework.core.utils.lang;

import com.alibaba.fastjson.JSON;
import com.bitactor.framework.core.constant.CommonConstants;
import com.bitactor.framework.core.constant.StrConstants;
import com.bitactor.framework.core.logger.Logger;
import com.bitactor.framework.core.logger.LoggerFactory;
import com.bitactor.framework.core.utils.io.UnsafeStringWriter;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * StringUtils
 */

public final class StringUtils {

    public static final String[] EMPTY_STRING_ARRAY = new String[0];
    private static final Logger logger = LoggerFactory.getLogger(StringUtils.class);
    private static final Pattern KVP_PATTERN = Pattern.compile("([_.a-zA-Z0-9][-_.a-zA-Z0-9]*)[=](.*)");

    private static final Pattern INT_PATTERN = Pattern.compile("^\\d+$");

    private StringUtils() {
    }

    public static boolean isBlank(String str) {
        if (str == null || str.length() == 0)
            return true;
        return false;
    }

    /**
     * 是否是空字符串。
     *
     * @param str source string.
     * @return is empty.
     */
    public static boolean isEmpty(String str) {
        if (str == null || str.length() == 0)
            return true;
        return false;
    }

    /**
     * 是否非空字符串.
     *
     * @param str source string.
     * @return is not empty.
     */
    public static boolean isNotEmpty(String str) {
        return str != null && str.length() > 0;
    }

    /**
     * 是否是 int 的字符串。
     *
     * @param str
     * @return is integer
     */
    public static boolean isInteger(String str) {
        if (str == null || str.length() == 0)
            return false;
        return INT_PATTERN.matcher(str).matches();
    }

    /**
     * 解析为int
     *
     * @param str
     * @return
     */
    public static int parseInteger(String str) {
        if (!isInteger(str))
            return 0;
        return Integer.parseInt(str);
    }

    /**
     * 如果是合法的Java标识符，则返回true。
     * <a href="http://www.exampledepot.com/egs/java.lang/IsJavaId.html">more info.</a>
     */
    public static boolean isJavaIdentifier(String s) {
        if (s.length() == 0 || !Character.isJavaIdentifierStart(s.charAt(0))) {
            return false;
        }
        for (int i = 1; i < s.length(); i++) {
            if (!Character.isJavaIdentifierPart(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 是否包含
     *
     * @param values
     * @param value
     * @return
     */
    public static boolean isContains(String values, String value) {
        if (values == null || values.length() == 0) {
            return false;
        }
        return isContains(StrConstants.COMMA_SPLIT_PATTERN.split(values), value);
    }

    /**
     * 数组是否包含value字符串
     *
     * @param values
     * @param value
     * @return contains
     */
    public static boolean isContains(String[] values, String value) {
        if (value != null && value.length() > 0 && values != null && values.length > 0) {
            for (String v : values) {
                if (value.equals(v)) {
                    return true;
                }
            }
        }
        return false;
    }
    /**
     * 指定字符串是否在字符串中出现过
     *
     * @param str       字符串
     * @param searchStr 被查找的字符串
     * @return 是否包含
     */
    public static boolean contains(CharSequence str, CharSequence searchStr) {
        if (null == str || null == searchStr) {
            return false;
        }
        return str.toString().contains(searchStr);
    }
    /**
     * 是否是数字字符串
     *
     * @param str
     * @return
     */
    public static boolean isNumeric(String str) {
        if (str == null) {
            return false;
        }
        int sz = str.length();
        for (int i = 0; i < sz; i++) {
            if (Character.isDigit(str.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }

    /**
     * 转换异常数据
     *
     * @param e
     * @return
     */
    public static String toString(Throwable e) {
        UnsafeStringWriter w = new UnsafeStringWriter();
        PrintWriter p = new PrintWriter(w);
        p.print(e.getClass().getName());
        if (e.getMessage() != null) {
            p.print(": " + e.getMessage());
        }
        p.println();
        try {
            e.printStackTrace(p);
            return w.toString();
        } finally {
            p.close();
        }
    }

    /**
     * 转换异常数据
     *
     * @param msg
     * @param e
     * @return string
     */
    public static String toString(String msg, Throwable e) {
        UnsafeStringWriter w = new UnsafeStringWriter();
        w.write(msg + "\n");
        PrintWriter p = new PrintWriter(w);
        try {
            e.printStackTrace(p);
            return w.toString();
        } finally {
            p.close();
        }
    }


    /**
     * 分割
     *
     * @param ch char.
     * @return string array.
     */
    public static String[] split(String str, char ch) {
        List<String> list = null;
        char c;
        int ix = 0, len = str.length();
        for (int i = 0; i < len; i++) {
            c = str.charAt(i);
            if (c == ch) {
                if (list == null)
                    list = new ArrayList<String>();
                list.add(str.substring(ix, i));
                ix = i + 1;
            }
        }
        if (ix > 0)
            list.add(str.substring(ix));
        return list == null ? EMPTY_STRING_ARRAY : (String[]) list.toArray(EMPTY_STRING_ARRAY);
    }

    /**
     * 连接字符串
     *
     * @param array String array.
     * @return String.
     */
    public static String join(String[] array) {
        if (array.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (String s : array)
            sb.append(s);
        return sb.toString();
    }

    /**
     * 连接字符串
     *
     * @param array String array.
     * @param split split
     * @return String.
     */
    public static String join(String[] array, char split) {
        if (array.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (i > 0)
                sb.append(split);
            sb.append(array[i]);
        }
        return sb.toString();
    }

    /**
     * 连接字符串
     *
     * @param array String array.
     * @param split split
     * @return String.
     */
    public static String join(String[] array, String split) {
        if (array.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (i > 0)
                sb.append(split);
            sb.append(array[i]);
        }
        return sb.toString();
    }

    /**
     * 连接字符串
     *
     * @param coll
     * @param split
     * @return
     */
    public static String join(Collection<String> coll, String split) {
        if (coll.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (String s : coll) {
            if (isFirst) isFirst = false;
            else sb.append(split);
            sb.append(s);
        }
        return sb.toString();
    }

    /**
     * @param s1
     * @param s2
     * @return equals
     */
    public static boolean isEquals(String s1, String s2) {
        if (s1 == null && s2 == null)
            return true;
        if (s1 == null || s2 == null)
            return false;
        return s1.equals(s2);
    }

    /**
     * 转换为键值对
     *
     * @param str           string.
     * @param itemSeparator 分隔符.
     * @return key-value map;
     */
    private static Map<String, String> parseKeyValuePair(String str, String itemSeparator) {
        String[] tmp = str.split(itemSeparator);
        Map<String, String> map = new HashMap<String, String>(tmp.length);
        for (int i = 0; i < tmp.length; i++) {
            Matcher matcher = KVP_PATTERN.matcher(tmp[i]);
            if (matcher.matches() == false)
                continue;
            map.put(matcher.group(1), matcher.group(2));
        }
        return map;
    }

    /**
     * 根据转换的键值对查询
     *
     * @param qs
     * @param key
     * @return
     */
    public static String getQueryStringValue(String qs, String key) {
        Map<String, String> map = StringUtils.parseQueryString(qs);
        return map.get(key);
    }

    /**
     * 将查询字符串解析为参数。
     *
     * @param qs query string.
     * @return Parameters instance.
     */
    public static Map<String, String> parseQueryString(String qs) {
        if (qs == null || qs.length() == 0)
            return new HashMap<String, String>();
        return parseKeyValuePair(qs, "\\&");
    }

    /**
     * 转换为定义的查询字符串
     *
     * @param ps
     * @return
     */
    public static String toQueryString(Map<String, String> ps) {
        StringBuilder buf = new StringBuilder();
        if (ps != null && ps.size() > 0) {
            for (Map.Entry<String, String> entry : new TreeMap<String, String>(ps).entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (key != null && key.length() > 0
                        && value != null && value.length() > 0) {
                    if (buf.length() > 0) {
                        buf.append("&");
                    }
                    buf.append(key);
                    buf.append("=");
                    buf.append(value);
                }
            }
        }
        return buf.toString();
    }

    /**
     * 驼峰转换
     *
     * @param camelName
     * @param split
     * @return
     */
    public static String camelToSplitName(String camelName, String split) {
        if (camelName == null || camelName.length() == 0) {
            return camelName;
        }
        StringBuilder buf = null;
        for (int i = 0; i < camelName.length(); i++) {
            char ch = camelName.charAt(i);
            if (ch >= 'A' && ch <= 'Z') {
                if (buf == null) {
                    buf = new StringBuilder();
                    if (i > 0) {
                        buf.append(camelName.substring(0, i));
                    }
                }
                if (i > 0) {
                    buf.append(split);
                }
                buf.append(Character.toLowerCase(ch));
            } else if (buf != null) {
                buf.append(ch);
            }
        }
        return buf == null ? camelName : buf.toString();
    }

    /**
     * 参数转字符串
     *
     * @param args
     * @return
     */
    public static String toArgumentString(Object[] args) {
        StringBuilder buf = new StringBuilder();
        for (Object arg : args) {
            if (buf.length() > 0) {
                buf.append(CommonConstants.COMMA_SEPARATOR);
            }
            if (arg == null || ReflectUtils.isPrimitives(arg.getClass())) {
                buf.append(arg);
            } else {
                try {
                    buf.append(JSON.toJSONString(arg));
                } catch (Exception e) {
                    logger.warn(e.getMessage(), e);
                    buf.append(arg);
                }
            }
        }
        return buf.toString();
    }

    /**
     * urlEncode
     *
     * @param value
     * @return
     */
    public static String urlEncode(String value) {
        if (value == null || value.length() == 0) {
            return "";
        }
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * urlDecode
     *
     * @param value
     * @return
     */
    public static String urlDecode(String value) {
        if (value == null || value.length() == 0) {
            return "";
        }
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
