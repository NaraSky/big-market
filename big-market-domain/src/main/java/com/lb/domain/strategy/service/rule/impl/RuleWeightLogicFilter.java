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
import java.util.*;

/**
 * 【抽奖前规则】根据抽奖权重返回可抽奖范围KEY
 */
@Slf4j
@Component
@LogicStrategy(logicMode = DefaultLogicFactory.LogicModel.RULE_WIGHT)  // 通过注解标明该类对应的规则是"抽奖权重"规则
public class RuleWeightLogicFilter implements ILogicFilter<RuleActionEntity.RaffleBeforeEntity> {

    @Resource  // 自动注入IStrategyRepository的实例
    private IStrategyRepository repository;

    public Long userScore = 4500L;  // 假设用户当前积分为4500

    /**
     * 过滤方法，根据用户积分和权重规则判断是否允许抽奖
     * @param ruleMatterEntity 包含规则信息的实体类
     * @return RuleActionEntity 返回过滤后的规则实体
     */
    @Override
    public RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> filter(RuleMatterEntity ruleMatterEntity) {
        log.info("规则过滤-权重范围 userId:{} strategyId:{} ruleModel:{}",
                ruleMatterEntity.getUserId(), ruleMatterEntity.getStrategyId(), ruleMatterEntity.getRuleModel());

        String userId = ruleMatterEntity.getUserId();  // 获取用户ID
        Long strategyId = ruleMatterEntity.getStrategyId();  // 获取策略ID
        String ruleValue = repository.queryStrategyRuleValue(ruleMatterEntity.getStrategyId(), ruleMatterEntity.getAwardId(), ruleMatterEntity.getRuleModel());

        // 1. 根据规则值解析用户积分和对应的奖项ID
        Map<Long, String> analyticalValueGroup = getAnalyticalValue(ruleValue);

        if (null == analyticalValueGroup || analyticalValueGroup.isEmpty()) {
            // 如果没有配置有效的规则，直接返回允许抽奖
            return RuleActionEntity.<RuleActionEntity.RaffleBeforeEntity>builder()
                    .code(RuleLogicCheckTypeVO.ALLOW.getCode())  // 规则允许
                    .info(RuleLogicCheckTypeVO.ALLOW.getInfo())  // 允许抽奖的描述信息
                    .build();
        }

        // 2. 将权重值进行排序
        List<Long> analyticalSortedKeys = new ArrayList<>(analyticalValueGroup.keySet());
        Collections.sort(analyticalSortedKeys);

        // 3. 查找最小符合用户积分的权重规则
        Long nextValue = analyticalSortedKeys.stream().filter(key -> userScore >= key).findFirst().orElse(null);

        if (null != nextValue) {
            // 如果找到了符合的权重规则，返回接管的规则
            return RuleActionEntity.<RuleActionEntity.RaffleBeforeEntity>builder()
                    .data(RuleActionEntity.RaffleBeforeEntity.builder()
                            .strategyId(strategyId)  // 设置策略ID
                            .ruleWeightValueKey(analyticalValueGroup.get(nextValue))  // 设置权重值的key
                            .build())
                    .ruleModel(DefaultLogicFactory.LogicModel.RULE_WIGHT.getCode())  // 设置规则模型为权重规则
                    .code(RuleLogicCheckTypeVO.TAKE_OVER.getCode())  // 规则被接管
                    .info(RuleLogicCheckTypeVO.TAKE_OVER.getInfo())  // 规则接管的描述信息
                    .build();
        }

        // 如果找不到符合的规则，返回允许抽奖
        return RuleActionEntity.<RuleActionEntity.RaffleBeforeEntity>builder()
                .code(RuleLogicCheckTypeVO.ALLOW.getCode())  // 规则允许
                .info(RuleLogicCheckTypeVO.ALLOW.getInfo())  // 允许抽奖的描述信息
                .build();
    }

    /**
     * 解析规则值，将规则值转化为Map，以便判断用户积分是否符合条件
     * @param ruleValue 规则值字符串
     * @return 解析后的Map，键为积分值，值为规则值字符串
     */
    private Map<Long, String> getAnalyticalValue(String ruleValue) {
        String[] ruleValueGroups = ruleValue.split(Constants.SPACE);  // 按空格分割规则值
        Map<Long, String> ruleValueMap = new HashMap<>();
        for (String ruleValueKey : ruleValueGroups) {
            // 检查输入是否为空
            if (ruleValueKey == null || ruleValueKey.isEmpty()) {
                return ruleValueMap;
            }
            // 分割规则，获取键值对
            String[] parts = ruleValueKey.split(Constants.COLON);
            if (parts.length != 2) {
                throw new IllegalArgumentException("rule_weight rule_rule invalid input format" + ruleValueKey);  // 异常处理
            }
            ruleValueMap.put(Long.parseLong(parts[0]), ruleValueKey);  // 将规则值解析为Map
        }
        return ruleValueMap;
    }

}
