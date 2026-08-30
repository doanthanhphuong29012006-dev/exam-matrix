import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { Navigate, useLocation, useNavigate, useSearchParams } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { FieldError } from '../components/UI'

export default function LoginPage() {
  const { login, isAuthenticated } = useAuth(); const navigate = useNavigate(); const location = useLocation(); const [searchParams] = useSearchParams()
  const { register, handleSubmit, setError, formState: { errors, isSubmitting } } = useForm({ defaultValues: { username: '', password: '' } })
  useEffect(() => { document.title = 'Đăng nhập | Exam Matrix' }, [])
  if (isAuthenticated) return <Navigate to="/" replace />
  const submit = async (values) => {
    try { await login(values); navigate(location.state?.from?.pathname || '/', { replace: true }) }
    catch (error) { setError('root', { message: error.message || 'Không thể đăng nhập' }) }
  }
  return <main className="login-page"><section className="login-panel"><div className="login-heading"><span className="eyebrow">HỆ THỐNG QUẢN LÝ GIÁO DỤC</span><h1>Đăng nhập Exam Matrix</h1><p>Quản lý ngân hàng câu hỏi, cấu hình ma trận và sinh đề thi.</p></div>
    {searchParams.get('expired') && <div className="alert warning">Phiên làm việc đã hết hạn. Vui lòng đăng nhập lại.</div>}
    {errors.root && <div className="alert error" role="alert">{errors.root.message}</div>}
    <form onSubmit={handleSubmit(submit)} noValidate>
      <label>Tên đăng nhập<input autoFocus autoComplete="username" {...register('username', { required: 'Vui lòng nhập tên đăng nhập' })} /></label><FieldError>{errors.username?.message}</FieldError>
      <label>Mật khẩu<input type="password" autoComplete="current-password" {...register('password', { required: 'Vui lòng nhập mật khẩu' })} /></label><FieldError>{errors.password?.message}</FieldError>
      <button className="button full" disabled={isSubmitting}>{isSubmitting ? 'Đang đăng nhập...' : 'Đăng nhập'}</button>
    </form>
    <div className="demo-accounts"><strong>Tài khoản dùng thử</strong><span>Quản trị: admin / admin123</span><span>Giáo viên: teacher / teacher123</span></div>
  </section></main>
}
