package cn.cidea.framework.strategy.core.proxy;

import cn.cidea.framework.strategy.core.IStrategyRouter;
import cn.cidea.framework.strategy.core.annotation.StrategyAPI;
import cn.cidea.framework.strategy.core.exception.StrategyMasterNotFoundException;
import cn.cidea.framework.strategy.core.support.Invocation;
import cn.cidea.framework.strategy.core.support.StrategyCache;
import cn.cidea.framework.strategy.core.support.StrategyRegistry;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 路由代理实现
 *
 * @author CIdea
 */
@Scope("prototype")
public class StrategyProxy implements MethodInterceptor, BeanFactoryAware {

    private final Logger log = LoggerFactory.getLogger(StrategyProxy.class);

    /**
     * 路由API{@link StrategyAPI}
     */
    private final Class<?> api;

    /**
     * 指定路由
     */
    private final Class<? extends IStrategyRouter> routerClass;

    private BeanFactory beanFactory;

    @Autowired
    @Lazy
    private StrategyRegistry registry;

    public StrategyProxy(Class<?> api) {
        Assert.notNull(api, "api not be null");
        this.api = api;
        StrategyAPI annotation = AnnotationUtils.findAnnotation(api, StrategyAPI.class);
        // Assert.notNull(annotation, "can not find annotation @StrategyAPI");
        if (annotation != null) {
            this.routerClass = annotation.router();
        } else {
            this.routerClass = IStrategyRouter.class;
        }
    }

    /**
     * @param obj         执行类
     * @param method      执行方法
     * @param args        参数
     * @param methodProxy 代理方法
     * @return
     * @throws Throwable
     */
    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        if (ReflectionUtils.isObjectMethod(method)) {
            return method.invoke(obj, args);
        }
        Object masterBean = registry.getMasterBean(api);
        log.debug("invoke: {}#{}({})", obj.getClass(), method.getName(), Arrays.toString(method.getParameterTypes()));
        // 获取routeKey。getRouteKeys()是抽象方法，用于重写，提供自定义的获取方案
        IStrategyRouter router = beanFactory.getBean(routerClass);
        Assert.notNull(router, "not bean of IStrategyRoute");

        String[] routeKeys = router.getRouteKeys(obj, method, args, methodProxy);
        if (routeKeys == null) {
            // 默认，避免NPE
            routeKeys = new String[]{};
        }
        log.debug("api = {}, routeKeys = {}", api, Arrays.toString(routeKeys));
        // 待执行bean和method的封装对象
        Invocation invocationToUse = StrategyCache.getCache(routeKeys, api, method);
        // 尝试获取缓存
        if (invocationToUse == null) {
            // 无缓存，尝试匹配branch
            for (String routeKey : routeKeys) {
                if (routeKey == null) {
                    continue;
                }
                Object beanToUse = registry.getBranchBean(api, routeKey);
                if (beanToUse == null) {
                    continue;
                }
                Method methodToUse = MethodUtils.getMatchingAccessibleMethod(
                        beanToUse.getClass(), method.getName(), method.getParameterTypes());
                if (methodToUse == null) {
                    log.info("not found methodToUse.");
                    continue;
                }
                invocationToUse = new Invocation(methodToUse, beanToUse);
                break;
            }
        }
        if (invocationToUse == null) {
            if (masterBean == null) {
                throw new StrategyMasterNotFoundException("strategy `" + api.getName() + "` has not master.");
            }
            log.debug("call master service.");

            Method methodToUse = MethodUtils.getMatchingAccessibleMethod(
                    masterBean.getClass(), method.getName(), method.getParameterTypes());
            if (methodToUse == null) {
                throw new StrategyMasterNotFoundException(api.getName() + " can access method `" + method.getName() + "`.");
            }
            invocationToUse = new Invocation(methodToUse, masterBean);
        }
        StrategyCache.cacheBean(routeKeys, api, method, invocationToUse);

        Object result = invocationToUse.invoke(args);
        log.debug("invoke finished.");
        return result;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
