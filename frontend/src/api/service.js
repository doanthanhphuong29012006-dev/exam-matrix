import { realApi } from './realApi'
import { mockApi } from '../mocks/mockApi'

export const isMockMode = String(import.meta.env.VITE_USE_MOCK_API).toLowerCase() !== 'false'
export const api = isMockMode ? mockApi : realApi
