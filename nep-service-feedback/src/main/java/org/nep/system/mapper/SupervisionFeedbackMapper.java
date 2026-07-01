package org.nep.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.nep.system.entity.SupervisionFeedback;

import java.util.List;

/**
 * 监督反馈 Mapper
 * 支持工单超时扫描、自动升级、冷数据归档
 */
public interface SupervisionFeedbackMapper extends BaseMapper<SupervisionFeedback> {

    /**
     * 查询超过72小时仍未处理的工单（PENDING 或 ASSIGNED 状态）
     * 用于每日自动升级扫描
     */
    @Select("""
        SELECT * FROM nep_supervision_feedback
        WHERE deleted = 0
          AND status IN ('PENDING', 'ASSIGNED', 'ESCALATED')
          AND escalation_level < #{maxLevel}
          AND create_time <= DATE_SUB(NOW(), INTERVAL #{hours} HOUR)
        ORDER BY escalation_level DESC, create_time ASC
        """)
    List<SupervisionFeedback> selectOverdueForEscalation(
            @Param("hours") int hours,
            @Param("maxLevel") int maxLevel);

    /**
     * 查询已完成超过90天的工单（待归档）
     */
    @Select("""
        SELECT * FROM nep_supervision_feedback
        WHERE deleted = 0
          AND status = 'COMPLETED'
          AND archived = 0
          AND update_time <= DATE_SUB(NOW(), INTERVAL #{days} DAY)
        ORDER BY update_time ASC
        LIMIT #{batchSize}
        """)
    List<SupervisionFeedback> selectCompletedForArchive(
            @Param("days") int days,
            @Param("batchSize") int batchSize);

    /**
     * 批量归档（将 archived 标记设为 1）
     */
    @Update("""
        UPDATE nep_supervision_feedback
        SET archived = 1, archive_time = NOW(), update_time = NOW()
        WHERE id IN
        <foreach collection="ids" item="id" open="(" separator="," close=")">
            #{id}
        </foreach>
        """)
    int batchMarkArchived(@Param("ids") List<Long> ids);

    /**
     * 统计各状态工单数量
     */
    @Select("""
        SELECT status, COUNT(*) AS cnt
        FROM nep_supervision_feedback
        WHERE deleted = 0
        GROUP BY status
        """)
    List<java.util.Map<String, Object>> countByStatus();

    /**
     * 统计超时工单数量（>72h 未处理）
     */
    @Select("""
        SELECT COUNT(*) FROM nep_supervision_feedback
        WHERE deleted = 0
          AND status IN ('PENDING', 'ASSIGNED')
          AND create_time <= DATE_SUB(NOW(), INTERVAL #{hours} HOUR)
        """)
    long countOverdue(@Param("hours") int hours);

    /**
     * 查询已升级的工单列表（按级别排序）
     */
    @Select("""
        SELECT * FROM nep_supervision_feedback
        WHERE deleted = 0
          AND status = 'ESCALATED'
        ORDER BY escalation_level DESC, escalated_at DESC
        """)
    List<SupervisionFeedback> selectEscalated();
}
