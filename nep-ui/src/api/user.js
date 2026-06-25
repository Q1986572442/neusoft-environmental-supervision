import request from './index'
import axios from 'axios'

/** 用户注册 */
export function register(data) {
  return request.post('/user/register', data)
}

/** 用户登录 */
export function login(phone, password) {
  return request.post('/user/login', null, { params: { phone, password } })
}

/** 获取用户信息 */
export function getUserById(id) {
  return request.get(`/user/${id}`)
}

/** 获取用户列表 */
export function getUserList() {
  return request.get('/user/list')
}

/** 更新用户信息 */
export function updateUser(id, data) {
  return request.put(`/user/${id}`, data)
}

/** 修改密码 */
export function changePassword(data) {
  return request.put('/user/change-password', data)
}

/** 上传头像 */
export function uploadAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)
  
  const token = localStorage.getItem('token')
  
  return axios.post('/api/file/avatar', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    },
    timeout: 30000
  })
}
