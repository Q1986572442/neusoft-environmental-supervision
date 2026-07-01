import request from './index'

export function getDashboardAll() {
  return request.get('/dashboard/all')
}

export function getDashboardKpis() {
  return request.get('/dashboard/kpis')
}

export function getDashboardAqiDaily() {
  return request.get('/dashboard/aqi-daily')
}

export function getDashboardPollutionLevels() {
  return request.get('/dashboard/pollution-levels')
}

export function getDashboardHealthIndicators() {
  return request.get('/dashboard/health-indicators')
}

export function getDashboardProvinceAnomalies() {
  return request.get('/dashboard/province-anomalies')
}