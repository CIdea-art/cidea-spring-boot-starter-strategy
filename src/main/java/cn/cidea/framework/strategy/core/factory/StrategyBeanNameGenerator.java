package cn.cidea.framework.strategy.core.factory;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;

/**
 * @author CIdea
 */
public class StrategyBeanNameGenerator extends DefaultBeanNameGenerator {

    private static final String NAME_SUFFIX = "$strategy";

    @Override
    public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
        return super.generateBeanName(definition, registry) + NAME_SUFFIX;
    }
}
