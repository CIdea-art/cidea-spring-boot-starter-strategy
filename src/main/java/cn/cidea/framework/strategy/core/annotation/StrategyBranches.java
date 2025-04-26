package cn.cidea.framework.strategy.core.annotation;


import org.springframework.stereotype.Service;

import java.lang.annotation.*;

/**
 * @author CIdea
 */
@Service
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface StrategyBranches {

    StrategyBranch[] value();

}
