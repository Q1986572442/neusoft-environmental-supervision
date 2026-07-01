package org.nep.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.nep.common.base.BaseEntity;

/**
 * 反馈内部备注
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("nep_feedback_note")
public class FeedbackNote extends BaseEntity {
    /** 关联反馈ID */
    private Long feedbackId;
    /** 创建人ID */
    private Long userId;
    /** 创建人姓名 */
    private String userName;
    /** 备注内容 */
    private String content;
}
