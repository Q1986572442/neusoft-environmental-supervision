import request from './index'

/** 分页查询我的通知 */
export function getNotificationPage(page = 1, size = 10, userId) {
  return request.get('/notification/page', { params: { page, size, userId } })
}

/** 获取未读通知数量 */
export function getUnreadCount(userId) {
  return request.get('/notification/unread-count', { params: { userId } })
}

/** 标记为已读 */
export function readNotification(id) {
  return request.put(`/notification/read/${id}`)
}

/** 全部标记为已读 */
export function readAllNotifications(userId) {
  return request.put('/notification/read-all', null, { params: { userId } })
}

/** 删除通知 */
export function deleteNotification(id) {
  return request.delete(`/notification/${id}`)
}
