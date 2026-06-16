package com.campus.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.system.entity.SysLoginLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 登录日志Mapper
 */
@Mapper
public interface SysLoginLogMapper extends BaseMapper<SysLoginLog> {
}
