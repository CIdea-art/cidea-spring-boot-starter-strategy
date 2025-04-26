package cn.cidea.framework.strategy.core.annotation;

import cn.cidea.framework.strategy.core.StrategyScannerRegistrar;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 手动扫描
 * @author CIdea
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(StrategyScannerRegistrar.class)
public @interface StrategyScan {

    @AliasFor("basePackages")
    String[] value() default {};

    @AliasFor("value")
    String[] basePackages() default {};

    Class<?>[] basePackageClasses() default {};

    // Class<? extends BeanNameGenerator> nameGenerator() default StrategyBeanNameGenerator.class;
}
