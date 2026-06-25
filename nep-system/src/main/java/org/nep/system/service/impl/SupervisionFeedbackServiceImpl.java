package org.nep.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.nep.common.exception.BusinessException;
import org.nep.system.entity.SupervisionFeedback;
import org.nep.system.enums.FeedbackStatusEnum;
import org.nep.system.mapper.SupervisionFeedbackMapper;
import org.nep.system.service.SupervisionFeedbackService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class SupervisionFeedbackServiceImpl
        extends ServiceImpl<SupervisionFeedbackMapper, SupervisionFeedback>
        implements SupervisionFeedbackService {

    @Override
    public SupervisionFeedback submit(SupervisionFeedback feedback) {
        feedback.setStatus(FeedbackStatusEnum.PENDING);
        this.save(feedback);
        return feedback;
    }

    @Override
    @Transactional
    public void assign(Long feedbackId, Long inspectorId) {
        SupervisionFeedback f = this.getById(feedbackId);
        if (f == null) throw new BusinessException("反馈记录不存在");
        if (f.getStatus() != FeedbackStatusEnum.PENDING) {
            throw new BusinessException("该反馈已指派或已完成，当前状态: " + f.getStatus().getLabel());
        }

        // CAS 原子更新：仅在状态仍为 PENDING 时更新为 ASSIGNED，防止并发重复指派
        UpdateWrapper<SupervisionFeedback> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", feedbackId)
                     .eq("status", FeedbackStatusEnum.PENDING.getCode())
                     .set("status", FeedbackStatusEnum.ASSIGNED.getCode())
                     .set("assigned_inspector_id", inspectorId)
                     .set("assign_time", LocalDateTime.now());
        int affected = this.baseMapper.update(updateWrapper);
        if (affected == 0) {
            throw new BusinessException("反馈已被其他管理员指派，请刷新后重试");
        }
    }
}
