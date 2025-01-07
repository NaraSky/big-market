package com.lb.domain.strategy.service.rule.chain.impl;

import com.lb.domain.strategy.repository.IStrategyRepository;
import com.lb.domain.strategy.service.armory.IStrategyDispatch;
import com.lb.domain.strategy.service.rule.chain.AbstractLogicChain;
import com.lb.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 逻辑责任链：黑名单规则
 */
@Slf4j
@Component("rule_blackList")
public class BlackListLogicChain extends AbstractLogicChain {

    @Resource
    private IStrategyRepository strategyRepository;

    @Override
    public Integer logic(String userId, Long strategyId) {
        log.info("责任链模式-黑名单规则 userId:{} strategyId:{} ruleModel:{}", userId, strategyId, ruleModel());
        String ruleValue = strategyRepository.queryStrategyRuleValue(strategyId, ruleModel());
        String[] splitRuleValue = ruleValue.split(Constants.COLON);
        Integer awardId = Integer.parseInt(splitRuleValue[0]);

        String[] userBlackIds = ruleValue.split(Constants.SPLIT);
        for (String userBlackId : userBlackIds) {
            if (userId.equals(userBlackId)) {
                log.info("命中黑名单规则 userId:{} strategyId:{} ruleModel:{} award:{}", userId, strategyId, ruleModel(), awardId);
                return awardId;
            }
        }

        log.info("抽奖责任链-黑名单放行 userId:{} strategyId:{} ruleModel:{}", userId, strategyId, ruleModel());

        return getNext().logic(userId, strategyId);
    }

    @Override
    protected String ruleModel() {
        return "rule_blackList";
    }
}
