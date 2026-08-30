import axios from 'axios'

const httpClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
})

httpClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('exam_access_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

httpClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('exam_access_token')
      localStorage.removeItem('exam_current_user')
      if (window.location.pathname !== '/dang-nhap') window.location.assign('/dang-nhap?expired=1')
    }
    return Promise.reject(error)
  },
)

export default httpClient
