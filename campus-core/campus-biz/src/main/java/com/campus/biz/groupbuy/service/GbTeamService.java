package com.campus.biz.groupbuy.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.biz.groupbuy.entity.GbTeam;

import java.util.List;

/**
 * 拼团组队 Service
 */
public interface GbTeamService extends IService<GbTeam> {

    /**
     * 后台分页（可按活动/状态筛选）
     */
    Page<GbTeam> adminPage(Integer page, Integer pageSize, Long activityId, Integer status);

    /**
     * 查询某活动下仍可加入的拼团中组队（未满、未超时）
     */
    List<GbTeam> listJoinable(Long activityId);
}
