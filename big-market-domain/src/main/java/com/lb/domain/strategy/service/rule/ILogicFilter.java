package com.lb.domain.strategy.service.rule;

import com.lb.domain.strategy.model.entity.RuleActionEntity;
import com.lb.domain.strategy.model.entity.RuleMatterEntity;

/**
 * 抽奖规则过滤接口
 */
public interface ILogicFilter<T extends RuleActionEntity.RaffleEntity> {

    RuleActionEntity<T> filter(RuleMatterEntity ruleMatterEntity);

}
