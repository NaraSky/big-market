package com.lb.domain.strategy.service.rule.impl;

import com.lb.domain.strategy.model.entity.RuleActionEntity;
import com.lb.domain.strategy.model.entity.RuleMatterEntity;
import com.lb.domain.strategy.model.valobj.RuleLogicCheckTypeVO;
import com.lb.domain.strategy.repository.IStrategyRepository;
import com.lb.domain.strategy.service.annotation.LogicStrategy;
import com.lb.domain.strategy.service.rule.ILogicFilter;
import com.lb.domain.strategy.service.rule.factory.DefaultLogicFactory;
import com.lb.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j  // 日志记录
@Component  // Spring的组件注解，标明这是一个Spring的bean
@LogicStrategy(logicMode = DefaultLogicFactory.LogicModel.RULE_BLACKLIST)  // 通过注解标明该类对应的规则是"黑名单"规则
public class RuleBlackListLogicFilter implements ILogicFilter<RuleActionEntity.RaffleBeforeEntity> {

    @Resource  // 自动注入IStrategyRepository的实例
    private IStrategyRepository repository;

    /**
     * 过滤方法，根据规则逻辑判断用户是否在黑名单中
     * @param ruleMatterEntity 包含规则信息的实体类
     * @return RuleActionEntity 返回过滤后的规则实体
     */
    @Override
    public RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> filter(RuleMatterEntity ruleMatterEntity) {
        log.info("规则过滤-黑名单 userId:{} strategyId:{} ruleModel:{}",
                ruleMatterEntity.getUserId(), ruleMatterEntity.getStrategyId(), ruleMatterEntity.getRuleModel());

        String userId = ruleMatterEntity.getUserId();  // 获取用户ID

        // 查询规则值配置，获取黑名单规则信息
        String ruleValue = repository.queryStrategyRuleValue(ruleMatterEntity.getStrategyId(), ruleMatterEntity.getAwardId(), ruleMatterEntity.getRuleModel());
        String[] splitRuleValue = ruleValue.split(Constants.COLON);  // 用冒号分隔规则值
        Integer awardId = Integer.parseInt(splitRuleValue[0]);  // 获取奖项ID

        // 过滤黑名单用户
        String[] userBlackIds = splitRuleValue[1].split(Constants.SPLIT);  // 获取黑名单用户ID
        for (String userBlackId : userBlackIds) {
            // 如果用户在黑名单中，则返回"禁止抽奖"的规则
            if (userId.equals(userBlackId)) {
                return RuleActionEntity.<RuleActionEntity.RaffleBeforeEntity>builder()
                        .ruleModel(DefaultLogicFactory.LogicModel.RULE_BLACKLIST.getCode())  // 设置规则模型为黑名单规则
                        .data(RuleActionEntity.RaffleBeforeEntity.builder()
                                .strategyId(ruleMatterEntity.getStrategyId())  // 设置策略ID
                                .awardId(awardId)  // 设置奖项ID
                                .build())
                        .code(RuleLogicCheckTypeVO.TAKE_OVER.getCode())  // 规则被接管
                        .info(RuleLogicCheckTypeVO.TAKE_OVER.getInfo())  // 规则接管的描述信息
                        .build();
            }
        }

        // 如果不在黑名单中，则返回"允许抽奖"的规则
        return RuleActionEntity.<RuleActionEntity.RaffleBeforeEntity>builder()
                .code(RuleLogicCheckTypeVO.ALLOW.getCode())  // 规则允许
                .info(RuleLogicCheckTypeVO.ALLOW.getInfo())  // 允许抽奖的描述信息
                .build();
    }

}
