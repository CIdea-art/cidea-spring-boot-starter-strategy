package cn.cidea.framework.strategy.core.support;

import cn.cidea.framework.strategy.core.annotation.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationConfigurationException;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

/**
 * API注册表
 *
 * @author CIdea
 */
public class StrategyRegistry implements InitializingBean, ApplicationContextAware {

    private Logger log = LoggerFactory.getLogger(StrategyRegistry.class);

    private ApplicationContext applicationContext;

    /**
     * api的masterBean注册表
     */
    private final Map<Class<?>, Object> apiMasterBeans = new HashMap<>();
    /**
     * api的branchBean注册表
     */
    private final Map<Class<?>, Map<String, Object>> apiBranchBeans = new HashMap<>();

    /**
     * 获取masterBean
     *
     * @param clz
     * @return
     */
    public <T> T getMasterBean(Class<T> clz) {
        Object masterBean = apiMasterBeans.get(clz);
        if (masterBean == null && !clz.isInterface()) {
            List<Object> candidateBeanList = new ArrayList<>();
            for (Class<?> api : getApis(clz)) {
                Object candidateBean = apiMasterBeans.get(api);
                if (candidateBean != null) {
                    candidateBeanList.add(candidateBean);
                }
            }
            if (candidateBeanList.size() > 1) {
                String names = candidateBeanList.stream().map(Object::getClass).map(Class::getName).collect(Collectors.joining(","));
                throw new RuntimeException("conflict masterBean api " + clz.getName() + ", list = " + names);
            }
            if (candidateBeanList.size() == 1) {
                masterBean = candidateBeanList.get(0);
            }
        }
        return (T) masterBean;
    }

    public <T> List<T> getBranchBean(Class<T> clz) {
        Collection<Object> collection = Optional.ofNullable(apiBranchBeans.get(clz)).map(Map::values).orElse(new ArrayList<>(0));
        return collection.stream().map(bean -> (T) bean).distinct().collect(Collectors.toList());
    }

    /**
     * 获取routeKey对应的brancheBean
     *
     * @param clz
     * @param routeKey
     * @return
     */
    public <T> T getBranchBean(Class<T> clz, String routeKey) {
        Object branchBean = Optional.ofNullable(apiBranchBeans.get(clz)).map(m -> m.get(routeKey)).orElse(null);
        if (branchBean == null && !clz.isInterface()) {
            for (Class<?> api : getApis(clz)) {
                Map<String, Object> candidateMap = apiBranchBeans.get(api);
                if (candidateMap == null) {
                    continue;
                }
                Object candidateBean = candidateMap.get(routeKey);
                if (candidateBean == null) {
                    continue;
                }
                if (branchBean == null) {
                    branchBean = candidateBean;
                } else {
                    log.warn("conflict branchBean api {}", clz.getName());
                }
            }
        }
        return (T) branchBean;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        registryMasterBean();
        registryBranchBean();
    }

    /**
     * 注册MasterBean
     */
    private void registryMasterBean() {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(StrategyMaster.class);
        for (Object bean : beans.values()) {
            // spring注册表会把branch上级的注解带过来，根据class再校验一次
            if (AnnotationUtils.getAnnotation(AopUtils.getTargetClass(bean), StrategyMaster.class) == null) {
                continue;
            }
            Set<Class<?>> apis = getApis(bean);
            log.info("try registry master, bean = {}, apis = [{}]", AopUtils.getTargetClass(bean).getName(), apis.stream().map(Class::getName).collect(Collectors.joining(",")));
            for (Class<?> api : apis) {
                Object exist = apiMasterBeans.put(api, bean);
                if (exist != null && exist != bean) {
                    throw new AnnotationConfigurationException("strategy `" + api.getName() + "` has duplicate master");
                }
            }
        }
    }

    /**
     * 注册BranchBean
     */
    private void registryBranchBean() {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(StrategyBranch.class);
        for (Object bean : beans.values()) {
            List<StrategyBranch> branchList = getBranchAnnotations(AopUtils.getTargetClass(bean));
            if (CollectionUtils.isEmpty(branchList)) {
                if (bean.getClass().getName().contains("$$EnhancerBySpringCGLIB$$")) {
                    branchList = getBranchAnnotations(bean.getClass().getSuperclass());
                }
            }
            if (CollectionUtils.isEmpty(branchList)) {
                log.error("registry branch error, bean = {}", bean.getClass().getName());
                continue;
            }
            Set<Class<?>> apis = getApis(bean);
            log.info("try registry branch, bean = {}, apis = [{}], keys = [{}]",
                    AopUtils.getTargetClass(bean).getName(),
                    apis.stream().map(Class::getName).collect(Collectors.joining(",")),
                    branchList.stream().flatMap(b -> Arrays.stream(b.value())).filter(StringUtils::isNotBlank).distinct().collect(Collectors.joining(",")));
            if (CollectionUtils.isEmpty(apis)) {
                continue;
            }
            for (StrategyBranch branch : branchList) {
                if (branch.value() == null) {
                    continue;
                }
                for (Class<?> api : apis) {
                    Map<String, Object> branchBeans = apiBranchBeans.computeIfAbsent(api, k -> new HashMap<>());
                    for (String key : branch.value()) {
                        // 用branch的值注册进去
                        Object lastBean = branchBeans.get(key);
                        if (lastBean != null && lastBean != bean) {
                            // 查看是否有继承关系
                            StrategyBranchPrimary lastPrimary = AnnotationUtils.getAnnotation(lastBean.getClass(), StrategyBranchPrimary.class);
                            StrategyBranchPrimary primary = AnnotationUtils.getAnnotation(bean.getClass(), StrategyBranchPrimary.class);
                            if (lastPrimary != null && primary != null) {
                                // 不能两个primary
                                throw new RuntimeException(api.getName() + " strategy branch primary key `" + key + "` conflict");
                            } else if (primary != null) {
                                // 设了primary，继续覆盖
                                log.info("{} strategy branch key `{}`. primary = {}, discard = {}", api.getName(), key, bean.getClass().getName(), lastBean.getClass().getName());
                            } else if (lastPrimary != null) {
                                // 上一个key是primary，跳过
                                log.info("{} strategy branch key `{}`. primary = {}, discard = {}", api.getName(), key, lastBean.getClass().getName(), bean.getClass().getName());
                                continue;
                            } else if (ClassUtils.isAssignableValue(AopUtils.getTargetClass(lastBean), bean)) {
                                // 子类允许覆盖父类
                                log.info("{} strategy branch key `{}` cover. bean = {}, parent = {}", api.getName(), key, bean.getClass().getName(), lastBean.getClass().getName());
                            } else if (ClassUtils.isAssignableValue(AopUtils.getTargetClass(bean), lastBean)) {
                                // 跳过父类
                                log.info("{} strategy branch key `{}` skipped. bean = {}, child = {}", api.getName(), key, bean.getClass().getName(), lastBean.getClass().getName());
                                continue;
                            } else {
                                // log.error("{} strategy branch key `{}` conflict", api.getName(), key);
                                throw new RuntimeException(api.getName() + " strategy branch key `" + key + "` conflict");
                            }
                        }
                        branchBeans.put(key, bean);
                    }
                }
            }
        }
    }

    /**
     * 获取master、branch类的API
     *
     * @param bean
     * @return
     */
    private static Set<Class<?>> getApis(Object bean) {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        return getApis(targetClass);
    }

    private static Set<Class<?>> getApis(Class<?> targetClass) {
        // 目标class的所有直接、间接父类，和实现的接口
        Set<Class<?>> candidateApis = new HashSet<>();
        Class<?> superclass = targetClass;
        while (superclass != null) {
            if (!superclass.getName().startsWith("java.lang")) {
                candidateApis.add(superclass);
            }
            superclass = superclass.getSuperclass();
        }
        candidateApis.addAll(Arrays.asList(ClassUtils.getAllInterfacesForClass(targetClass)));
        // 有StrategyAPI注解标记的API
        Set<Class<?>> apis = candidateApis.stream()
                .filter(candidateApi -> AnnotationUtils.getAnnotation(candidateApi, StrategyAPI.class) != null)
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(apis)) {
            // 如果没有显式的API注解标记，则从候选API里查找是否有在上级包或同级包的，作为默认API
            String packageName = targetClass.getPackage().getName();
            for (Class<?> candidateApi : candidateApis) {
                if (AnnotationUtils.getAnnotation(candidateApi, StrategyMaster.class) != null) {
                    apis.add(candidateApi);
                }
                if (targetClass.equals(candidateApi)) {
                    continue;
                }
                if (packageName.contains(candidateApi.getPackage().getName())) {
                    apis.add(candidateApi);
                }
            }
        }
        return apis;
    }

    private List<StrategyBranch> getBranchAnnotations(Class clazz) {
        List<StrategyBranch> strategyBranchList = new ArrayList<>();
        for (Annotation annotation : clazz.getAnnotations()) {
            if (annotation.annotationType().getName().contains("java.lang")) {
                // 跳过jdk注解
                continue;
            } else if (annotation instanceof StrategyBranch) {
                strategyBranchList.add((StrategyBranch) annotation);
            } else if (annotation instanceof StrategyBranches) {
                // 支持repeatable
                strategyBranchList.addAll(Arrays.asList(((StrategyBranches) annotation).value()));
            } else {
                // 支持复合注解
                strategyBranchList.addAll(getBranchAnnotations(annotation.annotationType()));
            }
        }
        return strategyBranchList;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
