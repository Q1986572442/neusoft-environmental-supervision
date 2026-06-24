import request from './index'

/** 获取用户的所有会话列表 */
export function getConversations(userId) {
  return request.get('/chat-history/conversations', { params: { userId } })
}

/** 获取某个会话的完整对话记录 */
export function getMessages(userId, conversationId) {
  return request.get('/chat-history/messages', { params: { userId, conversationId } })
}

/** 保存一条对话消息 */
export function saveMessage(data) {
  return request.post('/chat-history/save', data)
}

/** 批量保存对话消息 */
export function saveMessagesBatch(messages) {
  return request.post('/chat-history/save-batch', messages)
}

/** 删除某个会话的全部记录 */
export function deleteConversation(userId, conversationId) {
  return request.delete('/chat-history/conversation', { params: { userId, conversationId } })
}

/** 清空用户所有对话记录 */
export function clearAllConversations(userId) {
  return request.delete('/chat-history/clear-all', { params: { userId } })
}
