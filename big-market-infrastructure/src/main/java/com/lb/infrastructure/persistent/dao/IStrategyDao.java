package com.lb.infrastructure.persistent.dao;

import com.lb.infrastructure.persistent.po.Strategy;
import com.lb.infrastructure.persistent.po.StrategyAward;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IStrategyDao {

    List<Strategy> queryStrategyList();

    Strategy queryStrategyByStrategyId(Long strategyId);


}
