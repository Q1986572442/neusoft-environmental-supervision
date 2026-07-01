package org.nep.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.nep.common.base.BaseEntity;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("nep_dashboard_health_indicator")
public class DashboardHealthIndicator extends BaseEntity {
    private String indicatorKey;
    private String indicatorName;
    private BigDecimal indicatorValue;
    private BigDecimal maxValue;
    private String unit;
    private Integer sortOrder;
}