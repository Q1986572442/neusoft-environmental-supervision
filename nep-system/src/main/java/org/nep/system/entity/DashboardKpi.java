package org.nep.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.nep.common.base.BaseEntity;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("nep_dashboard_kpi")
public class DashboardKpi extends BaseEntity {
    private String kpiKey;
    private String kpiLabel;
    private BigDecimal kpiValue;
    private String kpiUnit;
    private Integer sortOrder;
}