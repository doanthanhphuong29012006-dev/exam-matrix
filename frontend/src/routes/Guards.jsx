import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

export function ProtectedRoute() {
  const { isAuthenticated } = useAuth(); const location = useLocation()
  return isAuthenticated ? <Outlet /> : <Navigate to="/dang-nhap" replace state={{ from: location }} />
}

export function AdminRoute() {
  const { user } = useAuth()
  return user?.role === 'ADMIN' ? <Outlet /> : <Navigate to="/403" replace />
}
