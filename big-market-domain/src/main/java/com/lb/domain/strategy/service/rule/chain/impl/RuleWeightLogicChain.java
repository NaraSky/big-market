package com.lb.domain.strategy.service.rule.chain.impl;

import com.lb.domain.strategy.repository.IStrategyRepository;
import com.lb.domain.strategy.service.armory.IStrategyDispatch;
import com.lb.domain.strategy.service.rule.chain.AbstractLogicChain;
import com.lb.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * 逻辑责任链：规则权重
 */
@Slf4j
@Component("rule_weight")
public class RuleWeightLogicChain extends AbstractLogicChain {

    @Resource
    private IStrategyRepository strategyRepository;

    @Resource
    private IStrategyDispatch strategyDispatch;

    public Long userScore = 0L;

    @Override
    public Integer logic(String userId, Long strategyId) {
        log.info("责任链模式-规则权重 userId:{} strategyId:{} ruleModel:{}", userId, strategyId, ruleModel());

        String ruleValue = strategyRepository.queryStrategyRuleValue(strategyId, ruleModel());

        Map<Long, String> analyticalValueGroup = getAnalyticalValue(ruleValue);

        if (analyticalValueGroup.isEmpty()) return null;

        List<Long> analyticalSortedKeys = new ArrayList<>(analyticalValueGroup.keySet());
        Collections.sort(analyticalSortedKeys);

        Long nextValue = analyticalSortedKeys.stream()
                .sorted(Comparator.reverseOrder())
                .filter(analyticalSortedKey -> userScore >= analyticalSortedKey)
                .findFirst()
                .orElse(null);

        if (nextValue != null) {
            Integer awardId = strategyDispatch.getRandomAwardId(strategyId, analyticalValueGroup.get(nextValue));
            log.info("抽奖责任链-命中规则权重 userId:{} strategyId:{} ruleModel:{} awardId:{}", userId, strategyId, ruleModel(), awardId);
            return awardId;
        }

        log.info("抽奖责任链-规则权重放行 userId:{} strategyId:{} ruleModel:{}", userId, strategyId, ruleModel());
        return getNext().logic(userId, strategyId);
    }

    /**
     * 根据规则值解析出分析值并存储到Map中
     *
     * @param ruleValue 规则值，格式为"key1:value1 key2:value2 ..."
     * @return 包含解析后的键值对的Map，键为Long类型，值为String类型
     * @throws IllegalArgumentException 如果规则值格式不正确，则抛出此异常
     */
    private Map<Long, String> getAnalyticalValue(String ruleValue) {
        String[] ruleValueGroups = ruleValue.split(Constants.SPACE);
        Map<Long, String> ruleValueMap = new HashMap<>();
        for (String ruleValueKey : ruleValueGroups) {
            if (ruleValueKey == null || ruleValueKey.isEmpty()) {
                return ruleValueMap;
            }

            String[] parts = ruleValueKey.split(Constants.COLON);
            if (parts.length != 2) {
                throw new IllegalArgumentException("规则值格式错误");
            }
            ruleValueMap.put(Long.parseLong(parts[0]), ruleValueKey);
        }
        return ruleValueMap;
    }


    @Override
    protected String ruleModel() {
        return "rule_weight";
    }
}
