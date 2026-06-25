package org.nep.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.nep.common.result.PageResult;
import org.nep.common.result.Result;
import org.nep.system.entity.Notification;
import org.nep.system.mapper.NotificationMapper;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 系统通知控制器
 * 提供用户通知列表、已读标记、批量删除等功能
 */
@Tag(name = "通知管理")
@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    private final NotificationMapper notificationMapper;
    public NotificationController(NotificationMapper notificationMapper) { this.notificationMapper = notificationMapper; }

    @Operation(summary = "分页查询我的通知")
    @GetMapping("/page")
    public PageResult<java.util.List<Notification>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam Long userId) {
        LambdaQueryWrapper<Notification> w = new LambdaQueryWrapper<>();
        w.eq(Notification::getUserId, userId).orderByDesc(Notification::getCreateTime);
        Page<Notification> r = notificationMapper.selectPage(new Page<>(page, size), w);
        return PageResult.ok(r.getRecords(), r.getCurrent(), r.getSize(), r.getTotal());
    }

    @Operation(summary = "获取未读通知数量")
    @GetMapping("/unread-count")
    public Result<java.util.Map<String, Object>> unreadCount(@RequestParam Long userId) {
        LambdaQueryWrapper<Notification> w = new LambdaQueryWrapper<>();
        w.eq(Notification::getUserId, userId).eq(Notification::getIsRead, 0);
        long count = notificationMapper.selectCount(w);
        java.util.Map<String, Object> map = new java.util.LinkedHashMap<>();
        map.put("unreadCount", count);
        return Result.ok(map);
    }

    @Operation(summary = "标记通知为已读")
    @PutMapping("/read/{id}")
    public Result<Void> read(@PathVariable Long id) {
        Notification notification = notificationMapper.selectById(id);
        if (notification != null) {
            notification.setIsRead(1);
            notification.setReadTime(LocalDateTime.now());
            notificationMapper.updateById(notification);
        }
        return Result.success("已标记为已读");
    }

    @Operation(summary = "全部标记为已读")
    @PutMapping("/read-all")
    public Result<Void> readAll(@RequestParam Long userId) {
        LambdaQueryWrapper<Notification> w = new LambdaQueryWrapper<>();
        w.eq(Notification::getUserId, userId).eq(Notification::getIsRead, 0);
        java.util.List<Notification> list = notificationMapper.selectList(w);
        for (Notification n : list) {
            n.setIsRead(1);
            n.setReadTime(LocalDateTime.now());
            notificationMapper.updateById(n);
        }
        return Result.success("全部标记为已读");
    }

    @Operation(summary = "删除通知")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        notificationMapper.deleteById(id);
        return Result.success("删除成功");
    }
}
