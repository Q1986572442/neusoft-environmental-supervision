package org.nep.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.nep.common.base.BaseEntity;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("nep_dashboard_aqi_daily")
public class DashboardAqiDaily extends BaseEntity {
    private LocalDate recordDate;
    private Integer aqiValue;
    private String aqiLevel;
}