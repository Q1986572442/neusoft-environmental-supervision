package org.nep.system.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 污染热点地图 - 全国AQI聚合结果
 * 包含省份级别和城市级别的聚合数据，用于前端地图渲染
 */
@Data
public class MapAqiResult implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 省份级别聚合 */
    private List<ProvinceAqi> provinces;

    /** 城市级别聚合 */
    private List<CityAqi> cities;

    /** 数据时间戳（用于前端判断数据新鲜度） */
    private Long dataTimestamp;

    /** 是否来自缓存 */
    private boolean fromCache;

    @Data
    public static class ProvinceAqi implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long provinceId;
        private String provinceName;
        private String provinceCode;
        private Double avgAqi;
        private Integer maxAqi;
        private Integer cityCount;
        private Long totalDetections;
        /** AQI等级标签: 优/良/轻度污染/中度污染/重度污染/严重污染 */
        private String levelLabel;
        /** AQI等级: 1-6 */
        private Integer level;
    }

    @Data
    public static class CityAqi implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long cityId;
        private String cityName;
        private Long provinceId;
        private String provinceName;
        private Double avgAqi;
        private Integer maxAqi;
        private Long detectionCount;
        private String levelLabel;
        private Integer level;
    }

    /** 根据AQI值计算等级 */
    public static int aqiToLevel(double aqi) {
        if (aqi <= 50) return 1;
        if (aqi <= 100) return 2;
        if (aqi <= 150) return 3;
        if (aqi <= 200) return 4;
        if (aqi <= 300) return 5;
        return 6;
    }

    /** 根据等级获取中文标签 */
    public static String levelToLabel(int level) {
        return switch (level) {
            case 1 -> "优";
            case 2 -> "良";
            case 3 -> "轻度污染";
            case 4 -> "中度污染";
            case 5 -> "重度污染";
            case 6 -> "严重污染";
            default -> "未知";
        };
    }
}
