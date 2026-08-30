import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { api } from '../api/service'
import { difficultyLabel } from '../constants'
import { formatDate } from '../utils'
import { ErrorState, LoadingState, PageHeader } from '../components/UI'

export default function DashboardPage() {
  const query = useQuery({ queryKey: ['dashboard'], queryFn: api.getDashboard })
  if (query.isLoading) return <><PageHeader title="Tổng quan" /><LoadingState /></>
  if (query.isError) return <><PageHeader title="Tổng quan" /><ErrorState error={query.error} onRetry={query.refetch} /></>
  const data = query.data
  const cards = [['Môn học', data.totalSubjects], ['Chương', data.totalChapters], ['Câu hỏi', data.totalQuestions], ['Ma trận đề', data.totalMatrices], ['Đề đã sinh', data.totalPapers]]
  return <><PageHeader title="Tổng quan" description="Theo dõi nhanh dữ liệu và hoạt động của hệ thống." />
    <section className="stat-grid" aria-label="Số liệu tổng quan">{cards.map(([label, value]) => <article className="stat-card" key={label}><span>{label}</span><strong>{value}</strong></article>)}</section>
    <div className="dashboard-grid"><section className="panel"><div className="panel-heading"><h2>Phân bố theo độ khó</h2><Link to="/cau-hoi">Xem ngân hàng</Link></div><div className="difficulty-list">{data.difficulty.map((item) => { const percent = data.totalQuestions ? Math.round(item.count * 100 / data.totalQuestions) : 0; return <div key={item.difficulty}><div><span>{difficultyLabel(item.difficulty)}</span><strong>{item.count} câu ({percent}%)</strong></div><div className="progress"><span style={{ width: `${percent}%` }} /></div></div> })}</div></section>
      <section className="panel"><div className="panel-heading"><h2>Ma trận cập nhật gần đây</h2><Link to="/ma-tran">Xem tất cả</Link></div>{data.recentMatrices.length ? <div className="compact-list">{data.recentMatrices.map((item) => <Link to={`/ma-tran/${item.id}`} key={item.id}><strong>{item.title}</strong><span>{item.totalQuestions} câu · {item.duration} phút · {formatDate(item.updatedAt)}</span></Link>)}</div> : <p className="muted">Chưa có ma trận đề.</p>}</section>
    </div>
  </>
}
