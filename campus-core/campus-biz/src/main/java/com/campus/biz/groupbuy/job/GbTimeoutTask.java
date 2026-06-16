package com.campus.biz.groupbuy.job;

import com.campus.biz.groupbuy.service.GbTradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 拼团超时扫描定时任务：周期性收尾超时未成团的团（置失败 + 退单 + 释放库存）。
 *
 * <p>固定间隔（默认 60s，可经 groupbuy.timeout-scan.delay-ms 配置）调 {@link GbTradeService#closeTimeoutTeams()}。
 * 启动类已开 @EnableScheduling。fixedDelay 语义：上一轮执行完毕后再计时，避免重叠。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GbTimeoutTask {

    private final GbTradeService gbTradeService;

    @Scheduled(fixedDelayString = "${groupbuy.timeout-scan.delay-ms:60000}")
    public void scanTimeoutTeams() {
        int closed = gbTradeService.closeTimeoutTeams();
        if (closed > 0) {
            log.info("拼团超时扫描: 收尾 {} 个团", closed);
        }
    }
}
