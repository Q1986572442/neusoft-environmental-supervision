package org.nep.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.nep.common.result.PageResult;
import org.nep.common.result.Result;
import org.nep.system.entity.Knowledge;
import org.nep.system.service.KnowledgeService;
import org.springframework.web.bind.annotation.*;

/**
 * 环保知识库控制器
 * 提供环保知识文章的分类浏览、搜索、点赞等功能
 */
@Tag(name = "环保知识库")
@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    private final KnowledgeService knowledgeService;
    public KnowledgeController(KnowledgeService knowledgeService) { this.knowledgeService = knowledgeService; }

    @Operation(summary = "分页查询知识库")
    @GetMapping("/page")
    public PageResult<java.util.List<Knowledge>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Knowledge> w = new LambdaQueryWrapper<>();
        w.eq(Knowledge::getStatus, 1);
        if (category != null && !category.isEmpty()) w.eq(Knowledge::getCategory, category);
        if (keyword != null && !keyword.isEmpty()) w.like(Knowledge::getTitle, keyword);
        w.orderByDesc(Knowledge::getCreateTime);
        Page<Knowledge> r = knowledgeService.page(new Page<>(page, size), w);
        return PageResult.ok(r.getRecords(), r.getCurrent(), r.getSize(), r.getTotal());
    }

    @Operation(summary = "获取知识详情")
    @GetMapping("/{id}")
    public Result<Knowledge> getById(@PathVariable Long id) {
        knowledgeService.incrementViewCount(id);
        return Result.ok(knowledgeService.getById(id));
    }

    @Operation(summary = "热门知识推荐")
    @GetMapping("/hot")
    public Result<java.util.List<Knowledge>> hot(@RequestParam(defaultValue = "5") Integer count) {
        LambdaQueryWrapper<Knowledge> w = new LambdaQueryWrapper<>();
        w.eq(Knowledge::getStatus, 1).orderByDesc(Knowledge::getViewCount).last("LIMIT " + count);
        return Result.ok(knowledgeService.list(w));
    }

    @Operation(summary = "点赞知识")
    @PostMapping("/like/{id}")
    public Result<Void> like(@PathVariable Long id) {
        knowledgeService.incrementLikeCount(id);
        return Result.success("点赞成功");
    }

    @Operation(summary = "发布知识文章（管理员）")
    @PostMapping
    public Result<Knowledge> create(@RequestBody Knowledge knowledge) {
        knowledge.setStatus(1);
        knowledge.setViewCount(0L);
        knowledge.setLikeCount(0L);
        knowledgeService.save(knowledge);
        return Result.ok("发布成功", knowledge);
    }

    @Operation(summary = "更新知识文章（管理员）")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Knowledge knowledge) {
        knowledge.setId(id);
        knowledgeService.updateById(knowledge);
        return Result.success("更新成功");
    }

    @Operation(summary = "删除知识文章（管理员）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        knowledgeService.removeById(id);
        return Result.success("删除成功");
    }
}
