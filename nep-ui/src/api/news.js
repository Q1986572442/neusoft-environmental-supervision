import request from './index'

/** 分页查询新闻列表 */
export function getNewsPage(page = 1, size = 10, params = {}) {
  return request.get('/news/page', { params: { page, size, ...params } })
}

/** 获取新闻详情 */
export function getNewsById(id) {
  return request.get(`/news/${id}`)
}

/** 获取最新新闻 */
export function getLatestNews(count = 5) {
  return request.get('/news/latest', { params: { count } })
}

/** 发布新闻（管理员） */
export function createNews(data) {
  return request.post('/news', data)
}

/** 更新新闻（管理员） */
export function updateNews(id, data) {
  return request.put(`/news/${id}`, data)
}

/** 删除新闻（管理员） */
export function deleteNews(id) {
  return request.delete(`/news/${id}`)
}
