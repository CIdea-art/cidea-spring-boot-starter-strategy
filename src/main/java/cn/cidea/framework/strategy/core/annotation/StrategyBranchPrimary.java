package cn.cidea.framework.strategy.core.annotation;

import java.lang.annotation.*;

/**
 * 无论类关系如何，key是否冲突，都会覆盖相同key的分支
 * @author CIdea
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface StrategyBranchPrimary {

}
