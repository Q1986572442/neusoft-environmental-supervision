package org.nep.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.nep.system.entity.SupervisionFeedback;

/**
 * 监督反馈 Mapper（跨服务只读查询 - 用于NEPV大屏实时统计）
 * <p>
 * 注：在微服务架构中，跨服务的DB直连仅用于高性能只读统计场景。
 * 生产环境建议通过 Feign/RPC 调用反馈服务获取数据。
 */
public interface SupervisionFeedbackMapper extends BaseMapper<SupervisionFeedback> {

    /** 统计待处理反馈数（PENDING + ASSIGNED） */
    @Select("SELECT COUNT(*) FROM nep_supervision_feedback WHERE deleted = 0 AND status IN ('PENDING', 'ASSIGNED')")
    long countPendingFeedback();
}
