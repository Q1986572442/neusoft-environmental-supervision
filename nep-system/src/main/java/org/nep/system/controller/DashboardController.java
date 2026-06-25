package org.nep.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.nep.common.result.Result;
import org.nep.system.entity.*;
import org.nep.system.mapper.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "大屏数据")
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Resource
    private DashboardKpiMapper kpiMapper;
    @Resource
    private DashboardAqiDailyMapper aqiDailyMapper;
    @Resource
    private DashboardPollutionLevelMapper pollutionLevelMapper;
    @Resource
    private DashboardHealthIndicatorMapper healthIndicatorMapper;
    @Resource
    private DashboardProvinceAnomalyMapper provinceAnomalyMapper;

    @Operation(summary = "获取大屏全部数据（聚合接口）")
    @GetMapping("/all")
    public Result<Map<String, Object>> getAll() {
        Map<String, Object> data = new HashMap<>();

        LambdaQueryWrapper<DashboardKpi> kpiWrapper = new LambdaQueryWrapper<>();
        kpiWrapper.orderByAsc(DashboardKpi::getSortOrder);
        List<DashboardKpi> kpiList = kpiMapper.selectList(kpiWrapper);
        data.put("kpis", kpiList);

        LambdaQueryWrapper<DashboardAqiDaily> aqiWrapper = new LambdaQueryWrapper<>();
        aqiWrapper.orderByAsc(DashboardAqiDaily::getRecordDate);
        aqiWrapper.last("LIMIT 7");
        List<DashboardAqiDaily> aqiList = aqiDailyMapper.selectList(aqiWrapper);
        data.put("aqiList", aqiList);

        LambdaQueryWrapper<DashboardPollutionLevel> levelWrapper = new LambdaQueryWrapper<>();
        levelWrapper.orderByAsc(DashboardPollutionLevel::getSortOrder);
        List<DashboardPollutionLevel> levelList = pollutionLevelMapper.selectList(levelWrapper);
        data.put("pollutionLevels", levelList);

        LambdaQueryWrapper<DashboardHealthIndicator> indicatorWrapper = new LambdaQueryWrapper<>();
        indicatorWrapper.orderByAsc(DashboardHealthIndicator::getSortOrder);
        List<DashboardHealthIndicator> indicatorList = healthIndicatorMapper.selectList(indicatorWrapper);
        data.put("healthIndicators", indicatorList);

        LambdaQueryWrapper<DashboardProvinceAnomaly> provinceWrapper = new LambdaQueryWrapper<>();
        provinceWrapper.orderByAsc(DashboardProvinceAnomaly::getSortOrder);
        List<DashboardProvinceAnomaly> provinceList = provinceAnomalyMapper.selectList(provinceWrapper);
        data.put("provinceAnomalies", provinceList);

        return Result.ok("获取成功", data);
    }

    @Operation(summary = "获取KPI指标列表")
    @GetMapping("/kpis")
    public Result<List<DashboardKpi>> getKpis() {
        LambdaQueryWrapper<DashboardKpi> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(DashboardKpi::getSortOrder);
        return Result.ok(kpiMapper.selectList(wrapper));
    }

    @Operation(summary = "获取近7天AQI数据")
    @GetMapping("/aqi-daily")
    public Result<List<DashboardAqiDaily>> getAqiDaily() {
        LambdaQueryWrapper<DashboardAqiDaily> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(DashboardAqiDaily::getRecordDate);
        wrapper.last("LIMIT 7");
        return Result.ok(aqiDailyMapper.selectList(wrapper));
    }

    @Operation(summary = "获取污染等级分布")
    @GetMapping("/pollution-levels")
    public Result<List<DashboardPollutionLevel>> getPollutionLevels() {
        LambdaQueryWrapper<DashboardPollutionLevel> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(DashboardPollutionLevel::getSortOrder);
        return Result.ok(pollutionLevelMapper.selectList(wrapper));
    }

    @Operation(summary = "获取健康指标数据")
    @GetMapping("/health-indicators")
    public Result<List<DashboardHealthIndicator>> getHealthIndicators() {
        LambdaQueryWrapper<DashboardHealthIndicator> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(DashboardHealthIndicator::getSortOrder);
        return Result.ok(healthIndicatorMapper.selectList(wrapper));
    }

    @Operation(summary = "获取省份异常数据")
    @GetMapping("/province-anomalies")
    public Result<List<DashboardProvinceAnomaly>> getProvinceAnomalies() {
        LambdaQueryWrapper<DashboardProvinceAnomaly> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(DashboardProvinceAnomaly::getSortOrder);
        return Result.ok(provinceAnomalyMapper.selectList(wrapper));
    }
}