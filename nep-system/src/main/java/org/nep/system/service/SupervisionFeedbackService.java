package org.nep.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.nep.system.entity.SupervisionFeedback;

import java.util.List;

public interface SupervisionFeedbackService extends IService<SupervisionFeedback> {
    SupervisionFeedback submit(SupervisionFeedback feedback);
    void assign(Long feedbackId, Long inspectorId);

    /** 网格员拒绝反馈 */
    void reject(Long feedbackId, Long inspectorId, String reason);

    /** 管理员转派反馈给其他网格员 */
    void transfer(Long feedbackId, Long toInspectorId);

    /** 监督员评价已完成反馈 */
    void rate(Long feedbackId, Long supervisorId, Integer rating, String comment);

    /** 管理员批量指派 */
    int batchAssign(List<Long> feedbackIds, Long inspectorId);
}
