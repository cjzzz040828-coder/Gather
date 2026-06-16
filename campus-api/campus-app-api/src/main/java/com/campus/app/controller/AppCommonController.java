package com.campus.app.controller;

import com.campus.common.exception.BusinessException;
import com.campus.common.result.Result;
import com.campus.file.entity.SysFile;
import com.campus.file.service.SysFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * App端 - 通用接口（图片上传等，仅需登录，无需文件管理权限）
 */
@RestController
@RequestMapping("/app/common")
@RequiredArgsConstructor
public class AppCommonController {

    private final SysFileService fileService;

    /**
     * 上传图片，返回可访问 URL
     */
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        SysFile sysFile = fileService.uploadImage(file);
        return Result.ok(sysFile.getUrl());
    }
}
