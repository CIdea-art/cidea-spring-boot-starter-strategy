package cn.cidea.framework.strategy.core.annotation;

import cn.cidea.framework.strategy.core.IStrategyRouter;
import cn.cidea.framework.strategy.core.StrategyAPIRegistrar;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;
import java.lang.reflect.Method;

/**
 * 标记策略端口，端口可以是类或接口
 * 被标记的API（可以是类和接口）会生成一个新的代理类注入Spring中，并设为{@link org.springframework.context.annotation.Primary}
 * 主干实现标记{@link StrategyMaster}
 * 分支实现标记{@link StrategyBranch}
 * 分支根据{@link IStrategyRouter#getRouteKeys(Object, Method, Object[], MethodProxy)}、{@link StrategyBranch#value()}匹配
 *
 * 注意除Master以外的子类/实现类不可标记{@link org.springframework.context.annotation.Primary}注解
 * @author CIdea
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(StrategyAPIRegistrar.class)
public @interface StrategyAPI {

    /**
     * 指定策略路由
     */
    Class<? extends IStrategyRouter> router() default IStrategyRouter.class;
}
