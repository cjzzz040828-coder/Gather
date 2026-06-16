package com.campus.admin.controller.biz;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.campus.biz.groupbuy.dcc.DCCService;
import com.campus.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 拼团动态配置(DCC) - 后台运营（运行期改试算链路的降级/切量/黑名单开关）
 */
@RestController
@RequestMapping("/biz/gbDcc")
@RequiredArgsConstructor
public class GbDccAdminController {

    private final DCCService dccService;

    /**
     * 运行期更新一个 DCC 配置项（立即生效，无需重启）。
     * 可用 key：downgradeSwitch(0/1)、cutRange(0-100)、blackList(逗号分隔 userId)。
     */
    @PostMapping("/update")
    @SaCheckPermission("biz:gbOrder:list")
    public Result<Void> update(@RequestParam String key, @RequestParam String value) {
        dccService.update(key, value);
        return Result.ok();
    }
}
