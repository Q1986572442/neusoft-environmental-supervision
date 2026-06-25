import request from './index'

/** 获取启用的轮播图列表 */
export function getBannerList() {
  return request.get('/banner/list')
}

/** 获取所有轮播图（管理员） */
export function getAllBanners() {
  return request.get('/banner/all')
}

/** 新增轮播图 */
export function createBanner(data) {
  return request.post('/banner', data)
}

/** 更新轮播图 */
export function updateBanner(id, data) {
  return request.put(`/banner/${id}`, data)
}

/** 删除轮播图 */
export function deleteBanner(id) {
  return request.delete(`/banner/${id}`)
}
