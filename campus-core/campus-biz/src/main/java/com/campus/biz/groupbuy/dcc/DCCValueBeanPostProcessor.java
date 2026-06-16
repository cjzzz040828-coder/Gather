package com.campus.biz.groupbuy.dcc;

import com.campus.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * DCC 动态配置处理器：扫描 {@link DCCValue} 字段，从 Redis 注入初值，并支持运行期动态刷新。
 *
 * <p>Bean 初始化后：解析注解的 key:default，以 gb:dcc:{key} 读 Redis；Redis 无值则写入默认值；
 * 反射 set 到字段，并登记 (key → 该字段所在 bean 实例 + Field) 以便后续刷新。</p>
 *
 * <p>{@link #updateValue} 改 Redis 后反射回写所有引用该 key 的字段，使配置在服务运行期立即生效。
 * 用于试算链路的活动降级、用户切量、黑名单等运营开关。</p>
 *
 * <p>注意：BeanPostProcessor 不直接构造注入 StringRedisTemplate，而是用 ObjectProvider 懒获取，
 * 避免 Redis 自动配置被过早初始化（否则会触发 not-eligible-for-auto-proxying 告警）。</p>
 */
@Slf4j
@Component
public class DCCValueBeanPostProcessor implements BeanPostProcessor {

    private final ObjectProvider<StringRedisTemplate> redisTemplateProvider;

    /** key -> 引用该配置的字段持有者列表 */
    private final Map<String, List<FieldHolder>> fieldHolders = new ConcurrentHashMap<>();

    private static final String KEY_PREFIX = "gb:dcc:";

    public DCCValueBeanPostProcessor(ObjectProvider<StringRedisTemplate> redisTemplateProvider) {
        this.redisTemplateProvider = redisTemplateProvider;
    }

    private StringRedisTemplate redis() {
        return redisTemplateProvider.getObject();
    }

    private record FieldHolder(Object bean, Field field) {
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // bean 可能是 @Transactional 等生成的 AOP 代理：字段在目标对象上，需取目标类/目标对象操作
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        Object target = AopProxyUtils.getSingletonTarget(bean);
        Object fieldTarget = target != null ? target : bean;
        for (Field field : targetClass.getDeclaredFields()) {
            DCCValue dcc = field.getAnnotation(DCCValue.class);
            if (dcc == null) {
                continue;
            }
            String raw = dcc.value();
            if (raw == null || !raw.contains(":")) {
                throw new BusinessException("@DCCValue 配置格式须为 key:defaultValue, 实际=" + raw);
            }
            int idx = raw.indexOf(':');
            String key = raw.substring(0, idx);
            String defaultValue = raw.substring(idx + 1);

            String redisKey = KEY_PREFIX + key;
            String value = redis().opsForValue().get(redisKey);
            if (value == null) {
                // Redis 无值：以默认值初始化，方便运营在控制台看到当前可调项
                redis().opsForValue().set(redisKey, defaultValue);
                value = defaultValue;
            }

            ReflectionUtils.makeAccessible(field);
            ReflectionUtils.setField(field, fieldTarget, value);

            fieldHolders.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>())
                    .add(new FieldHolder(fieldTarget, field));
            log.info("DCC 注入: key={}, value={}, bean={}", key, value, beanName);
        }
        return bean;
    }

    /**
     * 运行期更新配置：写 Redis + 反射回写所有引用该 key 的字段（立即生效，无需重启）。
     */
    public void updateValue(String key, String value) {
        List<FieldHolder> holders = fieldHolders.get(key);
        if (holders == null || holders.isEmpty()) {
            throw new BusinessException("未找到 DCC 配置项: " + key);
        }
        redis().opsForValue().set(KEY_PREFIX + key, value);
        for (FieldHolder holder : holders) {
            ReflectionUtils.makeAccessible(holder.field());
            ReflectionUtils.setField(holder.field(), holder.bean(), value);
        }
        log.info("DCC 运行期更新: key={}, value={}, 影响字段数={}", key, value, holders.size());
    }
}
