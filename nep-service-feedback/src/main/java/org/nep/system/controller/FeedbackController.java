package org.nep.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.nep.common.result.PageResult;
import org.nep.common.result.Result;
import org.nep.system.entity.SupervisionFeedback;
import org.nep.system.service.SupervisionFeedbackService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 监督反馈管理控制器
 *
 * @author NEP Team
 */
@Tag(name = "监督反馈管理")
@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final SupervisionFeedbackService feedbackService;

    // ==================== 基础 CRUD ====================

    @Operation(summary = "提交监督反馈")
    @PostMapping("/submit")
    public Result<SupervisionFeedback> submit(@RequestBody SupervisionFeedback f) {
        return Result.ok("反馈提交成功", feedbackService.submit(f));
    }

    @Operation(summary = "分页查询（支持关键词/日期/状态/优先级筛选）")
    @GetMapping("/page")
    public PageResult<java.util.List<SupervisionFeedback>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        LambdaQueryWrapper<SupervisionFeedback> w = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) w.eq(SupervisionFeedback::getStatus, status);
        if (priority != null && !priority.isEmpty()) w.eq(SupervisionFeedback::getPriority, priority);
        if (keyword != null && !keyword.isEmpty())
            w.and(qw -> qw.like(SupervisionFeedback::getDescription, keyword)
                         .or().like(SupervisionFeedback::getSpecificAddress, keyword));
        if (startDate != null && !startDate.isEmpty())
            w.ge(SupervisionFeedback::getCreateTime, startDate + " 00:00:00");
        if (endDate != null && !endDate.isEmpty())
            w.le(SupervisionFeedback::getCreateTime, endDate + " 23:59:59");
        w.orderByDesc(SupervisionFeedback::getEscalationLevel)
         .orderByAsc(SupervisionFeedback::getEscalatedAt)
         .orderByDesc(SupervisionFeedback::getCreateTime);

        Page<SupervisionFeedback> r = feedbackService.page(new Page<>(page, size), w);
        return PageResult.ok(r.getRecords(), r.getCurrent(), r.getSize(), r.getTotal());
    }

    @Operation(summary = "详情")
    @GetMapping("/{id}")
    public Result<SupervisionFeedback> getById(@PathVariable Long id) {
        return Result.ok(feedbackService.getById(id));
    }

    @Operation(summary = "指派网格员")
    @PutMapping("/assign/{id}")
    public Result<Void> assign(@PathVariable Long id, @RequestParam Long inspectorId) {
        feedbackService.assign(id, inspectorId);
        return Result.success("指派成功");
    }

    @Operation(summary = "我的反馈")
    @GetMapping("/my/{supervisorId}")
    public Result<java.util.List<SupervisionFeedback>> my(@PathVariable Long supervisorId) {
        LambdaQueryWrapper<SupervisionFeedback> w = new LambdaQueryWrapper<>();
        w.eq(SupervisionFeedback::getSupervisorId, supervisorId)
         .orderByDesc(SupervisionFeedback::getCreateTime);
        return Result.ok(feedbackService.list(w));
    }

    @Operation(summary = "撤回反馈（仅PENDING状态）")
    @PutMapping("/cancel/{id}")
    public Result<Void> cancel(@PathVariable Long id, @RequestParam Long supervisorId) {
        feedbackService.cancel(id, supervisorId);
        return Result.success("反馈已撤回");
    }

    // ==================== ⭐ 工单升级 & 归档 API ====================

    @Operation(summary = "手动升级指定工单（管理员操作）")
    @PutMapping("/escalate/{id}")
    public Result<SupervisionFeedback> escalate(@PathVariable Long id) {
        SupervisionFeedback result = feedbackService.escalate(id);
        return Result.ok("工单已升级至 " + result.getEscalationLevel() + " 级（" + result.getPriority() + "）", result);
    }

    @Operation(summary = "获取已升级工单列表（按紧急程度排序）")
    @GetMapping("/escalated")
    public Result<java.util.List<SupervisionFeedback>> listEscalated() {
        return Result.ok(feedbackService.listEscalated());
    }

    @Operation(summary = "超时工单统计信息")
    @GetMapping("/stats")
    public Result<Map<String, Object>> escalationStats() {
        return Result.ok(feedbackService.getEscalationStats());
    }

    @Operation(summary = "手动触发超时升级扫描（管理员操作，通常由定时任务自动执行）")
    @PostMapping("/trigger-escalation")
    public Result<String> triggerEscalation() {
        int count = feedbackService.autoEscalateExpired();
        return Result.ok("升级扫描完成，共升级 " + count + " 个超时工单");
    }

    @Operation(summary = "手动触发冷数据归档（管理员操作）")
    @PostMapping("/trigger-archive")
    public Result<String> triggerArchive(@RequestParam(defaultValue = "500") int batchSize) {
        int totalArchived = 0;
        int batchCount;
        do {
            batchCount = feedbackService.autoArchiveCompleted(batchSize);
            totalArchived += batchCount;
        } while (batchCount == batchSize);
        return Result.ok("归档完成，共归档 " + totalArchived + " 条记录");
    }
}
