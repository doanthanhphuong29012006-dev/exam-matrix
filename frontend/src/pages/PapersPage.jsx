import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link, useParams, useSearchParams } from 'react-router-dom'
import { api, getAllMatrices } from '../api/service'
import { difficultyLabel, examTypeLabel, typeLabel } from '../constants'
import { formatDate } from '../utils'
import { Badge, ConfirmModal, EmptyState, ErrorState, LoadingState, PageHeader, Pagination } from '../components/UI'
import { useToast } from '../components/ToastProvider'

export default function PapersPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const matrixId = searchParams.get('matrixId') || ''
  const [page, setPage] = useState(0)
  const [deleting, setDeleting] = useState(null)
  const queryClient = useQueryClient()
  const { notify } = useToast()
  const matrices = useQuery({ queryKey: ['matrices', 'all'], queryFn: getAllMatrices })
  const papers = useQuery({ queryKey: ['papers', matrixId, page], queryFn: () => api.getPapers({ matrixId, page, size: 10 }) })
  const remove = useMutation({ mutationFn: api.deletePaper, onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['papers'] }); queryClient.invalidateQueries({ queryKey: ['dashboard'] }); notify('Đã xóa đề thi'); if (papers.data?.content.length === 1) setPage((current) => Math.max(0, current - 1)); setDeleting(null) }, onError: (error) => notify(error.message, 'error') })

  if (matrices.isLoading) return <LoadingState />
  if (matrices.isError) return <ErrorState error={matrices.error} onRetry={matrices.refetch} />

  return <>
    <PageHeader title="Quản lý đề thi" description="Tra cứu các mã đề trắc nghiệm và tự luận đã sinh từ ma trận." />
    <div className="toolbar"><label>Ma trận đề<select value={matrixId} onChange={(event) => { setSearchParams(event.target.value ? { matrixId: event.target.value } : {}); setPage(0) }}><option value="">Tất cả ma trận</option>{matrices.data.content.map((matrix) => <option key={matrix.id} value={matrix.id}>{matrix.title}</option>)}</select></label></div>
    {papers.isLoading ? <LoadingState /> : papers.isError ? <ErrorState error={papers.error} onRetry={papers.refetch} /> : !papers.data.content.length ? <EmptyState title="Chưa có đề thi" description="Mở chi tiết một ma trận hợp lệ để sinh đề." /> : <><div className="table-wrap"><table><thead><tr><th>Mã đề</th><th>Ma trận</th><th>Loại đề</th><th>Số câu</th><th>Ngày tạo</th><th>Thao tác</th></tr></thead><tbody>{papers.data.content.map((paper) => { const matrix = matrices.data.content.find((item) => item.id === paper.matrixId); return <tr key={paper.id}><td><strong>{paper.examCode}</strong></td><td>{matrix?.title || '—'}</td><td>{examTypeLabel(matrix?.examType)}</td><td>{paper.questions.length}</td><td>{formatDate(paper.createdAt)}</td><td><div className="table-actions"><Link className="text-button" to={`/de-thi/${paper.id}`}>Xem chi tiết</Link><button className="text-button danger-text" onClick={() => setDeleting(paper)}>Xóa</button></div></td></tr> })}</tbody></table></div><Pagination page={papers.data.page} totalPages={papers.data.totalPages} onChange={setPage} /></>}
    {deleting && <ConfirmModal message={`Xóa mã đề ${deleting.examCode}?`} onClose={() => setDeleting(null)} onConfirm={() => remove.mutate(deleting.id)} busy={remove.isPending} />}
  </>
}

export function PaperDetailPage() {
  const { id } = useParams()
  const [mode, setMode] = useState('exam')
  const paper = useQuery({ queryKey: ['paper', id], queryFn: () => api.getPaper(id) })
  const matrixQuery = useQuery({ queryKey: ['matrix', paper.data?.matrixId], queryFn: () => api.getMatrix(paper.data.matrixId), enabled: Boolean(paper.data?.matrixId) })
  const subjects = useQuery({ queryKey: ['subjects'], queryFn: api.getSubjects })

  if (paper.isLoading || matrixQuery.isLoading || subjects.isLoading) return <LoadingState />
  if (paper.isError || !paper.data) return <ErrorState error={paper.error || new Error('Không tìm thấy đề thi')} onRetry={paper.refetch} />
  if (matrixQuery.isError || subjects.isError) return <ErrorState error={matrixQuery.error || subjects.error} onRetry={() => { matrixQuery.refetch(); subjects.refetch() }} />
  if (!matrixQuery.data) return <LoadingState />

  const matrix = matrixQuery.data
  const examType = matrix.examType || 'OBJECTIVE'
  const isEssayPaper = examType === 'ESSAY'
  const subject = subjects.data.find((item) => item.id === matrix.subjectId)
  const answerModeLabel = isEssayPaper ? 'Đáp án tham khảo / Hướng dẫn chấm' : 'Đáp án'
  const documentTitle = mode === 'answer' && isEssayPaper ? 'ĐÁP ÁN THAM KHẢO / HƯỚNG DẪN CHẤM' : matrix.title

  return <>
    <div className="no-print">
      <PageHeader title={`Mã đề ${paper.data.examCode}`} description={`${matrix.title} · ${examTypeLabel(examType)}`} action={<div className="header-actions"><Link className="button secondary" to={`/de-thi?matrixId=${paper.data.matrixId}`}>Quay lại</Link><button className="button" onClick={() => window.print()}>In đề</button></div>} />
      <div className="mode-switch" role="group" aria-label="Chế độ hiển thị"><button className={mode === 'exam' ? 'active' : ''} onClick={() => setMode('exam')}>Bản đề thi</button><button className={mode === 'answer' ? 'active' : ''} onClick={() => setMode('answer')}>{answerModeLabel}</button></div>
    </div>
    <article className="paper-sheet">
      <header className="paper-header"><div><strong>ĐƠN VỊ: ....................................</strong><span>Môn: {subject?.name || '....................'}</span></div><div><h1>{documentTitle}</h1>{mode === 'answer' && isEssayPaper && <span className="paper-original-title">{matrix.title}</span>}<span>Thời gian: {matrix.duration || '...'} phút</span></div><div><strong>MÃ ĐỀ {paper.data.examCode}</strong><span>Ngày tạo: {formatDate(paper.data.createdAt)}</span></div></header>
      {mode === 'exam' && <div className="student-info">Họ và tên: ............................................................ &nbsp;&nbsp; Lớp: ....................</div>}
      <section className="paper-questions">{[...paper.data.questions].sort((first, second) => first.questionOrder - second.questionOrder).map((question) => <div className="paper-question" key={question.id}>
        <p><strong>Câu {question.questionOrder}.</strong> {question.content}</p>
        <div className="paper-meta no-print">{difficultyLabel(question.difficulty)} · {typeLabel(question.type)}</div>
        {question.type === 'ESSAY' ? mode === 'answer' && <section className="paper-reference-answer"><strong>Đáp án tham khảo / Hướng dẫn chấm</strong><p>{question.referenceAnswer || 'Chưa có đáp án tham khảo.'}</p></section> : <ol type="A">{(question.answers || []).map((answer) => <li key={answer.id} className={mode === 'answer' && answer.isCorrect ? 'paper-correct' : ''}>{answer.content}{mode === 'answer' && answer.isCorrect && <Badge tone="success">Đúng</Badge>}</li>)}</ol>}
      </div>)}</section>
      <footer>— HẾT —</footer>
    </article>
  </>
}
