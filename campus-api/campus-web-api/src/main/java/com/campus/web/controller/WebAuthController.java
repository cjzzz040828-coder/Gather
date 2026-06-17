package com.campus.web.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.campus.auth.LoginRequest;
import com.campus.auth.LoginResult;
import com.campus.auth.LoginStrategyFactory;
import com.campus.auth.enums.ClientType;
import com.campus.auth.enums.LoginType;
import com.campus.common.result.Result;
import com.campus.file.entity.SysFile;
import com.campus.file.service.SysFileService;
import com.campus.system.entity.SysUser;
import com.campus.system.helper.SystemConfigHelper;
import com.campus.system.service.SysUserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PC端认证控制器
 * 支持密码登录、短信登录、三方登录
 */
@RestController
@RequestMapping("/web/auth")
@RequiredArgsConstructor
public class WebAuthController {

    private final LoginStrategyFactory loginStrategyFactory;
    private final SysUserService userService;
    private final SysFileService fileService;
    private final SystemConfigHelper configHelper;

    /**
     * 统一登录接口
     *
     * @param request loginType: password / sms / social
     */
    @PostMapping("/login")
    public Result<LoginResult> login(@RequestBody LoginRequest request) {
        request.setClientType(ClientType.WEB);
        if (request.getLoginType() == null) {
            request.setLoginType(LoginType.PASSWORD);
        }
        LoginResult result = loginStrategyFactory.login(request);
        return Result.ok(result);
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        StpUtil.logout();
        return Result.ok();
    }

    /**
     * 获取个人资料（需登录）
     */
    @GetMapping("/profile")
    public Result<SysUser> profile() {
        SysUser user = userService.getDetail(StpUtil.getLoginIdAsLong());
        if (user != null) {
            user.setPassword(null);
        }
        return Result.ok(user);
    }

    /**
     * 更新个人资料（需登录）
     */
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody SysUser user) {
        userService.updateProfile(StpUtil.getLoginIdAsLong(), user);
        return Result.ok();
    }

    /**
     * 修改密码（需登录）
     */
    @PostMapping("/password")
    public Result<Void> updatePassword(@RequestBody PasswordRequest request) {
        configHelper.validatePassword(request.getNewPassword());
        userService.updatePassword(StpUtil.getLoginIdAsLong(),
                request.getOldPassword(), request.getNewPassword());
        return Result.ok();
    }

    /**
     * 上传头像（需登录即可，不要求文件管理权限）
     */
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        SysFile sysFile = fileService.uploadImage(file);
        return Result.ok(sysFile.getUrl());
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> info() {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = userService.getDetail(userId);
        List<String> roles = userService.getRoleCodes(userId);
        List<String> permissions = userService.getPermissions(userId);

        Map<String, Object> result = new HashMap<>();
        user.setPassword(null);
        result.put("user", user);
        result.put("roles", roles);
        result.put("permissions", permissions);
        return Result.ok(result);
    }

    /**
     * 获取支持的登录方式
     */
    @GetMapping("/login-types")
    public Result<?> loginTypes() {
        return Result.ok(loginStrategyFactory.getRegisteredTypes());
    }

    @Data
    public static class PasswordRequest {
        private String oldPassword;
        private String newPassword;
    }
}
