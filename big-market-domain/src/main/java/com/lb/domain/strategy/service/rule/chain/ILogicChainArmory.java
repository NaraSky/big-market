package com.lb.domain.strategy.service.rule.chain;

/**
 * 责任链装配
 */
public interface ILogicChainArmory {

    ILogicChain next();

    ILogicChain appendNext(ILogicChain next);

}
