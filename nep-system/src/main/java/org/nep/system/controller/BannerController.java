package org.nep.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.nep.common.result.Result;
import org.nep.system.entity.Banner;
import org.nep.system.mapper.BannerMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 首页轮播图控制器
 */
@Tag(name = "轮播图管理")
@RestController
@RequestMapping("/api/banner")
public class BannerController {

    private final BannerMapper bannerMapper;
    public BannerController(BannerMapper bannerMapper) { this.bannerMapper = bannerMapper; }

    @Operation(summary = "获取启用的轮播图列表（前台）")
    @GetMapping("/list")
    public Result<List<Banner>> list() {
        LambdaQueryWrapper<Banner> w = new LambdaQueryWrapper<>();
        w.eq(Banner::getStatus, 1).orderByAsc(Banner::getSortOrder);
        return Result.ok(bannerMapper.selectList(w));
    }

    @Operation(summary = "获取所有轮播图（管理员）")
    @GetMapping("/all")
    public Result<List<Banner>> all() {
        return Result.ok(bannerMapper.selectList(null));
    }

    @Operation(summary = "新增轮播图")
    @PostMapping
    public Result<Banner> create(@RequestBody Banner banner) {
        bannerMapper.insert(banner);
        return Result.ok("添加成功", banner);
    }

    @Operation(summary = "更新轮播图")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Banner banner) {
        banner.setId(id);
        bannerMapper.updateById(banner);
        return Result.success("更新成功");
    }

    @Operation(summary = "删除轮播图")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        bannerMapper.deleteById(id);
        return Result.success("删除成功");
    }
}
