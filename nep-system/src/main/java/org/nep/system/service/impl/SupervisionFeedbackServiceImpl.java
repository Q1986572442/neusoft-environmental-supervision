package org.nep.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.nep.common.exception.BusinessException;
import org.nep.system.entity.SupervisionFeedback;
import org.nep.system.mapper.SupervisionFeedbackMapper;
import org.nep.system.service.SupervisionFeedbackService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SupervisionFeedbackServiceImpl
        extends ServiceImpl<SupervisionFeedbackMapper, SupervisionFeedback>
        implements SupervisionFeedbackService {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_ASSIGNED = "ASSIGNED";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_ESCALATED = "ESCALATED";

    @Override
    public SupervisionFeedback submit(SupervisionFeedback feedback) {
        feedback.setStatus(STATUS_PENDING);
        this.save(feedback);
        return feedback;
    }

    @Override
    @Transactional
    public void assign(Long feedbackId, Long inspectorId) {
        SupervisionFeedback f = this.getById(feedbackId);
        if (f == null) throw new BusinessException("反馈记录不存在");
        if (!STATUS_PENDING.equals(f.getStatus())) throw new BusinessException("该反馈已指派或已完成");

        f.setAssignedInspectorId(inspectorId);
        f.setAssignTime(LocalDateTime.now());
        f.setStatus(STATUS_ASSIGNED);
        this.updateById(f);
    }

    @Override
    @Transactional
    public void reject(Long feedbackId, Long inspectorId, String reason) {
        SupervisionFeedback f = this.getById(feedbackId);
        if (f == null) throw new BusinessException("反馈记录不存在");
        if (!STATUS_ASSIGNED.equals(f.getStatus())) throw new BusinessException("只能拒绝已指派的反馈");
        if (!inspectorId.equals(f.getAssignedInspectorId())) throw new BusinessException("只能拒绝指派给自己的反馈");
        f.setStatus(STATUS_REJECTED);
        f.setRejectReason(reason);
        this.updateById(f);
    }

    @Override
    @Transactional
    public void transfer(Long feedbackId, Long toInspectorId) {
        SupervisionFeedback f = this.getById(feedbackId);
        if (f == null) throw new BusinessException("反馈记录不存在");
        if (!STATUS_ASSIGNED.equals(f.getStatus()) && !STATUS_REJECTED.equals(f.getStatus()))
            throw new BusinessException("只能转派已指派或被拒绝的反馈");
        f.setAssignedInspectorId(toInspectorId);
        f.setAssignTime(LocalDateTime.now());
        f.setStatus(STATUS_ASSIGNED);
        f.setRejectReason(null);
        this.updateById(f);
    }

    @Override
    @Transactional
    public void rate(Long feedbackId, Long supervisorId, Integer rating, String comment) {
        SupervisionFeedback f = this.getById(feedbackId);
        if (f == null) throw new BusinessException("反馈记录不存在");
        if (!STATUS_COMPLETED.equals(f.getStatus())) throw new BusinessException("只能评价已完成的反馈");
        if (!supervisorId.equals(f.getSupervisorId())) throw new BusinessException("只能评价自己提交的反馈");
        if (rating == null || rating < 1 || rating > 5) throw new BusinessException("评分范围为1-5");
        f.setRating(rating);
        f.setRatingComment(comment);
        f.setRatingTime(LocalDateTime.now());
        this.updateById(f);
    }

    @Override
    @Transactional
    public int batchAssign(List<Long> feedbackIds, Long inspectorId) {
        int count = 0;
        for (Long id : feedbackIds) {
            SupervisionFeedback f = this.getById(id);
            if (f != null && STATUS_PENDING.equals(f.getStatus())) {
                f.setAssignedInspectorId(inspectorId);
                f.setAssignTime(LocalDateTime.now());
                f.setStatus(STATUS_ASSIGNED);
                this.updateById(f);
                count++;
            }
        }
        return count;
    }
}