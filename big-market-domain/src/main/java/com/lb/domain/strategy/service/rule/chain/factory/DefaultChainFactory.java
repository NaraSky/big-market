package com.lb.domain.strategy.service.rule.chain.factory;


import com.lb.domain.strategy.model.entity.StrategyEntity;
import com.lb.domain.strategy.repository.IStrategyRepository;
import com.lb.domain.strategy.service.rule.chain.ILogicChain;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class DefaultChainFactory {
    // 逻辑链组，存储所有的逻辑链实现
    private final Map<String, ILogicChain> logicChainGroup;

    // 策略仓库，用于查询策略相关信息
    private final IStrategyRepository strategyRepository;

    // 构造函数，初始化逻辑链组和策略仓库
    public DefaultChainFactory(Map<String, ILogicChain> logicChainGroup, IStrategyRepository strategyRepository) {
        this.logicChainGroup = logicChainGroup;
        this.strategyRepository = strategyRepository;
    }

    // 打开逻辑链，根据策略ID获取对应的逻辑链
    public ILogicChain openLogicChain(Long strategyId) {
        StrategyEntity strategy = strategyRepository.queryStrategyEntityByStrategyId(strategyId);

        String[] ruleModels = strategy.getRuleModels();

        // 如果规则模型为空或长度为0，返回默认的逻辑链
        if (ruleModels == null || ruleModels.length == 0) return logicChainGroup.get("default");

        // 获取第一个规则模型对应的逻辑链
        ILogicChain iLogicChain = logicChainGroup.get(ruleModels[0]);
        ILogicChain current = iLogicChain;

        // 遍历剩余的规则模型，依次将逻辑链连接起来
        for (int i = 1; i < ruleModels.length; i++) {
            ILogicChain nextChain = logicChainGroup.get(ruleModels[i]);
            current = current.appendNext(nextChain);
        }

        // 最后将默认的逻辑链连接到链的末尾
        current.appendNext(logicChainGroup.get("default"));
        return iLogicChain;
    }
}
