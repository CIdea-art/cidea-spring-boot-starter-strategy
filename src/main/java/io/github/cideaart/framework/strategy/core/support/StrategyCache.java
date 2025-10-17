package io.github.cideaart.framework.strategy.core.support;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author CIdea
 */
public class StrategyCache {

    /**
     * 分支执行方法缓存，(routeKey, 代理的method): 执行bean和执行method的封装对象
     */
    private static final Map<String, Map<Class, Map<Method, Invocation>>> branchCache = new ConcurrentHashMap<>(16);
    /**
     * 主干执行方法缓存
     */
    private static final Map<Class, Map<Method, Invocation>> masterCache = new ConcurrentHashMap<>(16);

    public static void cacheBranch(String key, Class api, Method method, Invocation invocation) {
        if (key == null) {
            return;
        }
        Map<Class, Map<Method, Invocation>> beanMap = branchCache.computeIfAbsent(key, s -> new ConcurrentHashMap<>());
        Map<Method, Invocation> methodMap = beanMap.computeIfAbsent(api, o -> new ConcurrentHashMap<>());
        methodMap.put(method, invocation);
    }

    public static Invocation getBranch(String[] keys, Class api, Method method) {
        if(keys == null || keys.length == 0){
            return null;
        }
        Invocation invocation;
        for (String key : keys) {
            if ((invocation = getBranch(key, api, method)) != null) {
                return invocation;
            }
        }
        return null;
    }

    public static Invocation getBranch(String key, Class api, Method method) {
        if (key == null) {
            return null;
        }
        Map<Class, Map<Method, Invocation>> beanMap = branchCache.computeIfAbsent(key, s -> new ConcurrentHashMap<>());
        Map<Method, Invocation> methodMap = beanMap.computeIfAbsent(api, o -> new ConcurrentHashMap<>());
        return methodMap.get(method);
    }

    public static void cacheMaster(Class api, Method method, Invocation invocation) {
        Map<Method, Invocation> beanMap = masterCache.computeIfAbsent(api, o -> new ConcurrentHashMap<>());
        beanMap.put(method, invocation);
    }

    public static Invocation getMaster(Class api, Method method) {
        Map<Method, Invocation> beanMap = masterCache.computeIfAbsent(api, o -> new ConcurrentHashMap<>());
        return beanMap.get(method);
    }

}
