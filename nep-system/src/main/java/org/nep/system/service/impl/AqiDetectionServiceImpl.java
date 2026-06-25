package org.nep.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.nep.common.exception.BusinessException;
import org.nep.system.entity.AqiDetection;
import org.nep.system.entity.SupervisionFeedback;
import org.nep.system.enums.FeedbackStatusEnum;
import org.nep.system.mapper.AqiDetectionMapper;
import org.nep.system.mapper.SupervisionFeedbackMapper;
import org.nep.system.service.AqiDetectionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class AqiDetectionServiceImpl
        extends ServiceImpl<AqiDetectionMapper, AqiDetection>
        implements AqiDetectionService {

    private final SupervisionFeedbackMapper feedbackMapper;

    public AqiDetectionServiceImpl(SupervisionFeedbackMapper feedbackMapper) {
        this.feedbackMapper = feedbackMapper;
    }

    @Override
    @Transactional
    public AqiDetection submitDetection(AqiDetection detection) {
        SupervisionFeedback feedback = feedbackMapper.selectById(detection.getFeedbackId());
        if (feedback == null) throw new BusinessException("关联的反馈记录不存在");
        if (feedback.getStatus() != FeedbackStatusEnum.ASSIGNED) {
            throw new BusinessException("反馈记录状态不正确，当前状态: " + feedback.getStatus().getLabel());
        }

        int finalAqi = Math.max(Math.max(detection.getSo2Aqi(), detection.getCoAqi()), detection.getPm25Aqi());
        detection.setFinalAqi(finalAqi);
        detection.setDetectionTime(LocalDateTime.now());
        this.save(detection);

        // CAS 原子更新：仅在状态仍为 ASSIGNED 时更新为 COMPLETED，防止并发重复提交
        int affected = feedbackMapper.update(
            new UpdateWrapper<SupervisionFeedback>()
                .eq("id", feedback.getId())
                .eq("status", FeedbackStatusEnum.ASSIGNED.getCode())
                .set("status", FeedbackStatusEnum.COMPLETED.getCode())
        );
        if (affected == 0) {
            throw new BusinessException("反馈状态已被其他网格员更新，请勿重复提交检测数据");
        }
        return detection;
    }
}
