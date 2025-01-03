package com.lb.domain.strategy.model.entity;

import com.lb.types.common.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrategyEntity {
    /**
     * 抽奖策略ID
     */
    private Long strategyId;
    /**
     * 抽奖策略描述
     */
    private String strategyDesc;
    /**
     * 抽奖规则模型 rule_weight,rule_blacklist
     */
    private String ruleModels;

    /**
     * 获取规则模型数组。
     *
     * @return 返回规则模型数组，如果ruleModels为空或仅包含空白字符，则返回null。
     */
    public String[] getRuleModels() {
        if (StringUtils.isBlank(ruleModels)) return null;
        return ruleModels.split(Constants.SPLIT);
    }

    /**
     * 获取指定规则模型的权重。
     *
     * @return 如果找到规则模型为"rule_weight"则返回该模型的字符串表示，否则返回null。
     */
    public String getRuleWeight() {
        String[] ruleModels = this.getRuleModels();
        if(ruleModels==null)
            return null;
        for (String ruleModel : ruleModels) {
            if ("rule_weight".equals(ruleModel)) return ruleModel;
        }
        return null;
    }
}
