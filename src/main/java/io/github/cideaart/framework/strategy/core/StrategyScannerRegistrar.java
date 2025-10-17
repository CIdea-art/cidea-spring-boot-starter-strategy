package io.github.cideaart.framework.strategy.core;

import io.github.cideaart.framework.strategy.core.annotation.StrategyScan;
import io.github.cideaart.framework.strategy.core.scanner.StrategyScannerConfigurer;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * {@link StrategyScan}扫描
 * @author CIdea
 */
public class StrategyScannerRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes annoAttrs = AnnotationAttributes
                .fromMap(annotationMetadata.getAnnotationAttributes(StrategyScan.class.getName()));
        if (annoAttrs == null) {
            return;
        }
        registerBeanDefinitions(annotationMetadata, annoAttrs, registry, generateBaseBeanName(annotationMetadata, 0));
    }

    void registerBeanDefinitions(AnnotationMetadata annotationMetadata, AnnotationAttributes annoAttrs, BeanDefinitionRegistry registry, String beanName) {

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(StrategyScannerConfigurer.class);

        // 以下都是获取注解信息，并进行相应设置
        // Class<? extends Annotation> annotationClass = annoAttrs.getClass("annotationClass");
        // if (!Annotation.class.equals(annotationClass)) {
        //     builder.addPropertyValue("annotationClass", annotationClass);
        // }
        // // 生成nameGenerator实例
        // Class<? extends BeanNameGenerator> generatorClass = annoAttrs.getClass("nameGenerator");
        // if (!BeanNameGenerator.class.equals(generatorClass)) {
        //     builder.addPropertyValue("nameGenerator", BeanUtils.instantiateClass(generatorClass));
        // }

        // 待扫描包路径集合
        Set<String> basePackages = new HashSet<>();
        basePackages.addAll(
                Arrays.stream(annoAttrs.getStringArray("value")).filter(StringUtils::hasText).collect(Collectors.toList()));
        basePackages.addAll(Arrays.stream(annoAttrs.getStringArray("basePackages")).filter(StringUtils::hasText)
                .collect(Collectors.toList()));
        basePackages.addAll(Arrays.stream(annoAttrs.getClassArray("basePackageClasses")).map(ClassUtils::getPackageName)
                .collect(Collectors.toList()));
        if (basePackages.size() == 0){
            Class<?> introspectedClass = ((StandardAnnotationMetadata) annotationMetadata).getIntrospectedClass();
            String defaultPackageName = introspectedClass.getPackage().getName();
            basePackages.add(defaultPackageName);
        }
        builder.addPropertyValue("basePackage", StringUtils.collectionToCommaDelimitedString(basePackages));

        registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
    }

    private static String generateBaseBeanName(AnnotationMetadata importingClassMetadata, int index) {
        return importingClassMetadata.getClassName() + "#" + StrategyScannerRegistrar.class.getSimpleName() + "#" + index;
    }

}
