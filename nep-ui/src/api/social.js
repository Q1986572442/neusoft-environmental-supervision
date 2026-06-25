import request from './index'

// ==================== 评论 ====================

/** 发表评论或回复 */
export function postComment(data) {
  return request.post('/social/comment', data)
}

/** 获取评论列表 */
export function getComments(targetType, targetId) {
  return request.get('/social/comments', { params: { targetType, targetId } })
}

// ==================== 点赞 ====================

/** 切换点赞（自动判断点赞/取消） */
export function toggleLike(userId, targetType, targetId) {
  return request.post('/social/like', null, { params: { userId, targetType, targetId } })
}

/** 检查是否已点赞 */
export function checkLiked(userId, targetType, targetId) {
  return request.get('/social/liked', { params: { userId, targetType, targetId } })
}

// ==================== 收藏 ====================

/** 切换收藏 */
export function toggleFavorite(userId, targetType, targetId) {
  return request.post('/social/favorite', null, { params: { userId, targetType, targetId } })
}

/** 检查是否已收藏 */
export function checkFavorited(userId, targetType, targetId) {
  return request.get('/social/favorited', { params: { userId, targetType, targetId } })
}

/** 获取用户收藏列表 */
export function getUserFavorites(userId) {
  return request.get('/social/favorites', { params: { userId } })
}
