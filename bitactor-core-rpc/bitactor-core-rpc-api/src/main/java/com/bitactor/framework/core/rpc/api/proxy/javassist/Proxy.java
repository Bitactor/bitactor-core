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

package com.bitactor.framework.core.rpc.api.proxy.javassist;

import com.bitactor.framework.core.code.ClassGenerator;
import com.bitactor.framework.core.rpc.api.invoker.AbstractInvokerHandler;
import com.bitactor.framework.core.utils.lang.ClassHelper;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author WXH
 */
public class Proxy {

    private static final ConcurrentHashMap<String, Class> proxyMap = new ConcurrentHashMap<String, Class>();
    private static final String PROXYFREFIX = "$Proxy";//生成的代理对象名称前缀
    private static final String PROXYSUFFIX = "Impl";//生成的代理对象名称前缀
    private static AtomicLong WRAPPER_CLASS_COUNTER = new AtomicLong(0);

    public static Object getProxy(AbstractInvokerHandler invokerHandler) throws IllegalAccessException, InstantiationException {
        Class inf = invokerHandler.getInterface();
        if (!inf.isInterface()) {
            throw new Error("need a interface class");
        }

        if (proxyMap.containsKey(inf.getName())) {
            Class implClass = Proxy.proxyMap.get(inf.getName());
            ProxyObject po = (ProxyObject) implClass.newInstance();
            po.setHandler(invokerHandler);
            return po;
        }
        // 代理工厂
        ProxyFactory proxyFactory = new ProxyFactory();
        // 设置需要创建子类的父类
        proxyFactory.setSuperclass(Proxy.makeImpl(inf, ClassHelper.getClassLoader(Proxy.class)));
        Class implClass = proxyFactory.createClass();
        proxyMap.put(inf.getName(), implClass);
        ProxyObject po = (ProxyObject) implClass.newInstance();
        po.setHandler(invokerHandler);
        return po;
    }

    private static <T> Class makeImpl(Class<T> inf, ClassLoader loader) {
        ClassGenerator cc = ClassGenerator.newInstance(loader);
        cc.setClassName(Proxy.BuildPackageName(inf));
        cc.addInterface(inf);
        cc.addDefaultConstructor();
        Method[] methods = inf.getMethods();
        for (Method method : methods) {

            String name = method.getName();
            Class<?> rt = method.getReturnType();
            Class<?>[] pts = method.getParameterTypes();
            StringBuilder body = new StringBuilder();
            //body.append("System.debug.println(\"====do method [" + method.getName() + "]\");");
            if (!Void.TYPE.equals(rt)) {
                //body.append("System.debug.println(\"====return type [" + rt.getName() + "]\");");
                // 如果是原始类型不能返回 null
                if (rt.isPrimitive()) {
                    if (rt.getSimpleName().equals("int")
                            || rt.getSimpleName().equals("long")
                            || rt.getSimpleName().equals("short")
                            || rt.getSimpleName().equals("byte")
                            || rt.getSimpleName().equals("double")
                            || rt.getSimpleName().equals("float")
                    ) {
                        body.append("return ").append(0).append(";");
                    } else if (rt.getSimpleName().equals("char")) {
                        body.append("return ").append("\"\"").append(";");
                    } else if (rt.getSimpleName().equals("boolean")) {
                        body.append("return ").append("false").append(";");
                    }
                } else {
                    body.append("return ").append("null").append(";");
                }
            }
            cc.addMethod(name, method.getModifiers(), rt, pts, body.toString());
        }

        try {
            return cc.toClass();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cc.release();
        }
        return null;
    }

    //获取包名
    private static <T> String BuildPackageName(Class<T> inf) {
        Package aPackage = inf.getPackage();
        return inf.getPackage().getName() + PROXYFREFIX + inf.getSimpleName() + PROXYSUFFIX;
    }


}
