package org.nep.content.report;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.nep.system.dto.MapAqiResult;
import org.nep.system.entity.AqiDetection;
import org.nep.system.entity.City;
import org.nep.system.entity.Province;
import org.nep.system.mapper.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 空间分析报告生成服务
 * <p>
 * 生成包含以下 Sheet 的 Excel 报告：
 * <ol>
 *   <li>宏观态势总览 - KPI 指标卡片</li>
 *   <li>污染热点地图 - 省份/城市 AQI 聚合</li>
 *   <li>AQI 等级分布 - 六等级统计</li>
 *   <li>反馈状态分布 - 工单状态</li>
 *   <li>各省份统计 - 按省份排名</li>
 *   <li>月度趋势 - 近12月变化</li>
 * </ol>
 *
 * @author NEP Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpatialReportService {

    private final AqiDetectionMapper aqiMapper;
    private final ProvinceMapper provinceMapper;
    private final CityMapper cityMapper;
    private final SupervisionFeedbackMapper feedbackMapper;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 生成空间分析报告 Excel 文件
     * @return byte[] Excel 文件字节数组
     */
    public byte[] generateReport() {
        try (Workbook wb = new XSSFWorkbook()) {
            // 创建样式
            CellStyle titleStyle = createTitleStyle(wb);
            CellStyle headerStyle = createHeaderStyle(wb);
            CellStyle dataStyle = createDataStyle(wb);
            CellStyle numberStyle = createNumberStyle(wb);
            CellStyle subtitleStyle = createSubtitleStyle(wb);

            // Sheet 1: 宏观态势总览
            sheetOverview(wb, titleStyle, headerStyle, dataStyle, numberStyle, subtitleStyle);

            // Sheet 2: 污染热点地图
            sheetHotspotMap(wb, titleStyle, headerStyle, dataStyle, numberStyle, subtitleStyle);

            // Sheet 3: AQI 等级分布
            sheetAqiDistribution(wb, titleStyle, headerStyle, dataStyle, numberStyle, subtitleStyle);

            // Sheet 4: 反馈状态分布
            sheetFeedbackStatus(wb, titleStyle, headerStyle, dataStyle, numberStyle, subtitleStyle);

            // Sheet 5: 各省份统计
            sheetProvinceStats(wb, titleStyle, headerStyle, dataStyle, numberStyle, subtitleStyle);

            // Sheet 6: 月度趋势
            sheetMonthlyTrend(wb, titleStyle, headerStyle, dataStyle, numberStyle, subtitleStyle);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            wb.write(baos);
            log.info("空间分析报告生成成功，文件大小: {} bytes", baos.size());
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("生成报告失败", e);
            throw new RuntimeException("报告生成失败: " + e.getMessage(), e);
        }
    }

    // ==================== Sheet 1: 宏观态势总览 ====================

    private void sheetOverview(Workbook wb, CellStyle title, CellStyle header,
                                CellStyle data, CellStyle number, CellStyle sub) {
        Sheet sheet = wb.createSheet("宏观态势总览");
        int r = 0;

        // 标题
        Row titleRow = sheet.createRow(r++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("东软环保公众监督系统 - 空间分析报告");
        titleCell.setCellStyle(title);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));
        titleRow.setHeightInPoints(28);

        // 生成时间
        Row timeRow = sheet.createRow(r++);
        Cell timeCell = timeRow.createCell(0);
        timeCell.setCellValue("生成时间: " + LocalDateTime.now().format(DT_FMT));
        timeCell.setCellStyle(sub);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 3));

        r++; // 空行

        // KPI 数据
        long totalDetections = aqiMapper.selectCount(null);
        long totalProvinces = provinceMapper.selectCount(null);
        long totalCities = cityMapper.selectCount(null);
        long totalFeedbacks = 0, pendingFeedbacks = 0, completedFeedbacks = 0;
        try {
            totalFeedbacks = feedbackMapper.selectCount(null);
            pendingFeedbacks = feedbackMapper.countPendingFeedback();
            completedFeedbacks = feedbackMapper.selectCount(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<
                            org.nep.system.entity.SupervisionFeedback>()
                            .eq(org.nep.system.entity.SupervisionFeedback::getStatus, "COMPLETED"));
        } catch (Exception ignored) {}

        addKpiRow(sheet, r++, "AQI 检测总数", String.valueOf(totalDetections), "次", header, data, number);
        addKpiRow(sheet, r++, "监督反馈总数", String.valueOf(totalFeedbacks), "条", header, data, number);
        addKpiRow(sheet, r++, "覆盖省份数", String.valueOf(totalProvinces), "个", header, data, number);
        addKpiRow(sheet, r++, "覆盖城市数", String.valueOf(totalCities), "个", header, data, number);
        addKpiRow(sheet, r++, "已完成反馈", String.valueOf(completedFeedbacks), "条", header, data, number);
        addKpiRow(sheet, r++, "待处理反馈", String.valueOf(pendingFeedbacks), "条", header, data, number);
        addKpiRow(sheet, r++, "城市覆盖率", totalCities > 0 ? Math.round((double) totalCities / 339 * 100) + "%" : "0%", "", header, data, number);

        // 列宽
        sheet.setColumnWidth(0, 5000);
        sheet.setColumnWidth(1, 4000);
        sheet.setColumnWidth(2, 3000);
        sheet.setColumnWidth(3, 5000);
    }

    // ==================== Sheet 2: 污染热点地图 ====================

    private void sheetHotspotMap(Workbook wb, CellStyle title, CellStyle header,
                                  CellStyle data, CellStyle number, CellStyle sub) {
        Sheet sheet = wb.createSheet("污染热点地图");
        int r = 0;

        addSheetTitle(sheet, r++, "污染热点地图 - 全国 AQI 分布", title, sub);

        // 表头
        Row hRow = sheet.createRow(r++);
        String[] headers = {"省份", "城市", "平均AQI", "最大AQI", "检测次数", "AQI等级", "等级标签"};
        for (int i = 0; i < headers.length; i++) {
            Cell c = hRow.createCell(i);
            c.setCellValue(headers[i]);
            c.setCellStyle(header);
        }

        // 数据
        List<Map<String, Object>> cityData = aqiMapper.aggregateByCity30Days();
        for (Map<String, Object> row : cityData) {
            Row dRow = sheet.createRow(r++);
            setCell(dRow, 0, toString(row.get("cityName")), data);
            setCell(dRow, 1, toString(row.get("provinceName")), data);
            setCell(dRow, 2, toDouble(row.get("avgAqi")), number);
            setCell(dRow, 3, toInt(row.get("maxAqi")), number);
            setCell(dRow, 4, toLong(row.get("detectionCount")), number);
            double avgAqi = toDouble(row.get("avgAqi"));
            int level = MapAqiResult.aqiToLevel(avgAqi);
            setCell(dRow, 5, level, number);
            setCell(dRow, 6, MapAqiResult.levelToLabel(level), data);
        }

        if (cityData.isEmpty()) {
            sheet.createRow(r).createCell(0).setCellValue("暂无数据");
        }

        // 列宽
        for (int i = 0; i < 7; i++) sheet.setColumnWidth(i, 4000);
    }

    // ==================== Sheet 3: AQI 等级分布 ====================

    private void sheetAqiDistribution(Workbook wb, CellStyle title, CellStyle header,
                                       CellStyle data, CellStyle number, CellStyle sub) {
        Sheet sheet = wb.createSheet("AQI等级分布");
        int r = 0;
        addSheetTitle(sheet, r++, "AQI 等级分布统计", title, sub);

        Row hRow = sheet.createRow(r++);
        String[] headers = {"等级", "名称", "AQI范围", "检测次数", "占比"};
        for (int i = 0; i < headers.length; i++) {
            Cell c = hRow.createCell(i);
            c.setCellValue(headers[i]);
            c.setCellStyle(header);
        }

        String[] names = {"优", "良", "轻度污染", "中度污染", "重度污染", "严重污染"};
        int[] rangesLow = {0, 51, 101, 151, 201, 301};
        int[] rangesHigh = {50, 100, 150, 200, 300, Integer.MAX_VALUE};
        long total = aqiMapper.selectCount(null);

        for (int i = 0; i < 6; i++) {
            long count;
            if (i == 5) {
                count = aqiMapper.selectCount(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AqiDetection>()
                                .gt(AqiDetection::getFinalAqi, 300));
            } else {
                count = aqiMapper.selectCount(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AqiDetection>()
                                .ge(AqiDetection::getFinalAqi, rangesLow[i])
                                .le(AqiDetection::getFinalAqi, rangesHigh[i]));
            }
            Row dRow = sheet.createRow(r++);
            setCell(dRow, 0, i + 1, number);
            setCell(dRow, 1, names[i], data);
            setCell(dRow, 2, rangesLow[i] + (i < 5 ? "-" + rangesHigh[i] : "+"), data);
            setCell(dRow, 3, count, number);
            setCell(dRow, 4, total > 0 ? Math.round((double) count / total * 100) + "%" : "0%", data);
        }

        for (int i = 0; i < 5; i++) sheet.setColumnWidth(i, 4000);
    }

    // ==================== Sheet 4: 反馈状态分布 ====================

    private void sheetFeedbackStatus(Workbook wb, CellStyle title, CellStyle header,
                                      CellStyle data, CellStyle number, CellStyle sub) {
        Sheet sheet = wb.createSheet("反馈状态分布");
        int r = 0;
        addSheetTitle(sheet, r++, "反馈工单状态分布", title, sub);

        Row hRow = sheet.createRow(r++);
        String[] headers = {"状态", "说明", "数量"};
        for (int i = 0; i < headers.length; i++) {
            Cell c = hRow.createCell(i);
            c.setCellValue(headers[i]);
            c.setCellStyle(header);
        }

        String[][] statuses = {
                {"PENDING", "待指派"},
                {"ASSIGNED", "已指派/处理中"},
                {"COMPLETED", "已完成"},
                {"ESCALATED", "已升级/催办"},
                {"REJECTED", "已拒绝"}
        };

        try {
            for (String[] s : statuses) {
                long count = feedbackMapper.selectCount(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<
                                org.nep.system.entity.SupervisionFeedback>()
                                .eq(org.nep.system.entity.SupervisionFeedback::getStatus, s[0]));
                Row dRow = sheet.createRow(r++);
                setCell(dRow, 0, s[0], data);
                setCell(dRow, 1, s[1], data);
                setCell(dRow, 2, count, number);
            }
        } catch (Exception e) {
            sheet.createRow(r).createCell(0).setCellValue("反馈数据暂不可用");
        }

        for (int i = 0; i < 3; i++) sheet.setColumnWidth(i, 5000);
    }

    // ==================== Sheet 5: 各省份统计 ====================

    private void sheetProvinceStats(Workbook wb, CellStyle title, CellStyle header,
                                     CellStyle data, CellStyle number, CellStyle sub) {
        Sheet sheet = wb.createSheet("各省份统计");
        int r = 0;
        addSheetTitle(sheet, r++, "各省份 AQI 与检测统计（近30天）", title, sub);

        Row hRow = sheet.createRow(r++);
        String[] headers = {"排名", "省份", "平均AQI", "最大AQI", "检测次数", "覆盖城市", "等级"};
        for (int i = 0; i < headers.length; i++) {
            Cell c = hRow.createCell(i);
            c.setCellValue(headers[i]);
            c.setCellStyle(header);
        }

        List<Map<String, Object>> rows = aqiMapper.aggregateByProvince30Days();
        int rank = 1;
        for (Map<String, Object> row : rows) {
            Row dRow = sheet.createRow(r++);
            double avgAqi = toDouble(row.get("avgAqi"));
            setCell(dRow, 0, rank++, number);
            setCell(dRow, 1, toString(row.get("provinceName")), data);
            setCell(dRow, 2, avgAqi, number);
            setCell(dRow, 3, toInt(row.get("maxAqi")), number);
            setCell(dRow, 4, toLong(row.get("totalDetections")), number);
            setCell(dRow, 5, toInt(row.get("cityCount")), number);
            setCell(dRow, 6, MapAqiResult.levelToLabel(MapAqiResult.aqiToLevel(avgAqi)), data);
        }

        if (rows.isEmpty()) {
            sheet.createRow(r).createCell(0).setCellValue("暂无数据");
        }

        for (int i = 0; i < 7; i++) sheet.setColumnWidth(i, 4000);
    }

    // ==================== Sheet 6: 月度趋势 ====================

    private void sheetMonthlyTrend(Workbook wb, CellStyle title, CellStyle header,
                                    CellStyle data, CellStyle number, CellStyle sub) {
        Sheet sheet = wb.createSheet("月度趋势");
        int r = 0;
        addSheetTitle(sheet, r++, "AQI 检测月度趋势（近12个月）", title, sub);

        Row hRow = sheet.createRow(r++);
        String[] headers = {"月份", "检测次数", "环比变化"};
        for (int i = 0; i < headers.length; i++) {
            Cell c = hRow.createCell(i);
            c.setCellValue(headers[i]);
            c.setCellStyle(header);
        }

        LocalDateTime now = LocalDateTime.now();
        long prevCount = -1;
        for (int i = 11; i >= 0; i--) {
            LocalDateTime monthStart = now.minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime monthEnd = monthStart.plusMonths(1);
            long count = aqiMapper.selectCount(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AqiDetection>()
                            .ge(AqiDetection::getDetectionTime, monthStart.format(DT_FMT))
                            .lt(AqiDetection::getDetectionTime, monthEnd.format(DT_FMT)));

            Row dRow = sheet.createRow(r++);
            setCell(dRow, 0, monthStart.getYear() + "-" + String.format("%02d", monthStart.getMonthValue()), data);
            setCell(dRow, 1, count, number);
            if (prevCount >= 0 && prevCount > 0) {
                setCell(dRow, 2, Math.round((double)(count - prevCount) / prevCount * 100) + "%", data);
            } else {
                setCell(dRow, 2, "-", data);
            }
            prevCount = count;
        }

        for (int i = 0; i < 3; i++) sheet.setColumnWidth(i, 4000);
    }

    // ==================== 样式 ====================

    private CellStyle createTitleStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setFontName("Microsoft YaHei");
        f.setBold(true);
        f.setFontHeightInPoints((short) 16);
        f.setColor(IndexedColors.DARK_BLUE.getIndex());
        s.setFont(f);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        return s;
    }

    private CellStyle createSubtitleStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setFontName("Microsoft YaHei");
        f.setFontHeightInPoints((short) 10);
        f.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        s.setFont(f);
        return s;
    }

    private CellStyle createHeaderStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setFontName("Microsoft YaHei");
        f.setBold(true);
        f.setFontHeightInPoints((short) 11);
        f.setColor(IndexedColors.WHITE.getIndex());
        s.setFont(f);
        s.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
        return s;
    }

    private CellStyle createDataStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setFontName("Microsoft YaHei");
        f.setFontHeightInPoints((short) 10);
        s.setFont(f);
        s.setAlignment(HorizontalAlignment.LEFT);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
        return s;
    }

    private CellStyle createNumberStyle(Workbook wb) {
        CellStyle s = createDataStyle(wb);
        s.setAlignment(HorizontalAlignment.CENTER);
        return s;
    }

    // ==================== 工具方法 ====================

    private void addSheetTitle(Sheet sheet, int row, String titleText, CellStyle title, CellStyle sub) {
        Row tRow = sheet.createRow(row);
        tRow.createCell(0).setCellValue(titleText);
        tRow.getCell(0).setCellStyle(title);
        sheet.addMergedRegion(new CellRangeAddress(row, row, 0, 6));
        tRow.setHeightInPoints(24);

        Row timeRow = sheet.createRow(row + 1);
        timeRow.createCell(0).setCellValue("生成时间: " + LocalDateTime.now().format(DT_FMT));
        timeRow.getCell(0).setCellStyle(sub);
        sheet.addMergedRegion(new CellRangeAddress(row + 1, row + 1, 0, 6));

        // 跳过标题行 + 时间行，返回新行号
        sheet.createRow(row + 2); // 空行
    }

    private void addKpiRow(Sheet sheet, int row, String label, String value, String unit,
                           CellStyle header, CellStyle data, CellStyle number) {
        Row r = sheet.createRow(row);
        setCell(r, 0, label, header);
        setCell(r, 1, value, number);
        setCell(r, 2, unit, data);
    }

    private void setCell(Row row, int col, String value, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(value != null ? value : "");
        c.setCellStyle(style);
    }

    private void setCell(Row row, int col, double value, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(value);
        c.setCellStyle(style);
    }

    private void setCell(Row row, int col, long value, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(value);
        c.setCellStyle(style);
    }

    private void setCell(Row row, int col, int value, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(value);
        c.setCellStyle(style);
    }

    // ==================== 类型转换 ====================

    private String toString(Object obj) { return obj != null ? obj.toString() : ""; }
    private double toDouble(Object obj) {
        if (obj == null) return 0.0;
        if (obj instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(obj.toString()); } catch (NumberFormatException e) { return 0.0; }
    }
    private int toInt(Object obj) {
        if (obj == null) return 0;
        if (obj instanceof Number n) return n.intValue();
        try { return Integer.parseInt(obj.toString()); } catch (NumberFormatException e) { return 0; }
    }
    private long toLong(Object obj) {
        if (obj == null) return 0L;
        if (obj instanceof Number n) return n.longValue();
        try { return Long.parseLong(obj.toString()); } catch (NumberFormatException e) { return 0L; }
    }
}
