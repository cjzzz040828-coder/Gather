package com.campus.sms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.sms.entity.SmsLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 短信发送记录 Mapper
 */
@Mapper
public interface SmsLogMapper extends BaseMapper<SmsLog> {
}
