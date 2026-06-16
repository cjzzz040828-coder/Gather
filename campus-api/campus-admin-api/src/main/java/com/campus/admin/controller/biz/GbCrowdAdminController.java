package com.campus.admin.controller.biz;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.campus.biz.groupbuy.crowd.CrowdTagService;
import com.campus.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 拼团人群标签 - 后台运营（写 Redis BitMap，圈选活动可见/可参与人群）
 */
@RestController
@RequestMapping("/biz/gbCrowd")
@RequiredArgsConstructor
public class GbCrowdAdminController {

    private final CrowdTagService crowdTagService;

    /** 将用户加入人群标签 */
    @PostMapping("/add")
    @SaCheckPermission("biz:gbOrder:list")
    public Result<Void> add(@RequestParam String tag, @RequestParam Long userId) {
        crowdTagService.addUser(tag, userId);
        return Result.ok();
    }

    /** 批量将用户加入人群标签 */
    @PostMapping("/addBatch")
    @SaCheckPermission("biz:gbOrder:list")
    public Result<Void> addBatch(@RequestParam String tag, @RequestBody List<Long> userIds) {
        crowdTagService.addUsers(tag, userIds);
        return Result.ok();
    }

    /** 将用户移出人群标签 */
    @PostMapping("/remove")
    @SaCheckPermission("biz:gbOrder:list")
    public Result<Void> remove(@RequestParam String tag, @RequestParam Long userId) {
        crowdTagService.removeUser(tag, userId);
        return Result.ok();
    }

    /** 查询用户是否命中人群标签 */
    @GetMapping("/contains")
    @SaCheckPermission("biz:gbOrder:list")
    public Result<Boolean> contains(@RequestParam String tag, @RequestParam Long userId) {
        return Result.ok(crowdTagService.contains(tag, userId));
    }
}
