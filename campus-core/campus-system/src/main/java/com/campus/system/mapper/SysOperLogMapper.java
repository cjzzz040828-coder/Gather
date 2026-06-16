package com.campus.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.system.entity.SysOperLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志Mapper
 */
@Mapper
public interface SysOperLogMapper extends BaseMapper<SysOperLog> {
}
