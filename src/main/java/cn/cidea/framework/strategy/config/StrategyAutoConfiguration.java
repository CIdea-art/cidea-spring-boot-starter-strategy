// package cn.cidea.framework.strategy.config;
//
// import cn.cidea.framework.strategy.core.StrategyScannerRegistrar;
// import cn.cidea.framework.strategy.core.annotation.StrategyAPI;
// import cn.cidea.framework.strategy.core.factory.StrategyBeanNameGenerator;
// import cn.cidea.framework.strategy.core.scanner.StrategyScannerConfigurer;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.BeanWrapper;
// import org.springframework.beans.BeanWrapperImpl;
// import org.springframework.beans.BeansException;
// import org.springframework.beans.factory.BeanFactory;
// import org.springframework.beans.factory.BeanFactoryAware;
// import org.springframework.beans.factory.support.BeanDefinitionBuilder;
// import org.springframework.beans.factory.support.BeanDefinitionRegistry;
// import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
// import org.springframework.boot.autoconfigure.AutoConfigureAfter;
// import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
// import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
// import org.springframework.core.type.AnnotationMetadata;
// import org.springframework.util.StringUtils;
//
// import java.util.Collections;
// import java.util.HashSet;
// import java.util.List;
// import java.util.Set;
// import java.util.stream.Stream;
//
// /**
//  * // TODO CIdea: 多个Application时会有问题，暂时注释，顺序问题，要等Application和ComponentScan扫完才能加载其它路径的StrategyScan，而这时该自启动类已经加载了
//  * 创建默认的{@link StrategyScannerConfigurer}，扫描和Spring工程一样的路径
//  *
//  * @author CIdea
//  */
// @AutoConfigureAfter(StrategyScannerRegistrar.class)
// @ConditionalOnMissingBean({StrategyScannerRegistrar.class})
// public class StrategyAutoConfiguration implements BeanFactoryAware, ImportBeanDefinitionRegistrar {
//
//     private Logger log = LoggerFactory.getLogger(StrategyAutoConfiguration.class);
//
//     private BeanFactory beanFactory;
//
//     private static final Set<Object> metadataSet = Collections.synchronizedSet(new HashSet<>());
//
//     @Override
//     public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {
//         if (!metadataSet.add(annotationMetadata)) {
//             return;
//         }
//         if (!AutoConfigurationPackages.has(this.beanFactory)) {
//             log.debug("Could not determine auto-configuration package, automatic Strategy scanning disabled.");
//             return;
//         }
//
//         log.debug("Searching for interface annotated with @Strategy");
//         List<String> packages = AutoConfigurationPackages.get(this.beanFactory);
//         if (log.isDebugEnabled()) {
//             packages.forEach(pkg -> log.debug("Using auto-configuration base package '{}'", pkg));
//         }
//
//         BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(StrategyScannerConfigurer.class);
//         builder.addPropertyValue("annotationClass", StrategyAPI.class);
//         builder.addPropertyValue("basePackage", StringUtils.collectionToCommaDelimitedString(packages));
//         builder.addPropertyValue("nameGenerator", new StrategyBeanNameGenerator());
//         BeanWrapper beanWrapper = new BeanWrapperImpl(StrategyScannerConfigurer.class);
//         Stream.of(beanWrapper.getPropertyDescriptors())
//                 .filter(x -> x.getName().equals("lazyInitialization")).findAny()
//                 .ifPresent(x -> builder.addPropertyValue("lazyInitialization", "${mybatis.lazy-initialization:false}"));
//         registry.registerBeanDefinition(StrategyScannerConfigurer.class.getName() + "@" + annotationMetadata.getClassName(), builder.getBeanDefinition());
//     }
//
//     @Override
//     public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
//         this.beanFactory = beanFactory;
//     }
// }
