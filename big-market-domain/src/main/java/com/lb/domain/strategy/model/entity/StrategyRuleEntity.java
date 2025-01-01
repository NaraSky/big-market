package com.lb.domain.strategy.model.entity;

import com.lb.types.common.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StrategyRuleEntity {

    /**
     * 抽奖策略ID
     */
    private Long strategyId;
    /**
     * 抽奖奖品ID【规则类型为策略，则不需要奖品ID】
     */
    private Integer awardId;
    /**
     * 抽象规则类型；1-策略规则、2-奖品规则
     */
    private Integer ruleType;
    /**
     * 抽奖规则类型【rule_random - 随机值计算、rule_lock - 抽奖几次后解锁、rule_luck_award - 幸运奖(兜底奖品)】
     */
    private String ruleModel;
    /**
     * 抽奖规则比值
     */
    private String ruleValue;
    /**
     * 抽奖规则描述
     */
    private String ruleDesc;


    /**
     * 获取规则权重值的方法。
     *
     * @return 一个包含规则权重及其对应值的映射。如果当前规则模型不是"rule_weight"，则返回一个空的HashMap。
     * @throws IllegalArgumentException 如果输入的规则权重格式无效，则抛出此异常。
     */
    public Map<String, List<Integer>> getRuleWeightValues() {
        if (!"rule_weight".equals(ruleModel)) return new HashMap<>();
        String[] ruleValueGroups = ruleValue.split(Constants.SPACE);

        // 初始化结果映射
        Map<String, List<Integer>> resultMap = new HashMap<>();

        // 遍历每个规则值组
        for (String ruleValueGroup : ruleValueGroups) {
            if (ruleValueGroup == null || ruleValueGroup.isEmpty()) {
                continue; // 跳过空字符串，继续处理下一个规则组
            }

            String[] parts = ruleValueGroup.split(Constants.COLON);
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid input format for rule_weight: " + ruleValueGroup);
            }
            String[] valueStrings = parts[1].split(Constants.SPLIT);
            // 初始化一个列表来存储转换后的整数值
            List<Integer> values = new ArrayList<>();

            // 遍历每个值字符串，并将其转换为整数后添加到列表中
            for (String valueString : valueStrings) {
                values.add(Integer.parseInt(valueString));
            }

            resultMap.put(ruleValueGroup, values); // 使用权重部分作为键
        }

        return resultMap;
    }

}
