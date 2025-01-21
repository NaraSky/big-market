package com.lb.infrastructure.persistent.dao;

import com.lb.infrastructure.persistent.po.Award;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 奖品表DAO
 */
@Mapper
public interface IAwardDao {

    List<Award> queryAwardList();

}
