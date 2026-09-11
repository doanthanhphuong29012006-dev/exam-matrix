import axios from 'axios'
import { normalizeError } from '../utils'

const httpClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
})

httpClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('exam_access_token')
  if (token && config.url !== '/auth/login') config.headers.Authorization = `Bearer ${token}`
  if (config.params) {
    config.params = Object.fromEntries(Object.entries(config.params).filter(([, value]) => value !== '' && value != null))
  }
  return config
})

httpClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const normalized = normalizeError(error)
    error.message = normalized.message
    error.fieldErrors = normalized.fieldErrors
    error.status = error.response?.status
    if (error.config?.url !== '/auth/login' && [401, 423].includes(error.status)) {
      localStorage.removeItem('exam_access_token')
      localStorage.removeItem('exam_current_user')
      if (window.location.pathname !== '/dang-nhap') window.location.assign('/dang-nhap?expired=1')
    }
    return Promise.reject(error)
  },
)

export default httpClient
