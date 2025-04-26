package cn.cidea.framework.strategy.core.scanner;

import cn.cidea.framework.strategy.core.annotation.StrategyAPI;
import cn.cidea.framework.strategy.core.factory.StrategyFactoryBean;
import cn.cidea.framework.strategy.core.factory.StrategyBeanNameGenerator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;

import static org.springframework.util.Assert.notNull;

/**
 * 核心配置类{@link #postProcessBeanDefinitionRegistry}
 * 创建{@link ClassPathStrategyScanner}
 * 扫描{@link #annotationClass}注解的策略端口
 * @author CIdea
 */
public class StrategyScannerConfigurer
        implements BeanDefinitionRegistryPostProcessor, InitializingBean, ApplicationContextAware, BeanNameAware {

    private String basePackage;

    private Class<? extends StrategyFactoryBean> factoryBeanClass = StrategyFactoryBean.class;

    private Class<? extends Annotation> annotationClass = StrategyAPI.class;

    private BeanNameGenerator nameGenerator = new StrategyBeanNameGenerator();

    private ApplicationContext applicationContext;

    private String beanName;

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
        // 构建一个Scanner，以下都是设置注解中的信息
        ClassPathStrategyScanner scanner = new ClassPathStrategyScanner(registry, this);
        scanner.setResourceLoader(this.applicationContext);
        // 这里是进行实践的扫描注册操作
        // StringUtils.tokenizeToStringArray是分给数组，匹配,或者;
        scanner.scan(
                StringUtils.tokenizeToStringArray(this.basePackage, ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));
    }

    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        notNull(this.basePackage, "Property 'basePackage' is required");
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public Class<? extends StrategyFactoryBean> getFactoryBeanClass() {
        return factoryBeanClass;
    }

    public void setFactoryBeanClass(Class<? extends StrategyFactoryBean> factoryBeanClass) {
        this.factoryBeanClass = factoryBeanClass;
    }

    public void setNameGenerator(BeanNameGenerator nameGenerator) {
        this.nameGenerator = nameGenerator;
    }

    public BeanNameGenerator getNameGenerator() {
        return nameGenerator;
    }

    public Class<? extends Annotation> getAnnotationClass() {
        return annotationClass;
    }

    public void setAnnotationClass(Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }

}
