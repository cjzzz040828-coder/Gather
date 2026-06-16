package com.campus.admin.controller.biz;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.campus.biz.groupbuy.entity.GbOrder;
import com.campus.biz.groupbuy.entity.GbTeam;
import com.campus.biz.groupbuy.service.GbOrderService;
import com.campus.biz.groupbuy.service.GbTeamService;
import com.campus.common.result.PageResult;
import com.campus.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 拼团拼单/组队 - 后台监控（只读）
 */
@RestController
@RequestMapping("/biz/gbOrder")
@RequiredArgsConstructor
public class GbOrderAdminController {

    private final GbOrderService gbOrderService;
    private final GbTeamService gbTeamService;

    /**
     * 拼单记录分页
     */
    @GetMapping("/page")
    @SaCheckPermission("biz:gbOrder:list")
    public Result<PageResult<GbOrder>> orderPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status) {
        return Result.ok(PageResult.of(gbOrderService.adminPage(page, pageSize, teamId, userId, status)));
    }

    /**
     * 组队记录分页
     */
    @GetMapping("/team/page")
    @SaCheckPermission("biz:gbOrder:list")
    public Result<PageResult<GbTeam>> teamPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long activityId,
            @RequestParam(required = false) Integer status) {
        return Result.ok(PageResult.of(gbTeamService.adminPage(page, pageSize, activityId, status)));
    }
}
