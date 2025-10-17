package io.github.cideaart.framework.strategy.core.annotation;

import io.github.cideaart.framework.strategy.core.IStrategyRouter;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.stereotype.Service;

import java.lang.annotation.*;
import java.lang.reflect.Method;

/**
 * 策略分支
 * @author CIdea
 */
@Service
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(StrategyBranches.class)
public @interface StrategyBranch {

    /**
     * // TODO CIdea: 支持环境变量占位符{@link org.springframework.beans.factory.annotation.Value}
     * 路由映射值
     * 对应{@link IStrategyRouter#getRouteKeys(Object, Method, Object[], MethodProxy)}的返回值
     */
    String[] value();

}
