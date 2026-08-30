export const formatDate = (value) => value
  ? new Intl.DateTimeFormat('vi-VN', { dateStyle: 'short', timeStyle: 'short' }).format(new Date(value))
  : '—'

export const newId = () => globalThis.crypto?.randomUUID?.() || `${Date.now()}-${Math.random()}`

export const normalizeError = (error) => ({
  message: error?.response?.data?.message || error?.message || 'Đã có lỗi xảy ra',
  fieldErrors: error?.response?.data?.fieldErrors || error?.fieldErrors || {},
})
