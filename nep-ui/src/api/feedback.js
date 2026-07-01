import request from './index'

/**
 * 提交监督反馈
 */
export function submitFeedback(data) {
  return request.post('/feedback/submit', data)
}

/**
 * 分页查询反馈列表（支持关键词/日期筛选）
 */
export function getFeedbackPage(page = 1, size = 10, status = '', keyword = '', startDate = '', endDate = '', assignedInspectorId = null) {
  const params = { page, size, status, keyword, startDate, endDate }
  if (assignedInspectorId) params.assignedInspectorId = assignedInspectorId
  return request.get('/feedback/page', { params })
}

/**
 * 获取反馈详情
 */
export function getFeedbackById(id) {
  return request.get(`/feedback/${id}`)
}

/**
 * 管理员指派网格员
 */
export function assignInspector(id, inspectorId) {
  return request.put(`/feedback/assign/${id}`, null, { params: { inspectorId } })
}

/**
 * 查询我的反馈
 */
export function getMyFeedback(supervisorId) {
  return request.get(`/feedback/my/${supervisorId}`)
}

// ==================== 新增 API ====================

/**
 * 网格员拒绝反馈
 */
export function rejectFeedback(id, inspectorId, reason) {
  return request.put(`/feedback/reject/${id}`, null, { params: { inspectorId, reason } })
}

/**
 * 管理员转派反馈
 */
export function transferFeedback(id, toInspectorId) {
  return request.put(`/feedback/transfer/${id}`, null, { params: { toInspectorId } })
}

/**
 * 监督员评价已完成反馈
 */
export function rateFeedback(id, supervisorId, rating, ratingComment) {
  return request.put(`/feedback/rate/${id}`, { supervisorId, rating, ratingComment })
}

/**
 * 管理员批量指派
 */
export function batchAssignFeedback(ids, inspectorId) {
  return request.post('/feedback/batch-assign', { ids, inspectorId })
}

/**
 * 监督员撤回反馈（仅PENDING状态）
 */
export function cancelFeedback(id, supervisorId) {
  return request.put(`/feedback/cancel/${id}`, null, { params: { supervisorId } })
}
