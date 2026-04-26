import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 车辆相关 API
export const vehicleApi = {
  getAll: () => api.get('/vehicles'),
  getById: (id) => api.get(`/vehicles/${id}`),
  create: (data) => api.post('/vehicles', data),
  update: (id, data) => api.put(`/vehicles/${id}`, data),
  delete: (id) => api.delete(`/vehicles/${id}`),
  getAvailable: () => api.get('/vehicles/available'),
  recommend: (params) => api.get('/vehicles/recommend', { params }),
  checkMaintenance: (id) => api.get(`/vehicles/${id}/maintenance-check`)
}

// 司机相关 API
export const driverApi = {
  getAll: () => api.get('/drivers'),
  getById: (id) => api.get(`/drivers/${id}`),
  create: (data) => api.post('/drivers', data),
  update: (id, data) => api.put(`/drivers/${id}`, data),
  delete: (id) => api.delete(`/drivers/${id}`),
  getAvailable: () => api.get('/drivers/available'),
  getAvailableForVehicleType: (vehicleType) => api.get('/drivers/available-for-vehicle-type', { params: { vehicleType } })
}

// 员工相关 API
export const employeeApi = {
  getAll: () => api.get('/employees'),
  getById: (id) => api.get(`/employees/${id}`),
  create: (data) => api.post('/employees', data),
  update: (id, data) => api.put(`/employees/${id}`, data),
  delete: (id) => api.delete(`/employees/${id}`)
}

// 用车申请相关 API
export const applicationApi = {
  getAll: () => api.get('/applications'),
  getById: (id) => api.get(`/applications/${id}`),
  create: (data) => api.post('/applications', data),
  submit: (id) => api.post(`/applications/${id}/submit`),
  approve: (id, data) => api.post(`/applications/${id}/approve`, data),
  reject: (id, data) => api.post(`/applications/${id}/reject`, data),
  assign: (id, data) => api.post(`/applications/${id}/assign`, data),
  start: (id) => api.post(`/applications/${id}/start`),
  complete: (id, data) => api.post(`/applications/${id}/complete`, data),
  review: (id, data) => api.post(`/applications/${id}/review`, data),
  cancel: (id, data) => api.post(`/applications/${id}/cancel`, data)
}

// 维修保养相关 API
export const maintenanceApi = {
  getAll: () => api.get('/maintenance'),
  getById: (id) => api.get(`/maintenance/${id}`),
  create: (data) => api.post('/maintenance', data),
  start: (id) => api.post(`/maintenance/${id}/start`),
  complete: (id, data) => api.post(`/maintenance/${id}/complete`, data),
  cancel: (id, data) => api.post(`/maintenance/${id}/cancel`, data)
}

// 报表相关 API
export const reportApi = {
  getAll: () => api.get('/reports'),
  generate: (data) => api.post('/reports/generate', data),
  getCurrent: () => api.get('/reports/current')
}

export default api
