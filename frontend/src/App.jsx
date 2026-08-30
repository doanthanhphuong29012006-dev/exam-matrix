import { Navigate, Route, Routes } from 'react-router-dom'
import MainLayout from './layouts/MainLayout'
import { AdminRoute, ProtectedRoute } from './routes/Guards'
import LoginPage from './pages/LoginPage'
import DashboardPage from './pages/DashboardPage'
import SubjectsPage from './pages/SubjectsPage'
import ChaptersPage from './pages/ChaptersPage'
import QuestionsPage from './pages/QuestionsPage'
import MatricesPage, { MatrixDetailPage } from './pages/MatricesPage'
import PapersPage, { PaperDetailPage } from './pages/PapersPage'
import UsersPage from './pages/UsersPage'
import ProfilePage from './pages/ProfilePage'
import { ForbiddenPage, NotFoundPage } from './pages/StatusPages'

export default function App() {
  return <Routes>
    <Route path="/dang-nhap" element={<LoginPage />} />
    <Route element={<ProtectedRoute />}>
      <Route element={<MainLayout />}>
        <Route index element={<DashboardPage />} />
        <Route path="mon-hoc" element={<SubjectsPage />} />
        <Route path="chuong" element={<ChaptersPage />} />
        <Route path="cau-hoi" element={<QuestionsPage />} />
        <Route path="ma-tran" element={<MatricesPage />} />
        <Route path="ma-tran/:id" element={<MatrixDetailPage />} />
        <Route path="de-thi" element={<PapersPage />} />
        <Route path="de-thi/:id" element={<PaperDetailPage />} />
        <Route element={<AdminRoute />}><Route path="nguoi-dung" element={<UsersPage />} /></Route>
        <Route path="ho-so" element={<ProfilePage />} />
      </Route>
      <Route path="403" element={<ForbiddenPage />} />
    </Route>
    <Route path="/404" element={<NotFoundPage />} />
    <Route path="*" element={<Navigate to="/404" replace />} />
  </Routes>
}
