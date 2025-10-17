package io.github.cideaart.framework.strategy.core.support;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author: CIdea
 */
public class Invocation {

    private final Object bean;

    private final Method method;

    public Invocation(Method method, Object bean) {
        this.method = method;
        this.bean = bean;
    }

    public Object invoke(Object... args) throws Throwable {
        try {
            return method.invoke(bean, args);
        } catch (InvocationTargetException ite) {
            throw ite.getTargetException();
        }
    }
}
