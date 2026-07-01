package org.nep.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI聊天记录实体
 */
@Data
@TableName("nep_chat_history")
public class ChatHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 用户ID */
    private Long userId;
    /** 会话ID（同一轮对话共享） */
    private String conversationId;
    /** 角色: user / ai */
    private String role;
    /** 消息内容 */
    private String content;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 逻辑删除 */
    private Integer deleted;
}
