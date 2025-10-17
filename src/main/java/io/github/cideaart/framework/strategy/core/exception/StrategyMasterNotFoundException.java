package io.github.cideaart.framework.strategy.core.exception;

/**
 * 未找到主干或主干方法
 *
 * @author: CIdea
 */
public class StrategyMasterNotFoundException extends RuntimeException {

    public StrategyMasterNotFoundException(String message) {
        super(message);
    }

}
