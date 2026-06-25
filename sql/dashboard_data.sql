-- ========================================
-- 全景数据分析中枢 - 大屏数据表
-- ========================================

-- 1. 指标统计表（顶部 4 个 KPI 卡片）
DROP TABLE IF EXISTS `nep_dashboard_kpi`;
CREATE TABLE `nep_dashboard_kpi` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `kpi_key` VARCHAR(64) NOT NULL COMMENT '指标键名：pending_todo/recent_visits/data_accuracy/safe_days',
  `kpi_label` VARCHAR(64) NOT NULL COMMENT '指标显示名称',
  `kpi_value` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '指标数值',
  `kpi_unit` VARCHAR(16) DEFAULT '' COMMENT '单位：个/次/%/天',
  `sort_order` INT DEFAULT 0 COMMENT '排序序号',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_kpi_key` (`kpi_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='大屏KPI指标统计表';

-- 2. 每日 AQI 数据表（折线图）
DROP TABLE IF EXISTS `nep_dashboard_aqi_daily`;
CREATE TABLE `nep_dashboard_aqi_daily` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `record_date` DATE NOT NULL COMMENT '统计日期',
  `aqi_value` INT NOT NULL DEFAULT 0 COMMENT 'AQI数值',
  `aqi_level` VARCHAR(16) DEFAULT '' COMMENT '空气质量等级：优/良/轻度/中度/重度',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_record_date` (`record_date`),
  KEY `idx_aqi_value` (`aqi_value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日AQI数据表';

-- 3. 污染评估分布表（饼图）
DROP TABLE IF EXISTS `nep_dashboard_pollution_level`;
CREATE TABLE `nep_dashboard_pollution_level` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `level_key` VARCHAR(32) NOT NULL COMMENT '等级键名：excellent/good/light/moderate/severe',
  `level_name` VARCHAR(32) NOT NULL COMMENT '等级名称：优/良/轻度污染/中度污染/重度污染',
  `level_value` INT NOT NULL DEFAULT 0 COMMENT '数量或占比',
  `level_color` VARCHAR(16) DEFAULT '' COMMENT '显示颜色（HEX）',
  `sort_order` INT DEFAULT 0 COMMENT '排序序号',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_level_key` (`level_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='污染评估分布表';

-- 4. 健康指标多维表（雷达图）
DROP TABLE IF EXISTS `nep_dashboard_health_indicator`;
CREATE TABLE `nep_dashboard_health_indicator` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `indicator_key` VARCHAR(32) NOT NULL COMMENT '指标键名：so2/no2/co/o3/pm25/pm10',
  `indicator_name` VARCHAR(32) NOT NULL COMMENT '指标名称',
  `indicator_value` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '指标数值',
  `max_value` DECIMAL(10,2) NOT NULL DEFAULT 100 COMMENT '参考最大值（雷达图刻度）',
  `unit` VARCHAR(16) DEFAULT '' COMMENT '单位',
  `sort_order` INT DEFAULT 0 COMMENT '排序序号',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_indicator_key` (`indicator_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康指标多维表';

-- 5. 省份异常数据表（柱状图）
DROP TABLE IF EXISTS `nep_dashboard_province_anomaly`;
CREATE TABLE `nep_dashboard_province_anomaly` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `province_code` VARCHAR(32) NOT NULL COMMENT '省份编码',
  `province_name` VARCHAR(32) NOT NULL COMMENT '省份名称',
  `anomaly_count` INT NOT NULL DEFAULT 0 COMMENT '异常数量',
  `sort_order` INT DEFAULT 0 COMMENT '排序序号',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_province_code` (`province_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='省份异常数据表';

-- ========================================
-- 测试数据
-- ========================================

-- KPI 指标数据
INSERT INTO `nep_dashboard_kpi` (`kpi_key`, `kpi_label`, `kpi_value`, `kpi_unit`, `sort_order`) VALUES
('pending_todo', '待处理待办数量', 1024, '个', 1),
('recent_visits', '近三十日访问次数', 238, '次', 2),
('data_accuracy', '污染源数据准确率', 94.2, '%', 3),
('safe_days', '连续安全运行天数', 286, '天', 4);

-- 近7天 AQI 数据
INSERT INTO `nep_dashboard_aqi_daily` (`record_date`, `aqi_value`, `aqi_level`) VALUES
(DATE_SUB(CURDATE(), INTERVAL 6 DAY), 42, '优'),
(DATE_SUB(CURDATE(), INTERVAL 5 DAY), 65, '良'),
(DATE_SUB(CURDATE(), INTERVAL 4 DAY), 58, '良'),
(DATE_SUB(CURDATE(), INTERVAL 3 DAY), 85, '良'),
(DATE_SUB(CURDATE(), INTERVAL 2 DAY), 120, '轻度'),
(DATE_SUB(CURDATE(), INTERVAL 1 DAY), 95, '良'),
(CURDATE(), 62, '良');

-- 污染等级分布数据
INSERT INTO `nep_dashboard_pollution_level` (`level_key`, `level_name`, `level_value`, `level_color`, `sort_order`) VALUES
('excellent', '优', 45, '#2AA876', 1),
('good', '良', 30, '#85C77A', 2),
('light', '轻度', 15, '#F5A623', 3),
('moderate', '中度', 8, '#E87A31', 4),
('severe', '重度', 2, '#D9534F', 5);

-- 健康指标数据
INSERT INTO `nep_dashboard_health_indicator` (`indicator_key`, `indicator_name`, `indicator_value`, `max_value`, `unit`, `sort_order`) VALUES
('so2', 'SO₂', 42, 100, 'μg/m³', 1),
('no2', 'NO₂', 35, 100, 'μg/m³', 2),
('pm10', 'PM10', 80, 150, 'μg/m³', 3),
('pm25', 'PM2.5', 65, 150, 'μg/m³', 4),
('o3', 'O₃', 45, 100, 'μg/m³', 5),
('co', 'CO', 4, 10, 'mg/m³', 6);

-- 省份异常数据
INSERT INTO `nep_dashboard_province_anomaly` (`province_code`, `province_name`, `anomaly_count`, `sort_order`) VALUES
('hebei', '河北', 155, 1),
('shanxi', '山西', 199, 2),
('shandong', '山东', 189, 3),
('henan', '河南', 170, 4),
('liaoning', '辽宁', 118, 5),
('shaanxi', '陕西', 157, 6);