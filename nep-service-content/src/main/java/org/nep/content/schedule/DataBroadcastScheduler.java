package org.nep.content.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nep.system.entity.AqiDetection;
import org.nep.system.mapper.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 实时数据广播定时任务（NEPV 决策大屏）
 * <p>
 * 每 10 秒从数据库查询真实统计数据，通过 WebSocket 推送到 NEPV 决策大屏。
 *
 * <h3>数据来源</h3>
 * <ul>
 *   <li>totalDetections - AQI 检测总数</li>
 *   <li>goodDetections - 优良检测数 (AQI ≤ 100)</li>
 *   <li>badDetections - 超标检测数 (AQI > 100)</li>
 *   <li>gridCoverageByProvince - 省份覆盖率</li>
 *   <li>gridCoverageByCity - 城市覆盖率</li>
 *   <li>onlineInspectors - 在线网格员（role=NEPG 且 status=2）</li>
 *   <li>pendingFeedback - 待处理反馈数</li>
 *   <li>exceededByPollutant - SO2/CO/PM2.5 各污染物超标数</li>
 * </ul>
 *
 * @author NEP Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataBroadcastScheduler {

    private final SimpMessagingTemplate messagingTemplate;
    private final AqiDetectionMapper aqiMapper;
    private final ProvinceMapper provinceMapper;
    private final CityMapper cityMapper;
    private final SupervisionFeedbackMapper feedbackMapper;

    @Scheduled(fixedRate = 10000)
    public void broadcastStatistics() {
        try {
            Map<String, Object> data = buildRealtimeData();
            messagingTemplate.convertAndSend("/topic/dashboard", data);
            log.info("实时数据已推送: total={}, good={}, bad={}, coverage={}%",
                    data.get("totalDetections"), data.get("goodDetections"),
                    data.get("badDetections"), data.get("gridCoverageByProvince"));
        } catch (Exception e) {
            log.error("实时数据推送失败", e);
        }
    }

    private Map<String, Object> buildRealtimeData() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));

        // ==================== AQI 检测统计 ====================
        long totalDetections = aqiMapper.selectCount(null);
        long goodDetections = aqiMapper.selectCount(
                new LambdaQueryWrapper<AqiDetection>().le(AqiDetection::getFinalAqi, 100));
        long badDetections = totalDetections - goodDetections;

        data.put("totalDetections", totalDetections);
        data.put("goodDetections", goodDetections);
        data.put("badDetections", badDetections);

        // ==================== 网格覆盖率 ====================
        long totalProvinces = provinceMapper.selectCount(null);
        long totalCities = cityMapper.selectCount(null);

        List<Long> coveredProvinceIds = aqiMapper.selectDistinctProvinceIds();
        List<Long> coveredCityIds = aqiMapper.selectDistinctCityIds();

        int provinceCoverage = totalProvinces > 0
                ? (int) Math.round((double) coveredProvinceIds.size() / totalProvinces * 100) : 0;
        int cityCoverage = totalCities > 0
                ? (int) Math.round((double) coveredCityIds.size() / totalCities * 100) : 0;

        data.put("gridCoverageByProvince", provinceCoverage);
        data.put("gridCoverageByCity", cityCoverage);

        // ==================== 在线网格员（近24h有检测上报的活跃网格员数） ====================
        long onlineInspectors = aqiMapper.countActiveInspectors24h();
        data.put("onlineInspectors", onlineInspectors);

        // ==================== 待处理反馈 ====================
        long pendingFeedback = 0;
        try {
            pendingFeedback = feedbackMapper.countPendingFeedback();
        } catch (Exception e) {
            log.debug("反馈统计查询失败（反馈服务可能未启动）: {}", e.getMessage());
        }
        data.put("pendingFeedback", pendingFeedback);

        // ==================== 各污染物超标统计 ====================
        Map<String, Object> exceeded = new LinkedHashMap<>();
        exceeded.put("SO2", aqiMapper.selectCount(
                new LambdaQueryWrapper<AqiDetection>().gt(AqiDetection::getSo2Aqi, 100)));
        exceeded.put("CO", aqiMapper.selectCount(
                new LambdaQueryWrapper<AqiDetection>().gt(AqiDetection::getCoAqi, 100)));
        exceeded.put("PM2.5", aqiMapper.selectCount(
                new LambdaQueryWrapper<AqiDetection>().gt(AqiDetection::getPm25Aqi, 100)));
        data.put("exceededByPollutant", exceeded);

        return data;
    }
}
