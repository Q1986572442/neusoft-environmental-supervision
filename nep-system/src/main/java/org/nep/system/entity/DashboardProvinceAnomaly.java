package org.nep.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.nep.common.base.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("nep_dashboard_province_anomaly")
public class DashboardProvinceAnomaly extends BaseEntity {
    private String provinceCode;
    private String provinceName;
    private Integer anomalyCount;
    private Integer sortOrder;
}