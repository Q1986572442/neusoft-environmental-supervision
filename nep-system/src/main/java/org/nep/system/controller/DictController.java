package org.nep.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.nep.common.result.Result;
import org.nep.system.entity.DictData;
import org.nep.system.entity.DictType;
import org.nep.system.mapper.DictDataMapper;
import org.nep.system.mapper.DictTypeMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据字典控制器
 * 提供系统数据字典的查询功能，用于前端下拉框等场景
 */
@Tag(name = "数据字典")
@RestController
@RequestMapping("/api/dict")
public class DictController {

    private final DictTypeMapper dictTypeMapper;
    private final DictDataMapper dictDataMapper;
    public DictController(DictTypeMapper dictTypeMapper, DictDataMapper dictDataMapper) {
        this.dictTypeMapper = dictTypeMapper;
        this.dictDataMapper = dictDataMapper;
    }

    @Operation(summary = "根据字典类型获取数据")
    @GetMapping("/data/{dictType}")
    public Result<List<DictData>> dataByType(@PathVariable String dictType) {
        LambdaQueryWrapper<DictData> w = new LambdaQueryWrapper<>();
        w.eq(DictData::getDictType, dictType).eq(DictData::getStatus, 1).orderByAsc(DictData::getSortOrder);
        return Result.ok(dictDataMapper.selectList(w));
    }

    @Operation(summary = "获取所有字典类型")
    @GetMapping("/types")
    public Result<List<DictType>> types() {
        LambdaQueryWrapper<DictType> w = new LambdaQueryWrapper<>();
        w.eq(DictType::getStatus, 1);
        return Result.ok(dictTypeMapper.selectList(w));
    }

    @Operation(summary = "获取全部字典数据（按类型分组）")
    @GetMapping("/all")
    public Result<Map<String, List<DictData>>> all() {
        List<DictType> types = dictTypeMapper.selectList(new LambdaQueryWrapper<DictType>().eq(DictType::getStatus, 1));
        Map<String, List<DictData>> result = new LinkedHashMap<>();
        for (DictType type : types) {
            LambdaQueryWrapper<DictData> w = new LambdaQueryWrapper<>();
            w.eq(DictData::getDictType, type.getDictType()).eq(DictData::getStatus, 1).orderByAsc(DictData::getSortOrder);
            result.put(type.getDictType(), dictDataMapper.selectList(w));
        }
        return Result.ok(result);
    }
}
