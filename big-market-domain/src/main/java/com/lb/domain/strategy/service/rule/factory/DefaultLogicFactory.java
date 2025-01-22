package com.lb.domain.strategy.service.rule.factory;

import com.lb.domain.strategy.model.entity.RuleActionEntity;
import com.lb.domain.strategy.service.annotation.LogicStrategy;
import com.lb.domain.strategy.service.rule.ILogicFilter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 规则工厂
 */
@Component
public class DefaultLogicFactory {

    public Map<String, ILogicFilter<?>> logicFilterMap = new ConcurrentHashMap<>();

    public DefaultLogicFactory(List<ILogicFilter<?>> logicFilters) {
        logicFilters.forEach(
                logicFilter -> {
                    LogicStrategy logicStrategy = AnnotationUtils.findAnnotation(logicFilter.getClass(), LogicStrategy.class);
                    if (logicStrategy != null) {
                        logicFilterMap.put(logicStrategy.logicMode().getCode(), logicFilter);
                    }
                }
        );
    }

    public <T extends RuleActionEntity.RaffleEntity> Map<String, ILogicFilter<T>> openLogicFilter() {
        return (Map<String, ILogicFilter<T>>) (Map<?, ?>) logicFilterMap;
    }

    @Getter
    @AllArgsConstructor
    public enum LogicModel {

        RULE_WIGHT("rule_weight", "【抽奖前规则】根据抽奖权重返回可抽奖范围KEY"),
        RULE_BLACKLIST("rule_blacklist", "【抽奖前规则】黑名单规则过滤，命中黑名单则直接返回"),

        ;

        private final String code;
        private final String info;

    }

}
