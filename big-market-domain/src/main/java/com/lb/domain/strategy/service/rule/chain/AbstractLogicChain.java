package com.lb.domain.strategy.service.rule.chain;

/**
 * 逻辑责任链抽象类
 */
public abstract class AbstractLogicChain implements ILogicChain {

    private ILogicChain next;

    @Override
    public ILogicChain appendNext(ILogicChain next) {
        this.next = next;
        return this;
    }

    @Override
    public ILogicChain getNext() {
        return next;
    }

    protected abstract String ruleModel();
}
