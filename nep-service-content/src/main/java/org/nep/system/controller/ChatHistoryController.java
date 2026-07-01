package org.nep.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.nep.common.result.Result;
import org.nep.system.entity.ChatHistory;
import org.nep.system.mapper.ChatHistoryMapper;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI对话记忆控制器
 * 每个用户独立存储和读取自己的对话记录
 */
@Tag(name = "AI对话记忆")
@RestController
@RequestMapping("/api/chat-history")
public class ChatHistoryController {

    private final ChatHistoryMapper chatHistoryMapper;
    public ChatHistoryController(ChatHistoryMapper chatHistoryMapper) { this.chatHistoryMapper = chatHistoryMapper; }

    @Operation(summary = "获取用户的所有会话列表")
    @GetMapping("/conversations")
    public Result<List<Map<String, Object>>> getConversations(@RequestParam Long userId) {
        // 获取所有不同的会话ID
        LambdaQueryWrapper<ChatHistory> w = new LambdaQueryWrapper<>();
        w.eq(ChatHistory::getUserId, userId).eq(ChatHistory::getRole, "user")
         .orderByDesc(ChatHistory::getCreateTime);
        List<ChatHistory> userMsgs = chatHistoryMapper.selectList(w);

        // 用LinkedHashMap去重保持顺序
        Map<String, Map<String, Object>> seen = new LinkedHashMap<>();
        for (ChatHistory msg : userMsgs) {
            String convId = msg.getConversationId();
            if (!seen.containsKey(convId)) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("conversationId", convId);
                String title = msg.getContent();
                if (title.length() > 25) title = title.substring(0, 25) + "...";
                item.put("title", title);
                item.put("lastTime", msg.getCreateTime());
                item.put("messageCount", 0); // 下面填充
                seen.put(convId, item);
            }
        }

        // 统计每个会话的消息数
        for (String convId : seen.keySet()) {
            LambdaQueryWrapper<ChatHistory> countW = new LambdaQueryWrapper<>();
            countW.eq(ChatHistory::getConversationId, convId);
            seen.get(convId).put("messageCount", chatHistoryMapper.selectCount(countW));
        }

        return Result.ok(new ArrayList<>(seen.values()));
    }

    @Operation(summary = "获取某个会话的完整对话记录")
    @GetMapping("/messages")
    public Result<List<ChatHistory>> getMessages(@RequestParam Long userId,
                                                  @RequestParam String conversationId) {
        LambdaQueryWrapper<ChatHistory> w = new LambdaQueryWrapper<>();
        w.eq(ChatHistory::getUserId, userId)
         .eq(ChatHistory::getConversationId, conversationId)
         .orderByAsc(ChatHistory::getCreateTime);
        return Result.ok(chatHistoryMapper.selectList(w));
    }

    @Operation(summary = "保存一条对话消息")
    @PostMapping("/save")
    public Result<ChatHistory> save(@RequestBody ChatHistory msg) {
        chatHistoryMapper.insert(msg);
        return Result.ok(msg);
    }

    @Operation(summary = "批量保存对话消息")
    @PostMapping("/save-batch")
    public Result<Void> saveBatch(@RequestBody List<ChatHistory> messages) {
        for (ChatHistory msg : messages) {
            chatHistoryMapper.insert(msg);
        }
        return Result.success("保存成功");
    }

    @Operation(summary = "删除某个会话的全部记录")
    @DeleteMapping("/conversation")
    public Result<Void> deleteConversation(@RequestParam Long userId,
                                            @RequestParam String conversationId) {
        LambdaQueryWrapper<ChatHistory> w = new LambdaQueryWrapper<>();
        w.eq(ChatHistory::getUserId, userId)
         .eq(ChatHistory::getConversationId, conversationId);
        chatHistoryMapper.delete(w);
        return Result.success("会话已删除");
    }

    @Operation(summary = "清空用户所有对话记录")
    @DeleteMapping("/clear-all")
    public Result<Void> clearAll(@RequestParam Long userId) {
        LambdaQueryWrapper<ChatHistory> w = new LambdaQueryWrapper<>();
        w.eq(ChatHistory::getUserId, userId);
        chatHistoryMapper.delete(w);
        return Result.success("所有记录已清空");
    }
}
