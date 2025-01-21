package com.lb.infrastructure.persistent.dao;

import com.lb.infrastructure.persistent.po.Strategy;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 抽奖策略 DAO
 */
@Mapper
public interface IStrategyDao {

    List<Strategy> queryStrategyList();

}
