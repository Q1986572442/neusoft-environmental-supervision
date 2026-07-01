package org.nep.content.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 布隆过滤器 & AQI 缓存初始化器
 * <p>
 * 在 Spring 容器完成加载后（ApplicationReadyEvent）执行初始化，
 * 确保数据库连接池、Redis 连接、MyBatis-Plus 等均已就绪。
 * <p>
 * 使用异步事件监听，不阻塞应用启动的主线程。
 *
 * @author NEP Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BloomFilterInitializer {

    private final AqiCacheService aqiCacheService;

    /**
     * 应用启动完成后预热缓存和布隆过滤器
     */
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        log.info("==============================================");
        log.info("  Bloom Filter & AQI Cache Initializing...");
        log.info("==============================================");
        try {
            aqiCacheService.warmUp();
            log.info("Bloom Filter & AQI Cache initialized successfully!");
        } catch (Exception e) {
            log.error("Bloom Filter initialization failed, will lazy-load on first request", e);
        }
        log.info("==============================================");
    }
}
