import request from './index'

/**
 * 添加内部备注
 */
export function addNote(data) {
  return request.post('/feedback-note', data)
}

/**
 * 查询某反馈的所有备注
 */
export function getNotesByFeedback(feedbackId) {
  return request.get(`/feedback-note/feedback/${feedbackId}`)
}

/**
 * 删除备注
 */
export function deleteNote(id) {
  return request.delete(`/feedback-note/${id}`)
}
