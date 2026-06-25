package org.nep.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.nep.common.result.PageResult;
import org.nep.common.result.Result;
import org.nep.system.entity.SupervisionFeedback;
import org.nep.system.service.SupervisionFeedbackService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "监督反馈管理")
@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final SupervisionFeedbackService feedbackService;
    public FeedbackController(SupervisionFeedbackService s) { this.feedbackService = s; }

    @Operation(summary = "提交监督反馈")
    @PostMapping("/submit")
    public Result<SupervisionFeedback> submit(@RequestBody SupervisionFeedback f) {
        return Result.ok("反馈提交成功", feedbackService.submit(f));
    }

    @Operation(summary = "分页查询（支持按状态、指派网格员筛选）")
    @GetMapping("/page")
    public PageResult<List<SupervisionFeedback>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long assignedInspectorId) {
        LambdaQueryWrapper<SupervisionFeedback> w = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) w.eq(SupervisionFeedback::getStatus, status);
        if (assignedInspectorId != null) w.eq(SupervisionFeedback::getAssignedInspectorId, assignedInspectorId);
        w.orderByDesc(SupervisionFeedback::getCreateTime);
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
    public Result<List<SupervisionFeedback>> my(@PathVariable Long supervisorId) {
        LambdaQueryWrapper<SupervisionFeedback> w = new LambdaQueryWrapper<>();
        w.eq(SupervisionFeedback::getSupervisorId, supervisorId).orderByDesc(SupervisionFeedback::getCreateTime);
        return Result.ok(feedbackService.list(w));
    }

    // ==================== 新增端点 ====================

    @Operation(summary = "网格员拒绝反馈")
    @PutMapping("/reject/{id}")
    public Result<Void> reject(@PathVariable Long id,
                               @RequestParam Long inspectorId,
                               @RequestParam String reason) {
        feedbackService.reject(id, inspectorId, reason);
        return Result.success("已拒绝该反馈");
    }

    @Operation(summary = "管理员转派反馈")
    @PutMapping("/transfer/{id}")
    public Result<Void> transfer(@PathVariable Long id,
                                 @RequestParam Long toInspectorId) {
        feedbackService.transfer(id, toInspectorId);
        return Result.success("转派成功");
    }

    @Operation(summary = "监督员评价已完成反馈")
    @PutMapping("/rate/{id}")
    public Result<Void> rate(@PathVariable Long id,
                             @RequestBody Map<String, Object> body) {
        Long supervisorId = Long.valueOf(body.get("supervisorId").toString());
        Integer rating = Integer.valueOf(body.get("rating").toString());
        String comment = body.get("ratingComment") != null ? body.get("ratingComment").toString() : null;
        feedbackService.rate(id, supervisorId, rating, comment);
        return Result.success("评价成功");
    }

    @Operation(summary = "管理员批量指派")
    @PostMapping("/batch-assign")
    public Result<Map<String, Object>> batchAssign(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> rawIds = (List<Integer>) body.get("ids");
        List<Long> ids = rawIds.stream().map(Long::valueOf).collect(Collectors.toList());
        Long inspectorId = Long.valueOf(body.get("inspectorId").toString());
        int count = feedbackService.batchAssign(ids, inspectorId);
        return Result.ok(Map.of("successCount", count, "totalCount", ids.size()));
    }
}
