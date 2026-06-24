package org.nep.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.nep.common.base.BaseEntity;

/**
 * 首页轮播图实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("nep_banner")
public class Banner extends BaseEntity {
    /** 轮播图标题 */
    private String title;
    /** 图片URL */
    private String imageUrl;
    /** 跳转链接 */
    private String linkUrl;
    /** 排序值 */
    private Integer sortOrder;
    /** 状态: 0-禁用, 1-启用 */
    private Integer status;
}
