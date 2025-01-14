package com.lb.domain.strategy.service.armory;

import com.lb.domain.strategy.model.entity.StrategyAwardEntity;
import com.lb.domain.strategy.model.entity.StrategyEntity;
import com.lb.domain.strategy.model.entity.StrategyRuleEntity;
import com.lb.domain.strategy.repository.IStrategyRepository;
import com.lb.types.common.Constants;
import com.lb.types.enums.ResponseCode;
import com.lb.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.*;

/**
 * 策略装配库(兵工厂)，负责初始化策略计算
 */
@Slf4j
@Service
public class StrategyArmoryDispatch implements IStrategyArmory, IStrategyDispatch {

    @Resource
    private IStrategyRepository strategyRepository;


    @Override
    public boolean assembleLotteryStrategy(Long strategyId) {
        // 1. 查询策略奖品列表
        List<StrategyAwardEntity> strategyAwardEntities = strategyRepository.queryStrategyAwardList(strategyId);
        // 2. 调用私有方法组装策略
        assembleLotteryStrategy(String.valueOf(strategyId), strategyAwardEntities);

        // 2. 权重策略配置 - 适用于 rule_weight 权重规则配置
        StrategyEntity strategyEntity = strategyRepository.queryStrategyEntityByStrategyId(strategyId);
        // 获取权重规则字符串
        String ruleWeight = strategyEntity.getRuleWeight();
        if (null == ruleWeight) return true;

        // 3. 根据策略ID和权重规则查询策略规则实体
        StrategyRuleEntity strategyRuleEntity = strategyRepository.queryStrategyRule(strategyId, ruleWeight);
        if (null == strategyRuleEntity) {
            throw new AppException(ResponseCode.STRATEGY_RULE_WEIGHT_IS_NULL.getCode(), ResponseCode.STRATEGY_RULE_WEIGHT_IS_NULL.getInfo());
        }
        // 4. 获取权重规则值映射
        Map<String, List<Integer>> ruleWeightValueMap = strategyRuleEntity.getRuleWeightValues();
        Set<String> keys = ruleWeightValueMap.keySet();
        for (String key : keys) {
            // 获取对应键的权重规则值列表
            List<Integer> ruleWeightValues = ruleWeightValueMap.get(key);
            // 克隆策略奖品列表
            ArrayList<StrategyAwardEntity> strategyAwardEntitiesClone = new ArrayList<>(strategyAwardEntities);
            // 移除不在权重规则值列表中的奖品实体
            strategyAwardEntitiesClone.removeIf(entity -> !ruleWeightValues.contains(entity.getAwardId()));
            // 调用私有方法组装策略，并传入策略ID和权重规则键拼接后的字符串作为key
            assembleLotteryStrategy(String.valueOf(strategyId).concat(Constants.UNDERLINE).concat(key), strategyAwardEntitiesClone);
        }

        return true;
    }

    /**
     * 组装策略方法。
     *
     * @param key                策略的唯一标识符。
     * @param strategyAwardEntities 策略奖品实体列表，包含奖品的ID和对应的概率。
     */
    private void assembleLotteryStrategy(String key, List<StrategyAwardEntity> strategyAwardEntities) {
        // 1. 获取最小概率值
        BigDecimal minAwardRate = strategyAwardEntities.stream()
                .map(StrategyAwardEntity::getAwardRate)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        // 2. 获取概率总和
        BigDecimal totalAwardRate = strategyAwardEntities.stream()
                .map(StrategyAwardEntity::getAwardRate)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. 计算概率区间 divide 方法用于将 totalAwardRate 除以 minAwardRate，并且结果保留 0 位小数。RoundingMode.CEILING 表示向上取整。
        BigDecimal rateRange = totalAwardRate.divide(minAwardRate, 0, RoundingMode.CEILING);

        // 4. 生成策略奖品概率查找表「这里指需要在list集合中，存放上对应的奖品占位即可，占位越多等于概率越高」
        List<Integer> strategyAwardSearchRateTable = new ArrayList<>(rateRange.intValue());

        for (StrategyAwardEntity strategyAwardEntity : strategyAwardEntities) {
            Integer awardId = strategyAwardEntity.getAwardId();
            BigDecimal awardRate = strategyAwardEntity.getAwardRate();
            // 计算出每个概率值需要存放到查找表的数量，循环填充
            for (int i = 0; i < rateRange.multiply(awardRate).setScale(0, RoundingMode.CEILING).intValue(); i++) {
                strategyAwardSearchRateTable.add(awardId);
            }
        }

        // 5. 对存储的奖品进行乱序操作
        Collections.shuffle(strategyAwardSearchRateTable);

        // 6. 生成出Map集合，key值，对应的就是后续的概率值。通过概率来获得对应的奖品ID
        Map<Integer, Integer> shuffleStrategyAwardSearchRateTable = new LinkedHashMap<>();
        for (int i = 0; i < strategyAwardSearchRateTable.size(); i++) {
            shuffleStrategyAwardSearchRateTable.put(i, strategyAwardSearchRateTable.get(i));
        }

        // 7. 存储策略奖品概率查找表到数据库中
        strategyRepository.storeStrategyAwardSearchRateTable(key, rateRange.intValue(), shuffleStrategyAwardSearchRateTable);
    }

    @Override
    public Integer getRandomAwardId(Long strategyId) {
        // 分布式部署下，不一定为当前应用做的策略装配。也就是值不一定会保存到本应用，而是分布式应用，所以需要从 Redis 中获取。
        int rateRange = strategyRepository.getRateRange(strategyId);
        // 通过生成的随机值，获取概率值奖品查找表的结果
        return strategyRepository.getStrategyAwardAssemble(String.valueOf(strategyId), new SecureRandom().nextInt(rateRange));

    }

    @Override
    public Integer getRandomAwardId(Long strategyId, String ruleWeightValue) {
        String key = String.valueOf(strategyId).concat(Constants.UNDERLINE).concat(ruleWeightValue);
        int rateRange = strategyRepository.getRateRange(key);
        // 通过生成的随机值，获取概率值奖品查找表的结果
        return strategyRepository.getStrategyAwardAssemble(key, new SecureRandom().nextInt(rateRange));

    }
}
