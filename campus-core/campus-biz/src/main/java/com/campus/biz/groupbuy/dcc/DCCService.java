package com.campus.biz.groupbuy.dcc;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * DCC 配置服务：供后台接口在运行期调整试算链路的动态开关（降级/切量/黑名单）。
 */
@Service
@RequiredArgsConstructor
public class DCCService {

    private final DCCValueBeanPostProcessor dccProcessor;

    public void update(String key, String value) {
        dccProcessor.updateValue(key, value);
    }
}
