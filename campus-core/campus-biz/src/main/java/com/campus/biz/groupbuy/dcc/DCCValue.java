package com.campus.biz.groupbuy.dcc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 轻量级 DCC(动态配置中心)注解：标注的字段值由 Redis 动态下发，运行期可改、无需重启。
 *
 * <p>value 形如 {@code "downgradeSwitch:0"}，冒号前为配置 key（实际 Redis key = gb:dcc:{key}），
 * 冒号后为默认值。由 {@link DCCValueBeanPostProcessor} 在 Bean 初始化后注入，
 * 并通过 {@link DCCService#update} 在运行期反射回写所有引用该 key 的字段。</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DCCValue {

    /** 形如 "key:defaultValue" */
    String value();
}
