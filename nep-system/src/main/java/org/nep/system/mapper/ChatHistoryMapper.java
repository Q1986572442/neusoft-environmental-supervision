package org.nep.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.nep.system.entity.ChatHistory;

import java.util.List;

@Mapper
public interface ChatHistoryMapper extends BaseMapper<ChatHistory> {

    /** 获取用户的所有会话ID列表（按最新消息时间排序） */
    @Select("SELECT DISTINCT conversation_id FROM nep_chat_history WHERE user_id = #{userId} AND deleted = 0 ORDER BY MAX(create_time) DESC")
    List<String> getConversationIds(Long userId);
}
