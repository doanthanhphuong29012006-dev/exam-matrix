import { Link } from 'react-router-dom'

export function ForbiddenPage() { return <div className="center-page"><div><span className="status-code">403</span><h1>Không có quyền truy cập</h1><p>Tài khoản của bạn không được phép mở trang này.</p><Link className="button" to="/">Về trang tổng quan</Link></div></div> }
export function NotFoundPage() { return <div className="center-page"><div><span className="status-code">404</span><h1>Không tìm thấy trang</h1><p>Địa chỉ bạn truy cập không tồn tại hoặc đã được thay đổi.</p><Link className="button" to="/">Về trang tổng quan</Link></div></div> }
