package com.lb.domain.strategy.service.rule.factory;

import com.lb.domain.strategy.model.entity.RuleActionEntity;
import com.lb.domain.strategy.service.annotation.LogicStrategy;
import com.lb.domain.strategy.service.rule.ILogicFilter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DefaultLogicFactory {

    // 声明一个ConcurrentHashMap类型的logicFilterMap，用于存储逻辑过滤器
    public Map<String, ILogicFilter<?>> logicFilterMap = new ConcurrentHashMap<>();

    /**
     * 构造函数，用于初始化逻辑过滤器映射
     *
     * @param logicFilters 逻辑过滤器列表
     */
    public DefaultLogicFactory(List<ILogicFilter<?>> logicFilters) {
        // 遍历逻辑过滤器列表
        logicFilters.forEach(logic -> {
            // 获取逻辑过滤器的LogicStrategy注解
            LogicStrategy strategy = AnnotationUtils.findAnnotation(logic.getClass(), LogicStrategy.class);
            // 如果策略不为空，则将逻辑过滤器添加到logicFilterMap中
            if (null != strategy) {
                logicFilterMap.put(strategy.logicMode().getCode(), logic);
            }
        });
    }

    /**
     * 获取逻辑过滤器映射
     *
     * @param <T> 泛型类型，继承自RuleActionEntity.RaffleEntity
     * @return 逻辑过滤器映射
     */
    public <T extends RuleActionEntity.RaffleEntity> Map<String, ILogicFilter<T>> openLogicFilter() {
        // 将logicFilterMap强制转换为Map<String, ILogicFilter<T>>类型并返回
        return (Map<String, ILogicFilter<T>>) (Map<?, ?>) logicFilterMap;
    }

    /**
     * 枚举类，定义逻辑模式
     */
    @Getter
    @AllArgsConstructor
    public enum LogicModel {

        // 规则权重模式
        RULE_WIGHT("rule_weight", "【抽奖前规则】根据抽奖权重返回可抽奖范围KEY"),
        // 黑名单规则模式
        RULE_BLACKLIST("rule_blacklist", "【抽奖前规则】黑名单规则过滤，命中黑名单则直接返回"),

        ;

        // 逻辑模式的代码
        private final String code;
        // 逻辑模式的描述信息
        private final String info;

    }

}
