import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { ErrorState, LoadingState } from '../components/UI'

export function ProtectedRoute() {
  const { isAuthenticated, isChecking, authError, retrySession, logout } = useAuth(); const location = useLocation()
  if (isChecking) return <LoadingState label="Đang kiểm tra phiên đăng nhập..." />
  if (authError) return <><ErrorState error={authError} onRetry={retrySession} /><button className="button secondary" onClick={logout}>Về đăng nhập</button></>
  return isAuthenticated ? <Outlet /> : <Navigate to="/dang-nhap" replace state={{ from: location }} />
}

export function AdminRoute() {
  const { user } = useAuth()
  return user?.role === 'ADMIN' ? <Outlet /> : <Navigate to="/403" replace />
}
