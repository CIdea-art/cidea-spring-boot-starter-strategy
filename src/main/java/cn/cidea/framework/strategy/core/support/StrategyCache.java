package cn.cidea.framework.strategy.core.support;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author CIdea
 */
public class StrategyCache {

    /**
     * key为null时的默认key值
     * 忘了为啥要这么做了
     */
    private static final String NULL_KEY = "null";
    private static final String[] NULL_KEYS = new String[]{NULL_KEY};

    /**
     * 分支执行方法缓存，(routeKey, 代理的method): 执行bean和执行method的封装对象
     */
    private static final Map<String, Map<Class, Map<Method, Invocation>>> cache = new ConcurrentHashMap<>(16);

    public static void cacheBean(String[] keys, Class bean, Method method, Invocation invocation) {
        if(keys == null || keys.length == 0){
            keys = NULL_KEYS;
        }
        for (String key : keys) {
            cacheBean(key, bean, method, invocation);
        }
    }

    public static void cacheBean(String key, Class bean, Method method, Invocation invocation) {
        if (key == null) {
            key = NULL_KEY;
        }
        Map<Class, Map<Method, Invocation>> beanMap = cache.computeIfAbsent(key, s -> new HashMap<>());
        Map<Method, Invocation> methodMap = beanMap.computeIfAbsent(bean, o -> new HashMap<>());
        methodMap.put(method, invocation);
    }

    public static Invocation getCache(String[] keys, Class bean, Method method) {
        if(keys == null || keys.length == 0){
            keys = NULL_KEYS;
        }
        Invocation invocation;
        for (String key : keys) {
            if ((invocation = getCache(key, bean, method)) != null) {
                return invocation;
            }
        }
        return null;
    }

    public static Invocation getCache(String key, Class bean, Method method) {
        if (key == null) {
            key = NULL_KEY;
        }
        Map<Class, Map<Method, Invocation>> beanMap = cache.computeIfAbsent(key, s -> new HashMap<>());
        Map<Method, Invocation> methodMap = beanMap.computeIfAbsent(bean, o -> new HashMap<>());
        return methodMap.get(method);
    }

    public static void cleanCache(){
        cache.clear();
    }
}
