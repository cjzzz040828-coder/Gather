package com.campus.biz.groupbuy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.biz.groupbuy.entity.GbOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 拼单记录 Mapper
 */
@Mapper
public interface GbOrderMapper extends BaseMapper<GbOrder> {
}
