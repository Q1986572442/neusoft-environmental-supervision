-- =====================================================
-- 东软环保公众监督系统 (NEP) - v4 数据库迁移
-- 新增: nep_user 表 email 和 avatar 字段
-- 使用前请确认已选择正确的数据库
-- =====================================================

USE nep_db;

-- 安全添加字段：使用存储过程检查字段是否存在
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

-- 1. nep_user 表新增 email 字段（用户邮箱）
CALL add_column_if_not_exists('nep_user', 'email', "VARCHAR(100) COMMENT '电子邮箱' AFTER real_name");

-- 2. nep_user 表新增 avatar 字段（用户头像URL）
CALL add_column_if_not_exists('nep_user', 'avatar', "VARCHAR(500) COMMENT '头像URL' AFTER city_id");

-- 清理存储过程
DROP PROCEDURE IF EXISTS add_column_if_not_exists;
