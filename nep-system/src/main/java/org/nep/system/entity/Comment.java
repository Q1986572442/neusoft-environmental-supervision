package org.nep.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.nep.common.base.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("nep_comment")
public class Comment extends BaseEntity {
    private String targetType;     // NEWS / KNOWLEDGE / DETECTION
    private Long targetId;
    private Long userId;
    private String userName;
    private String userAvatar;
    private String content;
    private Long parentId;         // null=顶级评论, 非null=回复
    private Long replyToUid;
    private String replyToName;
    private Long likeCount;
}
