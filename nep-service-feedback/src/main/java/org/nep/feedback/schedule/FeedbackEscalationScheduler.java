package org.nep.feedback.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nep.feedback.lock.DistributedLock;
import org.nep.system.service.SupervisionFeedbackService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 工单定时任务调度器
 *
 * <h3>调度策略</h3>
 * <table>
 *   <tr><th>任务</th><th>Cron</th><th>说明</th></tr>
 *   <tr><td>超时工单升级</td><td>每天凌晨 2:00</td><td>扫描 PENDING/ASSIGNED > 72h 的工单并自动升级</td></tr>
 *   <tr><td>冷数据归档</td><td>每天凌晨 3:30</td><td>将 COMPLETED > 90d 的数据标记为归档</td></tr>
 *   <tr><td>超时统计报告</td><td>每天上午 9:00</td><td>输出超时统计日志（可扩展为邮件告警）</td></tr>
 * </table>
 *
 * <h3>分布式锁保护</h3>
 * 使用 Redis 分布式锁确保多实例部署时只有一个实例执行定时任务，
 * 避免重复升级/归档。
 *
 * @author NEP Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FeedbackEscalationScheduler {

    private final SupervisionFeedbackService feedbackService;
    private final DistributedLock distributedLock;

    /** 锁 key */
    private static final String LOCK_ESCALATION = "task:feedback-escalation";
    private static final String LOCK_ARCHIVE = "task:feedback-archive";
    private static final String LOCK_REPORT = "task:feedback-report";

    /** 锁超时：30分钟（留足时间处理大量数据） */
    private static final Duration LOCK_TIMEOUT = Duration.ofMinutes(30);

    /** 每次归档批次大小（防止大事务） */
    private static final int ARCHIVE_BATCH_SIZE = 500;

    // ==================== 定时任务 ====================

    /**
     * 每日凌晨 2:00 — 超时工单自动升级
     * <p>
     * 扫描规则：
     * <ul>
     *   <li>PENDING 状态 > 72h → 升级为 ESCALATED (level 1)</li>
     *   <li>ASSIGNED 状态 > 72h → 升级为 ESCALATED (level 1)</li>
     *   <li>ESCALATED 状态再次 > 72h → escalationLevel++（最高3级）</li>
     * </ul>
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void escalateOverdueFeedback() {
        log.info("====== [定时任务] 超时工单升级扫描 开始 ======");

        String lockId = distributedLock.tryLock(LOCK_ESCALATION, LOCK_TIMEOUT);
        if (lockId == null) {
            log.info("超时工单升级任务已被其他实例执行，跳过");
            return;
        }

        try {
            int count = feedbackService.autoEscalateExpired();
            log.info("超时工单升级完成: {} 个工单已升级", count);
        } catch (Exception e) {
            log.error("超时工单升级任务执行失败", e);
        } finally {
            distributedLock.unlock(LOCK_ESCALATION, lockId);
        }

        log.info("====== [定时任务] 超时工单升级扫描 结束 ======");
    }

    /**
     * 每日凌晨 3:30 — 冷数据归档
     * <p>
     * 将 COMPLETED 状态超过 90 天的工单标记为归档（archived=1），
     * 减少活跃数据量，提升查询性能。
     */
    @Scheduled(cron = "0 30 3 * * ?")
    public void archiveCompletedFeedback() {
        log.info("====== [定时任务] 冷数据归档 开始 ======");

        String lockId = distributedLock.tryLock(LOCK_ARCHIVE, LOCK_TIMEOUT);
        if (lockId == null) {
            log.info("冷数据归档任务已被其他实例执行，跳过");
            return;
        }

        try {
            int totalArchived = 0;
            int batchCount;
            // 分批归档，每批 500 条
            do {
                batchCount = feedbackService.autoArchiveCompleted(ARCHIVE_BATCH_SIZE);
                totalArchived += batchCount;
            } while (batchCount == ARCHIVE_BATCH_SIZE);

            log.info("冷数据归档完成: {} 条记录已归档", totalArchived);
        } catch (Exception e) {
            log.error("冷数据归档任务执行失败", e);
        } finally {
            distributedLock.unlock(LOCK_ARCHIVE, lockId);
        }

        log.info("====== [定时任务] 冷数据归档 结束 ======");
    }

    /**
     * 每日上午 9:00 — 超时统计报告
     * <p>
     * 输出当前超时工单概览，可扩展为邮件/钉钉/企微告警。
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void overdueReport() {
        String lockId = distributedLock.tryLock(LOCK_REPORT, Duration.ofMinutes(5));
        if (lockId == null) return;

        try {
            var stats = feedbackService.getEscalationStats();
            log.info("====== 每日超时工单统计报告 ======");
            log.info("  时间: {}", LocalDateTime.now());
            log.info("  状态分布: {}", stats.get("byStatus"));
            log.info("  超时工单数: {} (>72h)", stats.get("overdueCount"));
            log.info("  升级阈值: {} 小时", stats.get("overdueThresholdHours"));
            log.info("===================================");
        } catch (Exception e) {
            log.error("超时报告生成失败", e);
        } finally {
            distributedLock.unlock(LOCK_REPORT, lockId);
        }
    }
}
