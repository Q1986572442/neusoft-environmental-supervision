-- =====================================================
-- 评论/点赞/收藏/分享 — 数据库迁移 v3
-- =====================================================

-- News增加like_count字段（Knowledge已有）
SET @sql_nl = IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'nep_db' AND TABLE_NAME = 'nep_news' AND COLUMN_NAME = 'like_count') = 0,
  'ALTER TABLE nep_news ADD COLUMN like_count BIGINT DEFAULT 0 COMMENT ''点赞数'' AFTER view_count',
  'SELECT 1'
);
PREPARE stmt FROM @sql_nl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 评论表
DROP TABLE IF EXISTS nep_comment;
CREATE TABLE nep_comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    target_type VARCHAR(20) NOT NULL COMMENT '目标类型: NEWS/KNOWLEDGE/DETECTION',
    target_id BIGINT NOT NULL COMMENT '目标ID',
    user_id BIGINT NOT NULL COMMENT '评论人ID',
    user_name VARCHAR(50) COMMENT '评论人姓名',
    user_avatar VARCHAR(500) COMMENT '评论人头像',
    content VARCHAR(2000) NOT NULL COMMENT '评论内容',
    parent_id BIGINT DEFAULT NULL COMMENT '父评论ID(0=顶级评论)',
    reply_to_uid BIGINT COMMENT '回复目标用户ID',
    reply_to_name VARCHAR(50) COMMENT '回复目标用户名',
    like_count BIGINT DEFAULT 0 COMMENT '评论点赞数',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NULL,
    deleted TINYINT DEFAULT 0,
    KEY idx_target (target_type, target_id),
    KEY idx_user (user_id),
    KEY idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- 用户点赞记录表（去重 + 可取消）
DROP TABLE IF EXISTS nep_user_like;
CREATE TABLE nep_user_like (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    target_type VARCHAR(20) NOT NULL COMMENT 'NEWS/KNOWLEDGE/DETECTION/COMMENT',
    target_id BIGINT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_target (user_id, target_type, target_id),
    KEY idx_target (target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户点赞记录';

-- 用户收藏表
DROP TABLE IF EXISTS nep_user_favorite;
CREATE TABLE nep_user_favorite (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    target_type VARCHAR(20) NOT NULL COMMENT 'NEWS/KNOWLEDGE/DETECTION',
    target_id BIGINT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_target (user_id, target_type, target_id),
    KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户收藏表';
