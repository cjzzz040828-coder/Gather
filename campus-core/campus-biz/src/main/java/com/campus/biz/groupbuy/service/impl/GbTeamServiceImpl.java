package com.campus.biz.groupbuy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.biz.groupbuy.entity.GbTeam;
import com.campus.biz.groupbuy.mapper.GbTeamMapper;
import com.campus.biz.groupbuy.service.GbTeamService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 拼团组队 Service 实现
 */
@Service
public class GbTeamServiceImpl extends ServiceImpl<GbTeamMapper, GbTeam> implements GbTeamService {

    private static final int TEAM_STATUS_GROUPING = 0;

    @Override
    public Page<GbTeam> adminPage(Integer page, Integer pageSize, Long activityId, Integer status) {
        LambdaQueryWrapper<GbTeam> wrapper = new LambdaQueryWrapper<>();
        if (activityId != null) {
            wrapper.eq(GbTeam::getActivityId, activityId);
        }
        if (status != null) {
            wrapper.eq(GbTeam::getStatus, status);
        }
        wrapper.orderByDesc(GbTeam::getId);
        return this.page(new Page<>(page, pageSize), wrapper);
    }

    @Override
    public List<GbTeam> listJoinable(Long activityId) {
        return this.list(new LambdaQueryWrapper<GbTeam>()
                .eq(GbTeam::getActivityId, activityId)
                .eq(GbTeam::getStatus, TEAM_STATUS_GROUPING)
                .gt(GbTeam::getValidEndTime, LocalDateTime.now())
                .apply("lock_count < target_count")
                .orderByDesc(GbTeam::getId));
    }
}
