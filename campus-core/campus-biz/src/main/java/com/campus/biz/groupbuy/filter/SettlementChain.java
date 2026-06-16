package com.campus.biz.groupbuy.filter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Comparator;
import java.util.List;

/**
 * 结算责任链：按 order 升序依次执行各 Filter
 */
@Component
@RequiredArgsConstructor
public class SettlementChain {

    private final List<SettlementFilter> filters;
    private List<SettlementFilter> orderedFilters;

    @PostConstruct
    public void init() {
        orderedFilters = filters.stream()
                .sorted(Comparator.comparingInt(SettlementFilter::order))
                .toList();
    }

    /**
     * 执行结算链，返回处理后的上下文
     */
    public SettlementContext execute(String outTradeNo) {
        SettlementContext context = new SettlementContext();
        context.setOutTradeNo(outTradeNo);
        for (SettlementFilter filter : orderedFilters) {
            filter.handle(context);
        }
        return context;
    }
}
