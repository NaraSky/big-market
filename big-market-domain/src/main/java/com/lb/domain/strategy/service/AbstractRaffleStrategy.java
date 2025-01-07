package com.lb.domain.strategy.service;

import com.lb.domain.strategy.model.entity.RaffleAwardEntity;
import com.lb.domain.strategy.model.entity.RaffleFactorEntity;
import com.lb.domain.strategy.model.entity.RuleActionEntity;
import com.lb.domain.strategy.model.valobj.RuleLogicCheckTypeVO;
import com.lb.domain.strategy.model.valobj.StrategyAwardRuleModelVO;
import com.lb.domain.strategy.repository.IStrategyRepository;
import com.lb.domain.strategy.service.armory.IStrategyDispatch;
import com.lb.domain.strategy.service.rule.chain.ILogicChain;
import com.lb.domain.strategy.service.rule.chain.factory.DefaultChainFactory;
import com.lb.types.enums.ResponseCode;
import com.lb.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * 抽奖策略抽象类，定义抽奖的标准流程
 */
@Slf4j
public abstract class AbstractRaffleStrategy implements IRaffleStrategy {

    // 策略仓储服务 -> domain层像一个大厨，仓储层提供米面粮油
    protected IStrategyRepository repository;
    // 策略调度服务 -> 只负责抽奖处理，通过新增接口的方式，隔离职责，不需要使用方关心或者调用抽奖的初始化
    protected IStrategyDispatch strategyDispatch;

    private DefaultChainFactory defaultChainFactory;

    public AbstractRaffleStrategy(IStrategyRepository repository, IStrategyDispatch strategyDispatch, DefaultChainFactory defaultChainFactory) {
        this.repository = repository;
        this.strategyDispatch = strategyDispatch;
        this.defaultChainFactory = defaultChainFactory;
    }

    /**
     * 执行抽奖操作
     *
     * @param raffleFactorEntity 抽奖因子实体对象，根据入参信息计算抽奖结果
     * @return 抽奖的奖品信息
     */
    @Override
    public RaffleAwardEntity performRaffle(RaffleFactorEntity raffleFactorEntity) {
        // 1. 参数校验
        String userId = raffleFactorEntity.getUserId();
        Long strategyId = raffleFactorEntity.getStrategyId();
        if (null == strategyId || StringUtils.isBlank(userId)) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }

        // 2. 责任链处理抽奖
        ILogicChain iLogicChain = defaultChainFactory.openLogicChain(strategyId);
        Integer awardId = iLogicChain.logic(userId, strategyId);

        // 3. 查询奖品规则「抽奖中（拿到奖品ID时，过滤规则）、抽奖后（扣减完奖品库存后过滤，抽奖中拦截和无库存则走兜底）」
        StrategyAwardRuleModelVO strategyAwardRuleModelVO = repository.queryStrategyAwardRuleModelVO(strategyId, awardId);

        // 4. 抽奖中 - 规则过滤
        RuleActionEntity<RuleActionEntity.RaffleCenterEntity> ruleActionCenterEntity = this.doCheckRaffleCenterLogic(
                RaffleFactorEntity.builder()
                        .userId(userId)
                        .strategyId(strategyId)
                        .awardId(awardId)
                        .build(),
                strategyAwardRuleModelVO.raffleCenterRuleModelList()
        );

        // 如果中奖中规则拦截，则返回兜底奖励
        if (RuleLogicCheckTypeVO.TAKE_OVER.getCode().equals(ruleActionCenterEntity.getCode())) {
            log.info("【临时日志】中奖中规则拦截，通过抽奖后规则 rule_luck_award 走兜底奖励。");
            return RaffleAwardEntity.builder()
                    .awardDesc("中奖中规则拦截，通过抽奖后规则 rule_luck_award 走兜底奖励。")
                    .build();
        }

        // 返回最终的奖品ID
        return RaffleAwardEntity.builder()
                .awardId(awardId)
                .build();
    }

    /**
     * 抽象方法，抽奖前的规则过滤逻辑
     *
     * @param raffleFactorEntity 抽奖因子实体对象
     * @param logics             规则模型数组
     * @return 规则过滤结果
     */
    protected abstract RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> doCheckRaffleBeforeLogic(RaffleFactorEntity raffleFactorEntity, String... logics);

    /**
     * 抽象方法，抽奖中的规则过滤逻辑
     *
     * @param raffleFactorEntity 抽奖因子实体对象
     * @param logics             规则模型数组
     * @return 规则过滤结果
     */
    protected abstract RuleActionEntity<RuleActionEntity.RaffleCenterEntity> doCheckRaffleCenterLogic(RaffleFactorEntity raffleFactorEntity, String... logics);

}