package org.nep.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.nep.common.base.BaseEntity;

import java.time.LocalDateTime;

/**
 * 系统通知实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("nep_notification")
public class Notification extends BaseEntity {
    /** 接收用户ID */
    private Long userId;
    /** 通知标题 */
    private String title;
    /** 通知内容 */
    private String content;
    /** 通知类型: SYSTEM-系统, FEEDBACK-反馈, ASSIGN-指派, NEWS-新闻 */
    private String type;
    /** 关联业务ID */
    private Long relatedId;
    /** 是否已读 */
    private Integer isRead;
    /** 阅读时间 */
    private LocalDateTime readTime;
}
