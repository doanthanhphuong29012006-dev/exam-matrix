import { useState } from 'react'
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { roleLabel } from '../constants'

const items = [
  { to: '/', label: 'Tổng quan' },
  { to: '/mon-hoc', label: 'Môn học' },
  { to: '/chuong', label: 'Chương' },
  { to: '/cau-hoi', label: 'Ngân hàng câu hỏi' },
  { to: '/ma-tran', label: 'Ma trận đề' },
  { to: '/de-thi', label: 'Đề thi' },
  { to: '/nguoi-dung', label: 'Người dùng', admin: true },
  { to: '/ho-so', label: 'Hồ sơ cá nhân' },
]

export default function MainLayout() {
  const { user, logout } = useAuth(); const navigate = useNavigate(); const location = useLocation(); const [open, setOpen] = useState(false)
  const signOut = async () => { await logout(); navigate('/dang-nhap', { replace: true }) }
  return <div className="app-shell">
    <aside className={`sidebar ${open ? 'sidebar-open' : ''}`}>
      <div className="brand"><strong>EXAM MATRIX</strong><span>Quản lý đề thi</span></div>
      <nav className="main-nav" aria-label="Điều hướng chính">{items.filter((item) => !item.admin || user?.role === 'ADMIN').map((item) => <NavLink key={item.to} to={item.to} end={item.to === '/'} onClick={() => setOpen(false)}>{item.label}</NavLink>)}</nav>
    </aside>
    {open && <button className="sidebar-overlay" aria-label="Đóng menu" onClick={() => setOpen(false)} />}
    <div className="main-column">
      <header className="topbar"><button className="menu-button" onClick={() => setOpen(!open)} aria-label="Mở menu">Menu</button><div className="topbar-user"><div><strong>{user?.fullName}</strong><span>{roleLabel(user?.role)}</span></div><button className="button secondary small" onClick={signOut}>Đăng xuất</button></div></header>
      <main className="main-content" key={location.pathname}><Outlet /></main>
    </div>
  </div>
}
