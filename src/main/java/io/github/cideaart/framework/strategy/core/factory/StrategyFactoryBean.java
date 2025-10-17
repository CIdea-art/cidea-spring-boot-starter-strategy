package io.github.cideaart.framework.strategy.core.factory;

import io.github.cideaart.framework.strategy.core.proxy.StrategyProxy;
import org.springframework.beans.factory.*;
import org.springframework.cglib.core.SpringNamingPolicy;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.util.ClassUtils;

import static org.springframework.util.Assert.notNull;

/**
 * @author CIdea
 */
public class StrategyFactoryBean<T> implements InitializingBean, FactoryBean<T>, BeanFactoryAware {

    /**
     * BeanDefinition的class
     */
    private Class<T> api;

    private BeanFactory beanFactory;

    public StrategyFactoryBean(Class<T> api) {
        this.api = api;
    }

    @Override
    public T getObject() throws Exception {
        // 创建代理类
        StrategyProxy proxy = beanFactory.getBeanProvider(StrategyProxy.class).getObject(api);

        Enhancer enhancer = new Enhancer();
        if(api.isInterface()){
            enhancer.setInterfaces(new Class[]{api});
        } else {
            enhancer.setInterfaces(ClassUtils.getAllInterfacesForClass(api));
            // enhancer.setInterfaces(api.getInterfaces());
            enhancer.setSuperclass(api);
        }
        enhancer.setCallback(proxy);
        enhancer.setCallbackType(proxy.getClass());
        enhancer.setNamingPolicy(SpringNamingPolicy.INSTANCE);
        T instance = (T) enhancer.create();
        return instance;
    }

    @Override
    public Class<?> getObjectType() {
        return this.api;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        notNull(this.api, "Property 'port' is required");
        notNull(this.beanFactory, "Property 'beanFactory' is required");
    }

    public void setApi(Class<T> api) {
        this.api = api;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }
}
