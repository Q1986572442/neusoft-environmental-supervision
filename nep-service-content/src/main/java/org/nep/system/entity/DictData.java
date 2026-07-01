package org.nep.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.nep.common.base.BaseEntity;

/**
 * 数据字典数据实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("nep_dict_data")
public class DictData extends BaseEntity {
    /** 字典类型编码 */
    private String dictType;
    /** 字典标签 */
    private String dictLabel;
    /** 字典值 */
    private String dictValue;
    /** 样式属性 */
    private String cssClass;
    /** 表格回显样式 */
    private String listClass;
    /** 是否默认 */
    private Integer isDefault;
    /** 排序 */
    private Integer sortOrder;
    /** 状态: 0-禁用, 1-启用 */
    private Integer status;
    /** 备注 */
    private String remark;
}
