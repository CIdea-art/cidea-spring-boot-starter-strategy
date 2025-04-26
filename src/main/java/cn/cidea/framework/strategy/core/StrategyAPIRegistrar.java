package cn.cidea.framework.strategy.core;

import cn.cidea.framework.strategy.core.annotation.StrategyAPI;
import cn.cidea.framework.strategy.core.factory.StrategyBeanNameGenerator;
import cn.cidea.framework.strategy.core.scanner.StrategyScannerConfigurer;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Stream;

/**
 * 根据{@link StrategyAPI}和{@link cn.cidea.framework.strategy.core.annotation.StrategyMaster}及其上一级接口、父类的路径自动扫描
 * @author CIdea
 */
public class StrategyAPIRegistrar implements ImportBeanDefinitionRegistrar {

    private Logger log = LoggerFactory.getLogger(StrategyAPIRegistrar.class);

    private static final Set<Object> metadataSet = Collections.synchronizedSet(new HashSet<>());

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {
        if (!metadataSet.add(annotationMetadata)) {
            return;
        }
        List<String> packages = new ArrayList<>();
        String className = annotationMetadata.getClassName();
        // 当前class目录
        addPackage(packages, className);
        if (!Object.class.getName().equals(annotationMetadata.getSuperClassName())) {
            addPackage(packages, annotationMetadata.getSuperClassName());
        }
        if (ArrayUtils.isNotEmpty(annotationMetadata.getInterfaceNames())) {
            for (String interfaceName : annotationMetadata.getInterfaceNames()) {
                addPackage(packages, interfaceName);
            }
        }
        log.info("import packages: {}", StringUtils.collectionToCommaDelimitedString(packages));

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(StrategyScannerConfigurer.class);
        builder.addPropertyValue("annotationClass", StrategyAPI.class);
        builder.addPropertyValue("basePackage", StringUtils.collectionToCommaDelimitedString(packages));
        builder.addPropertyValue("nameGenerator", new StrategyBeanNameGenerator());
        BeanWrapper beanWrapper = new BeanWrapperImpl(StrategyScannerConfigurer.class);
        Stream.of(beanWrapper.getPropertyDescriptors())
                .filter(x -> x.getName().equals("lazyInitialization")).findAny()
                .ifPresent(x -> builder.addPropertyValue("lazyInitialization", "${mybatis.lazy-initialization:false}"));
        registry.registerBeanDefinition(StrategyScannerConfigurer.class.getName() + "@" + className, builder.getBeanDefinition());
    }

    private void addPackage(List<String> packages, String className) {
        String next = className.substring(0, className.lastIndexOf('.'));
        for (String pkg : new ArrayList<>(packages)) {
            if (pkg.contains(next)) {
                // next范围更大，移除已有的再添加
                packages.remove(pkg);
                break;
            }
            if (next.contains(pkg)) {
                // pkg范围更大，不添加next
                return;
            }
        }
        packages.add(next);
    }

}
