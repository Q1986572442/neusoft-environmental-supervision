package org.nep.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.nep.common.base.BaseEntity;

/**
 * 数据字典类型实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("nep_dict_type")
public class DictType extends BaseEntity {
    /** 字典名称 */
    private String dictName;
    /** 字典类型编码 */
    private String dictType;
    /** 状态: 0-禁用, 1-启用 */
    private Integer status;
    /** 备注 */
    private String remark;
}
