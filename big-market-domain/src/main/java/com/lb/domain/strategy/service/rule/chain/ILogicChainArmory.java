package com.lb.domain.strategy.service.rule.chain;

public interface ILogicChainArmory {

    /**
     * 追加下一个责任链
     *
     * @param next 下一个责任链
     * @return 当前责任链
     */
    ILogicChain appendNext(ILogicChain next);

    /**
     * 获取下一个责任链
     *
     * @return 下一个责任链
     */
    ILogicChain getNext();
}
