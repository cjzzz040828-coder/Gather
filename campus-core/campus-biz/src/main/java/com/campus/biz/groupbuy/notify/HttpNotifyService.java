package com.campus.biz.groupbuy.notify;

import com.campus.biz.groupbuy.dcc.DCCValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP 通知回调服务：拼团成团/失团结果以 HTTP POST 回调外部商城，完成拼团营销闭环。
 *
 * <p>与 MQ + 本地消息表互补——MQ/本地表保证内部最终一致，HTTP 回调把结果同步给对接的外部商城。
 * 回调地址由 DCC 配置 {@code notifyCallbackUrl} 运行期下发，留空则跳过 HTTP（仅走内部通知）。</p>
 */
@Slf4j
@Service
public class HttpNotifyService {

    /** 外部商城回调地址，由 DCC 运行期配置；留空则不发 HTTP 回调。 */
    @DCCValue("notifyCallbackUrl:")
    private String notifyCallbackUrl;

    private final RestTemplate restTemplate;

    public HttpNotifyService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(3000);
        this.restTemplate = new RestTemplate(factory);
    }

    /**
     * 发送 HTTP 回调。未配置回调地址时跳过，返回 false。
     *
     * @param messageType 消息类型(team_complete/team_fail)
     * @param messageBody 消息体 JSON
     * @return 是否成功发出且对端 2xx
     */
    public boolean callback(String messageType, String messageBody) {
        if (notifyCallbackUrl == null || notifyCallbackUrl.isBlank()) {
            return false;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Gb-Notify-Type", messageType);
            HttpEntity<String> entity = new HttpEntity<>(messageBody, headers);
            ResponseEntity<String> resp = restTemplate.postForEntity(notifyCallbackUrl, entity, String.class);
            boolean ok = resp.getStatusCode().is2xxSuccessful();
            log.info("HTTP 通知回调: type={}, url={}, status={}", messageType, notifyCallbackUrl, resp.getStatusCode());
            return ok;
        } catch (Exception e) {
            log.error("HTTP 通知回调失败: type={}, url={}", messageType, notifyCallbackUrl, e);
            return false;
        }
    }
}
