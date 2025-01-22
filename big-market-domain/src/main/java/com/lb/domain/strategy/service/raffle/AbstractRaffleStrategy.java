package com.lb.domain.strategy.service.raffle;

import com.lb.domain.strategy.model.entity.RaffleAwardEntity;
import com.lb.domain.strategy.model.entity.RaffleFactorEntity;
import com.lb.domain.strategy.model.entity.RuleActionEntity;
import com.lb.domain.strategy.model.entity.StrategyEntity;
import com.lb.domain.strategy.model.valobj.RuleLogicCheckTypeVO;
import com.lb.domain.strategy.repository.IStrategyRepository;
import com.lb.domain.strategy.service.IRaffleStrategy;
import com.lb.domain.strategy.service.armory.IStrategyDispatch;
import com.lb.domain.strategy.service.rule.factory.DefaultLogicFactory;
import com.lb.types.enums.ResponseCode;
import com.lb.types.exception.AppException;
import org.apache.commons.lang3.StringUtils;

/**
 * 抽奖策略抽象类，定义了抽奖的基本流程，并由具体的子类来实现具体规则的处理。
 */
public abstract class AbstractRaffleStrategy implements IRaffleStrategy {

    // 策略仓储服务 -> 提供策略相关的资源（如规则、奖品等）
    protected IStrategyRepository strategyRepository;

    // 策略调度服务 -> 负责具体的抽奖操作，如根据权重或其他规则随机抽取奖品
    protected IStrategyDispatch strategyDispatch;

    /**
     * 构造函数，初始化策略仓储和策略调度服务
     * @param strategyRepository 策略仓储
     * @param strategyDispatch 策略调度
     */
    public AbstractRaffleStrategy(IStrategyRepository strategyRepository, IStrategyDispatch strategyDispatch) {
        this.strategyRepository = strategyRepository;
        this.strategyDispatch = strategyDispatch;
    }

    /**
     * 执行抽奖的流程，包括参数校验、规则过滤、抽奖过程等
     * @param raffleFactorEntity 包含抽奖所需的各种因素，如用户ID、策略ID等
     * @return RaffleAwardEntity 返回最终抽奖结果
     */
    @Override
    public RaffleAwardEntity performRaffle(RaffleFactorEntity raffleFactorEntity) {
        // 1. 参数校验
        String userId = raffleFactorEntity.getUserId();
        Long strategyId = raffleFactorEntity.getStrategyId();
        if (null == strategyId || StringUtils.isBlank(userId)) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }

        // 2. 查询策略实体，获取抽奖所需的策略信息
        StrategyEntity strategy = strategyRepository.queryStrategyEntityByStrategyId(strategyId);

        // 3. 抽奖前 - 规则过滤
        RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> ruleActionEntity =
                this.doCheckRaffleBeforeLogic(RaffleFactorEntity.builder().userId(userId).strategyId(strategyId).build(), strategy.ruleModels());

        // 根据规则过滤的结果决定是否进行抽奖
        if (RuleLogicCheckTypeVO.TAKE_OVER.getCode().equals(ruleActionEntity.getCode())) {
            // 如果是黑名单规则，直接返回固定的奖品ID
            if (DefaultLogicFactory.LogicModel.RULE_BLACKLIST.getCode().equals(ruleActionEntity.getCode())) {
                return RaffleAwardEntity.builder()
                        .awardId(ruleActionEntity.getData().getAwardId())  // 返回黑名单规则中的奖品ID
                        .build();
            } else if (DefaultLogicFactory.LogicModel.RULE_WIGHT.getCode().equals(ruleActionEntity.getRuleModel())) {
                // 如果是权重规则，根据返回的信息进行抽奖
                RuleActionEntity.RaffleBeforeEntity raffleBeforeEntity = ruleActionEntity.getData();
                String ruleWeightValueKey = raffleBeforeEntity.getRuleWeightValueKey();
                Integer awardId = strategyDispatch.getRandomAwardId(strategyId, ruleWeightValueKey);  // 根据权重获取奖品ID
                return RaffleAwardEntity.builder()
                        .awardId(awardId)  // 返回抽中的奖品ID
                        .build();
            }
        }

        // 4. 默认抽奖流程，如果没有符合的规则，随机返回奖品ID
        Integer awardId = strategyDispatch.getRandomAwardId(strategyId);

        return RaffleAwardEntity.builder()
                .awardId(awardId)  // 返回随机抽中的奖品ID
                .build();
    }

    /**
     * 抽象方法，定义了抽奖前的规则过滤逻辑，由具体的子类实现
     * @param raffleFactorEntity 抽奖因素实体
     * @param logics 规则模型数组
     * @return 返回经过规则过滤后的抽奖动作实体
     */
    protected abstract RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> doCheckRaffleBeforeLogic(RaffleFactorEntity raffleFactorEntity, String... logics);
}
