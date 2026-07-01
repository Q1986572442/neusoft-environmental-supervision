package org.nep.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.nep.common.base.BaseEntity;

/**
 * 监督反馈实体（跨服务只读 - NEPV大屏统计）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("nep_supervision_feedback")
public class SupervisionFeedback extends BaseEntity {
    private Long supervisorId;
    private Long provinceId;
    private Long cityId;
    private String status;
    private Long assignedInspectorId;
}
