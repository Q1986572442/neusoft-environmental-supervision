package org.nep.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.nep.common.result.PageResult;
import org.nep.common.result.Result;
import org.nep.system.entity.News;
import org.nep.system.service.NewsService;
import org.springframework.web.bind.annotation.*;

/**
 * 环保新闻公告控制器
 * 提供新闻的CRUD、分页查询、浏览量统计等接口
 */
@Tag(name = "新闻公告管理")
@RestController
@RequestMapping("/api/news")
public class NewsController {

    private final NewsService newsService;
    public NewsController(NewsService newsService) { this.newsService = newsService; }

    @Operation(summary = "分页查询新闻列表")
    @GetMapping("/page")
    public PageResult<java.util.List<News>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String newsType,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<News> w = new LambdaQueryWrapper<>();
        w.eq(News::getStatus, 1);
        if (newsType != null && !newsType.isEmpty()) w.eq(News::getNewsType, newsType);
        if (keyword != null && !keyword.isEmpty()) w.like(News::getTitle, keyword);
        w.orderByDesc(News::getPublishTime);
        Page<News> r = newsService.page(new Page<>(page, size), w);
        return PageResult.ok(r.getRecords(), r.getCurrent(), r.getSize(), r.getTotal());
    }

    @Operation(summary = "获取新闻详情")
    @GetMapping("/{id}")
    public Result<News> getById(@PathVariable Long id) {
        newsService.incrementViewCount(id);
        return Result.ok(newsService.getById(id));
    }

    @Operation(summary = "获取最新新闻（首页展示）")
    @GetMapping("/latest")
    public Result<java.util.List<News>> latest(@RequestParam(defaultValue = "5") Integer count) {
        LambdaQueryWrapper<News> w = new LambdaQueryWrapper<>();
        w.eq(News::getStatus, 1).orderByDesc(News::getPublishTime).last("LIMIT " + count);
        return Result.ok(newsService.list(w));
    }

    @Operation(summary = "发布新闻/公告（管理员）")
    @PostMapping
    public Result<News> create(@RequestBody News news) {
        return Result.ok("发布成功", newsService.publish(news));
    }

    @Operation(summary = "更新新闻/公告（管理员）")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody News news) {
        news.setId(id);
        newsService.updateById(news);
        return Result.success("更新成功");
    }

    @Operation(summary = "删除新闻/公告（管理员）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        newsService.removeById(id);
        return Result.success("删除成功");
    }
}
