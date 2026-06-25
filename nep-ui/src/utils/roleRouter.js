/**
 * 根据当前登录角色返回路由前缀
 */
export function getRolePrefix() {
  const role = localStorage.getItem('userRole') || ''
  switch (role) {
    case 'NEPS': return '/ne'
    case 'NEPG': return '/nepg'
    case 'NEPM': return '/admin'
    case 'NEPV': return '/nepv'
    default: return '/ne'
  }
}

/** 获取指定页面的角色路由 */
export function rolePath(path) {
  return getRolePrefix() + path
}
