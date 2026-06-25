package org.nep.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.nep.common.base.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("nep_dashboard_pollution_level")
public class DashboardPollutionLevel extends BaseEntity {
    private String levelKey;
    private String levelName;
    private Integer levelValue;
    private String levelColor;
    private Integer sortOrder;
}