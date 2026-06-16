package com.campus.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.biz.entity.Student;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生表 Mapper
 * 
 * @author campus
 * @date 2026-03-01
 */
@Mapper
public interface StudentMapper extends BaseMapper<Student> {

}
