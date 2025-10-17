package io.github.cideaart.framework.strategy.core;

import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * @author CIdea
 */
public interface IStrategyRouter {

    String[] getRouteKeys(Object obj, Method method, Object[] args, MethodProxy methodProxy);

}
