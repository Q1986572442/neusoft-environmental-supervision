package org.nep.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.nep.common.result.PageResult;
import org.nep.common.result.Result;
import org.nep.system.entity.OperationLog;
import org.nep.system.mapper.OperationLogMapper;
import org.springframework.web.bind.annotation.*;

/**
 * 操作日志审计控制器
 * 提供操作日志的分页查询与详情查看功能
 */
@Tag(name = "操作日志")
@RestController
@RequestMapping("/api/log")
public class LogController {

    private final OperationLogMapper logMapper;
    public LogController(OperationLogMapper logMapper) { this.logMapper = logMapper; }

    @Operation(summary = "分页查询操作日志（管理员）")
    @GetMapping("/page")
    public PageResult<java.util.List<OperationLog>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) Long userId) {
        LambdaQueryWrapper<OperationLog> w = new LambdaQueryWrapper<>();
        if (module != null && !module.isEmpty()) w.eq(OperationLog::getModule, module);
        if (userId != null) w.eq(OperationLog::getUserId, userId);
        w.orderByDesc(OperationLog::getCreateTime);
        Page<OperationLog> r = logMapper.selectPage(new Page<>(page, size), w);
        return PageResult.ok(r.getRecords(), r.getCurrent(), r.getSize(), r.getTotal());
    }

    @Operation(summary = "获取日志详情")
    @GetMapping("/{id}")
    public Result<OperationLog> getById(@PathVariable Long id) {
        return Result.ok(logMapper.selectById(id));
    }

    @Operation(summary = "清空日志（管理员）")
    @DeleteMapping("/clear")
    public Result<Void> clear() {
        logMapper.delete(new LambdaQueryWrapper<>());
        return Result.success("日志已清空");
    }
}
