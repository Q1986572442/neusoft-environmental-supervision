-- =====================================================
-- 东软环保公众监督系统 (NEP) - v5 数据库迁移
-- 新增: 工单升级与归档字段
-- =====================================================

USE nep_db;

-- 存储过程：安全添加字段
DELIMITER //
DROP PROCEDURE IF EXISTS add_column_if_not_exists//
CREATE PROCEDURE add_column_if_not_exists(
    IN tbl_name VARCHAR(64),
    IN col_name VARCHAR(64),
    IN col_def  VARCHAR(256)
)
BEGIN
    DECLARE col_count INT DEFAULT 0;
    SELECT COUNT(*) INTO col_count
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = tbl_name
      AND COLUMN_NAME = col_name;

    IF col_count = 0 THEN
        SET @ddl = CONCAT('ALTER TABLE ', tbl_name, ' ADD COLUMN ', col_name, ' ', col_def);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END//
DELIMITER ;

-- ==================== nep_supervision_feedback 表新增字段 ====================

-- 1. 升级级别（0=未升级, 1=一级, 2=二级, 3=三级最高）
CALL add_column_if_not_exists('nep_supervision_feedback', 'escalation_level',
    "INT DEFAULT 0 COMMENT '升级级别: 0-未升级, 1-一级(>72h), 2-二级(>144h), 3-三级(>216h)' AFTER status");

-- 2. 最近升级时间
CALL add_column_if_not_exists('nep_supervision_feedback', 'escalated_at',
    "TIMESTAMP NULL COMMENT '最近升级时间' AFTER escalation_level");

-- 3. 工单截止处理时间（创建/指派时自动计算 = 当前时间 + 72h）
CALL add_column_if_not_exists('nep_supervision_feedback', 'due_date',
    "TIMESTAMP NULL COMMENT '截止处理时间' AFTER escalated_at");

-- 4. 优先级（NORMAL/HIGH/URGENT，与升级级别联动）
CALL add_column_if_not_exists('nep_supervision_feedback', 'priority',
    "VARCHAR(20) DEFAULT 'NORMAL' COMMENT '优先级: NORMAL/HIGH/URGENT' AFTER due_date");

-- 5. 归档时间
CALL add_column_if_not_exists('nep_supervision_feedback', 'archive_time',
    "TIMESTAMP NULL COMMENT '归档时间' AFTER priority");

-- 6. 是否已归档
CALL add_column_if_not_exists('nep_supervision_feedback', 'archived',
    "TINYINT DEFAULT 0 COMMENT '是否归档: 0-未归档, 1-已归档' AFTER archive_time");

-- ==================== 索引优化 ====================

-- 升级工单查询索引
CREATE INDEX IF NOT EXISTS idx_escalation ON nep_supervision_feedback (escalation_level, status, create_time);

-- 归档查询索引
CREATE INDEX IF NOT EXISTS idx_archived ON nep_supervision_feedback (archived, status, update_time);

-- 超时扫描索引
CREATE INDEX IF NOT EXISTS idx_overdue ON nep_supervision_feedback (status, create_time, escalation_level);

-- ==================== 清理 ====================
DROP PROCEDURE IF EXISTS add_column_if_not_exists;

-- ==================== 更新现有数据 ====================
-- 将现有工单的默认值填好
UPDATE nep_supervision_feedback
SET escalation_level = 0, priority = 'NORMAL', archived = 0
WHERE escalation_level IS NULL;
