-- =====================================================
-- 反馈处理流程完善 — 数据库迁移 v2
-- 执行方式: mysql -u root -p nep_db < migration_v2.sql
-- =====================================================

-- 新增字段（先检查列是否存在，避免重复执行报错）
SET @sql_reject = IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'nep_db' AND TABLE_NAME = 'nep_supervision_feedback' AND COLUMN_NAME = 'reject_reason') = 0,
  'ALTER TABLE nep_supervision_feedback ADD COLUMN reject_reason VARCHAR(500) NULL COMMENT ''拒绝原因''',
  'SELECT 1'
);
PREPARE stmt FROM @sql_reject; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql_rating = IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'nep_db' AND TABLE_NAME = 'nep_supervision_feedback' AND COLUMN_NAME = 'rating') = 0,
  'ALTER TABLE nep_supervision_feedback ADD COLUMN rating TINYINT NULL COMMENT ''满意度评分(1-5)''',
  'SELECT 1'
);
PREPARE stmt FROM @sql_rating; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql_rc = IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'nep_db' AND TABLE_NAME = 'nep_supervision_feedback' AND COLUMN_NAME = 'rating_comment') = 0,
  'ALTER TABLE nep_supervision_feedback ADD COLUMN rating_comment VARCHAR(500) NULL COMMENT ''评分备注''',
  'SELECT 1'
);
PREPARE stmt FROM @sql_rc; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql_rt = IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'nep_db' AND TABLE_NAME = 'nep_supervision_feedback' AND COLUMN_NAME = 'rating_time') = 0,
  'ALTER TABLE nep_supervision_feedback ADD COLUMN rating_time TIMESTAMP NULL COMMENT ''评分时间''',
  'SELECT 1'
);
PREPARE stmt FROM @sql_rt; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 新建内部备注表
DROP TABLE IF EXISTS nep_feedback_note;
CREATE TABLE nep_feedback_note (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    feedback_id BIGINT NOT NULL COMMENT '关联反馈ID',
    user_id BIGINT NOT NULL COMMENT '创建人ID',
    user_name VARCHAR(50) COMMENT '创建人姓名',
    content VARCHAR(2000) NOT NULL COMMENT '备注内容',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP NULL COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    KEY idx_feedback (feedback_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='反馈内部备注表';

-- 数据修复：确保已有数据的status是字符串格式
UPDATE nep_supervision_feedback SET status = 'PENDING'  WHERE status = '0';
UPDATE nep_supervision_feedback SET status = 'ASSIGNED' WHERE status = '1';
UPDATE nep_supervision_feedback SET status = 'COMPLETED' WHERE status = '2';

-- 新增反馈状态字典数据（使用 INSERT IGNORE 避免重复）
INSERT INTO nep_dict_data (dict_type, dict_label, dict_value, css_class, list_class, is_default, sort_order, status)
SELECT 'feedback_status', '已拒绝', 'REJECTED', '', 'danger', 0, 4, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM nep_dict_data WHERE dict_type = 'feedback_status' AND dict_value = 'REJECTED');

INSERT INTO nep_dict_data (dict_type, dict_label, dict_value, css_class, list_class, is_default, sort_order, status)
SELECT 'feedback_status', '已升级', 'ESCALATED', '', 'danger', 0, 5, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM nep_dict_data WHERE dict_type = 'feedback_status' AND dict_value = 'ESCALATED');
