import { createContext, useCallback, useContext, useMemo, useState } from 'react'
import { api } from '../api/service'

const AuthContext = createContext(null)
const TOKEN_KEY = 'exam_access_token'
const USER_KEY = 'exam_current_user'

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem(USER_KEY)
    try { return saved ? JSON.parse(saved) : null } catch { return null }
  })

  const login = useCallback(async (credentials) => {
    const result = await api.login(credentials)
    localStorage.setItem(TOKEN_KEY, result.accessToken)
    localStorage.setItem(USER_KEY, JSON.stringify(result.user))
    setUser(result.user)
    return result.user
  }, [])

  const logout = useCallback(async () => {
    try { await api.logout() } finally {
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USER_KEY)
      setUser(null)
    }
  }, [])

  const updateLocalUser = useCallback((next) => {
    localStorage.setItem(USER_KEY, JSON.stringify(next))
    setUser(next)
  }, [])

  const value = useMemo(() => ({ user, login, logout, updateLocalUser, isAuthenticated: Boolean(user && localStorage.getItem(TOKEN_KEY)) }), [user, login, logout, updateLocalUser])
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export const useAuth = () => useContext(AuthContext)
