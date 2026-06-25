package org.nep.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.nep.common.base.BaseEntity;

import java.time.LocalDateTime;

/**
 * 环保新闻公告实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("nep_news")
public class News extends BaseEntity {
    /** 新闻标题 */
    private String title;
    /** 新闻摘要 */
    private String summary;
    /** 新闻内容（富文本） */
    private String content;
    /** 封面图片URL */
    private String coverImage;
    /** 类型: NEWS-新闻, NOTICE-公告, POLICY-政策 */
    private String newsType;
    /** 状态: 0-草稿, 1-已发布, 2-已下架 */
    private Integer status;
    /** 浏览次数 */
    private Long viewCount;
    /** 点赞次数 */
    private Long likeCount;
    /** 发布人ID */
    private Long publisherId;
    /** 发布时间 */
    private LocalDateTime publishTime;
}
