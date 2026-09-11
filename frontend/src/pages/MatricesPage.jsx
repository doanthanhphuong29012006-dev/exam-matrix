import { useMemo, useState } from 'react'
import { useForm } from 'react-hook-form'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link, useParams } from 'react-router-dom'
import { api } from '../api/service'
import { DIFFICULTIES, EXAM_TYPES, difficultyLabel, examTypeLabel } from '../constants'
import { formatDate } from '../utils'
import { Badge, ConfirmModal, EmptyState, ErrorState, FieldError, LoadingState, Modal, PageHeader, Pagination } from '../components/UI'
import { useToast } from '../components/ToastProvider'

const keyOf = (chapterId, difficulty) => `${chapterId}-${difficulty}`

function ConfigTable({ chapters, availability, quantities, setQuantity }) {
  return <div className="table-wrap matrix-config"><table><thead><tr><th>Chương</th>{DIFFICULTIES.map((difficulty) => <th key={difficulty.value}>{difficulty.label}</th>)}<th>Tổng</th></tr></thead><tbody>{chapters.map((chapter) => {
    const rowTotal = DIFFICULTIES.reduce((sum, difficulty) => sum + Number(quantities[keyOf(chapter.id, difficulty.value)] || 0), 0)
    return <tr key={chapter.id}><td><strong>{chapter.orderIndex}. {chapter.name}</strong></td>{DIFFICULTIES.map((difficulty) => {
      const key = keyOf(chapter.id, difficulty.value)
      const available = availability.find((item) => item.chapterId === chapter.id && item.difficulty === difficulty.value)?.available || 0
      const value = Number(quantities[key] || 0)
      return <td key={difficulty.value}><input aria-label={`${chapter.name} - ${difficulty.label}`} className={value > available ? 'invalid-input' : ''} type="number" min="0" step="1" value={value} onChange={(event) => setQuantity(key, Math.max(0, Math.floor(Number(event.target.value) || 0)))} /><small className={value > available ? 'danger-text' : ''}>Có {available}{value > available ? ` · thiếu ${value - available}` : ''}</small></td>
    })}<td><strong>{rowTotal}</strong></td></tr>
  })}</tbody></table></div>
}

function MatrixForm({ item, subjects, allChapters, onClose }) {
  const queryClient = useQueryClient()
  const { notify } = useToast()
  const initialSubject = item?.subjectId || ''
  const [subjectId, setSubjectId] = useState(initialSubject)
  const [examType, setExamType] = useState(item?.examType || 'OBJECTIVE')
  const [quantities, setQuantities] = useState(() => Object.fromEntries((item?.configs || []).map((config) => [keyOf(config.chapterId, config.difficulty), config.quantity])))
  const [configError, setConfigError] = useState('')
  const { register, handleSubmit, setError, formState: { errors } } = useForm({ defaultValues: item || { title: '', duration: 45, totalQuestions: 20 } })
  const availability = useQuery({ queryKey: ['availability', subjectId, examType], queryFn: () => api.getAvailability({ subjectId, examType }), enabled: Boolean(subjectId) })
  const chapters = useMemo(() => allChapters.filter((chapter) => chapter.subjectId === Number(subjectId)), [allChapters, subjectId])
  const totalConfigured = Object.values(quantities).reduce((sum, value) => sum + Number(value || 0), 0)

  const mutation = useMutation({
    mutationFn: (body) => item ? api.updateMatrix(item.id, body) : api.createMatrix(body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['matrices'] })
      queryClient.invalidateQueries({ queryKey: ['matrix'] })
      queryClient.invalidateQueries({ queryKey: ['dashboard'] })
      notify(item ? 'Đã cập nhật ma trận' : 'Đã thêm ma trận')
      onClose()
    },
    onError: (error) => setError('root', { message: error.message || error.response?.data?.message || 'Không thể lưu ma trận' }),
  })

  const resetConfiguration = () => {
    setQuantities({})
    setConfigError('')
  }

  const submit = (values) => {
    setConfigError('')
    if (!subjectId) return setError('subjectId', { message: 'Vui lòng chọn môn học' })
    if (!availability.isSuccess || availability.isFetching) return setConfigError('Chưa thể xác nhận số câu hỏi khả dụng. Vui lòng tải lại trước khi lưu.')
    const total = Number(values.totalQuestions)
    if (totalConfigured !== total) return setConfigError(`Tổng cấu hình hiện là ${totalConfigured}, phải bằng ${total} câu.`)
    const over = availability.data.find((item) => Number(quantities[keyOf(item.chapterId, item.difficulty)] || 0) > item.available)
    if (over) return setConfigError('Cấu hình đang yêu cầu nhiều hơn số câu hỏi khả dụng.')
    const configs = Object.entries(quantities).filter(([, quantity]) => Number(quantity) > 0).map(([key, quantity]) => {
      const [chapterId, difficulty] = key.split('-')
      return { chapterId: Number(chapterId), difficulty, quantity: Number(quantity) }
    })
    mutation.mutate({ subjectId: Number(subjectId), examType, title: values.title.trim(), duration: Number(values.duration), totalQuestions: total, configs })
  }

  return <Modal title={item ? 'Sửa ma trận đề' : 'Thêm ma trận đề'} onClose={onClose} wide>
    <form onSubmit={handleSubmit(submit)}>
      {errors.root && <div className="alert error">{errors.root.message}</div>}
      <div className="form-grid two">
        <label>Tiêu đề<input maxLength="100" {...register('title', { required: 'Tiêu đề là bắt buộc', maxLength: { value: 100, message: 'Tối đa 100 ký tự' } })} /></label>
        <label>Môn học<select value={subjectId} onChange={(event) => { setSubjectId(event.target.value); resetConfiguration() }} disabled={Boolean(item)}><option value="">Chọn môn học</option>{subjects.map((subject) => <option key={subject.id} value={subject.id}>{subject.code} - {subject.name}</option>)}</select></label>
        <FieldError>{errors.title?.message}</FieldError>
        <FieldError>{errors.subjectId?.message}</FieldError>
        <label>Loại đề<select value={examType} onChange={(event) => { setExamType(event.target.value); resetConfiguration() }} disabled={Boolean(item)}>{EXAM_TYPES.map((type) => <option key={type.value} value={type.value}>{type.label}</option>)}</select></label>
        <label>Thời gian (phút)<input type="number" min="1" {...register('duration', { valueAsNumber: true, required: 'Thời gian là bắt buộc', min: { value: 1, message: 'Thời gian phải lớn hơn 0' } })} /></label>
        <span />
        <FieldError>{errors.duration?.message}</FieldError>
        <label>Tổng số câu<input type="number" min="1" {...register('totalQuestions', { valueAsNumber: true, required: 'Tổng số câu là bắt buộc', min: { value: 1, message: 'Tổng số câu phải lớn hơn 0' } })} /></label>
        <span />
        <FieldError>{errors.totalQuestions?.message}</FieldError>
      </div>
      <div className="section-heading"><div><h3>Cấu hình câu {examType === 'ESSAY' ? 'tự luận' : 'trắc nghiệm'}</h3><p>Nhập số câu cho từng chương và mức độ. Tổng hiện tại: <strong>{totalConfigured}</strong></p></div></div>
      {!subjectId ? <div className="state-box">Chọn môn học để cấu hình.</div> : availability.isLoading ? <LoadingState label="Đang kiểm tra số câu khả dụng..." /> : availability.isError ? <ErrorState error={availability.error} onRetry={availability.refetch} /> : !chapters.length ? <EmptyState title="Môn học chưa có chương" /> : <ConfigTable chapters={chapters} availability={availability.data} quantities={quantities} setQuantity={(key, value) => setQuantities((current) => ({ ...current, [key]: value }))} />}
      <FieldError>{configError}</FieldError>
      <div className="modal-actions"><button type="button" className="button secondary" onClick={onClose}>Hủy</button><button className="button" disabled={mutation.isPending || !subjectId || !availability.isSuccess || availability.isFetching}>{mutation.isPending ? 'Đang lưu...' : 'Lưu ma trận'}</button></div>
    </form>
  </Modal>
}

export default function MatricesPage() {
  const [filters, setFilters] = useState({ search: '', subjectId: '', examType: '', page: 0, size: 10 })
  const [editing, setEditing] = useState(null)
  const [deleting, setDeleting] = useState(null)
  const queryClient = useQueryClient()
  const { notify } = useToast()
  const subjects = useQuery({ queryKey: ['subjects'], queryFn: api.getSubjects })
  const chapters = useQuery({ queryKey: ['chapters', 'all'], queryFn: () => api.getChapters({}) })
  const matrices = useQuery({ queryKey: ['matrices', filters], queryFn: () => api.getMatrices(filters) })
  const deleteMutation = useMutation({ mutationFn: api.deleteMatrix, onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['matrices'] }); queryClient.invalidateQueries({ queryKey: ['dashboard'] }); notify('Đã xóa ma trận'); if (matrices.data?.content.length === 1) setFilters((current) => ({ ...current, page: Math.max(0, current.page - 1) })); setDeleting(null) }, onError: (error) => notify(error.message, 'error') })
  const setFilter = (name, value) => setFilters((current) => ({ ...current, [name]: value, page: name === 'page' ? value : 0 }))

  if (subjects.isLoading || chapters.isLoading) return <LoadingState />
  if (subjects.isError || chapters.isError) return <ErrorState error={subjects.error || chapters.error} onRetry={() => { subjects.refetch(); chapters.refetch() }} />

  return <>
    <PageHeader title="Quản lý ma trận đề" description="Thiết lập riêng ma trận trắc nghiệm hoặc tự luận theo chương và độ khó." action={<button className="button" onClick={() => setEditing({})}>Thêm ma trận</button>} />
    <div className="toolbar multi"><label className="search-field">Tìm kiếm<input value={filters.search} onChange={(event) => setFilter('search', event.target.value)} placeholder="Nhập tiêu đề ma trận" /></label><label>Môn học<select value={filters.subjectId} onChange={(event) => setFilter('subjectId', event.target.value)}><option value="">Tất cả môn học</option>{subjects.data.map((subject) => <option key={subject.id} value={subject.id}>{subject.name}</option>)}</select></label><label>Loại đề<select value={filters.examType} onChange={(event) => setFilter('examType', event.target.value)}><option value="">Tất cả</option>{EXAM_TYPES.map((type) => <option key={type.value} value={type.value}>{type.label}</option>)}</select></label></div>
    {matrices.isLoading ? <LoadingState /> : matrices.isError ? <ErrorState error={matrices.error} onRetry={matrices.refetch} /> : !matrices.data.content.length ? <EmptyState title="Chưa có ma trận đề" /> : <><div className="table-wrap"><table><thead><tr><th>Tiêu đề</th><th>Loại đề</th><th>Môn học</th><th>Thời gian</th><th>Số câu</th><th>Cập nhật</th><th>Thao tác</th></tr></thead><tbody>{matrices.data.content.map((item) => <tr key={item.id}><td><strong>{item.title}</strong></td><td><Badge>{examTypeLabel(item.examType)}</Badge></td><td>{subjects.data.find((subject) => subject.id === item.subjectId)?.name || '—'}</td><td>{item.duration} phút</td><td>{item.totalQuestions}</td><td>{formatDate(item.updatedAt)}</td><td><div className="table-actions"><Link className="text-button" to={`/ma-tran/${item.id}`}>Xem chi tiết</Link><button className="text-button" onClick={() => setEditing(item)}>Sửa</button><button className="text-button danger-text" onClick={() => setDeleting(item)}>Xóa</button></div></td></tr>)}</tbody></table></div><Pagination page={matrices.data.page} totalPages={matrices.data.totalPages} onChange={(page) => setFilter('page', page)} /></>}
    {editing && <MatrixForm item={editing.id ? editing : null} subjects={subjects.data} allChapters={chapters.data} onClose={() => setEditing(null)} />}
    {deleting && <ConfirmModal message={`Xóa ma trận “${deleting.title}”?`} onClose={() => setDeleting(null)} onConfirm={() => deleteMutation.mutate(deleting.id)} busy={deleteMutation.isPending} />}
  </>
}

export function MatrixDetailPage() {
  const { id } = useParams()
  const { notify } = useToast()
  const queryClient = useQueryClient()
  const [number, setNumber] = useState(1)
  const matrix = useQuery({ queryKey: ['matrix', id], queryFn: () => api.getMatrix(id) })
  const examType = matrix.data?.examType || 'OBJECTIVE'
  const subjects = useQuery({ queryKey: ['subjects'], queryFn: api.getSubjects })
  const chapters = useQuery({ queryKey: ['chapters', 'all'], queryFn: () => api.getChapters({}) })
  const availability = useQuery({ queryKey: ['availability', matrix.data?.subjectId, examType], queryFn: () => api.getAvailability({ subjectId: matrix.data.subjectId, examType }), enabled: Boolean(matrix.data) })
  const papers = useQuery({ queryKey: ['papers', id], queryFn: () => api.getPapers({ matrixId: id, page: 0, size: 100 }) })
  const generate = useMutation({ mutationFn: () => api.generatePapers(id, { numberOfPapers: Number(number) }), onSuccess: (items) => { queryClient.invalidateQueries({ queryKey: ['papers'] }); queryClient.invalidateQueries({ queryKey: ['dashboard'] }); notify(`Đã sinh ${items.length} mã đề`) }, onError: (error) => notify(error.message || error.response?.data?.message || 'Không thể sinh đề', 'error') })

  if (matrix.isLoading || subjects.isLoading || chapters.isLoading || availability.isLoading) return <LoadingState />
  if (matrix.isError || !matrix.data) return <ErrorState error={matrix.error || new Error('Không tìm thấy ma trận')} onRetry={matrix.refetch} />
  if (subjects.isError || chapters.isError || availability.isError) return <ErrorState error={subjects.error || chapters.error || availability.error} onRetry={() => { subjects.refetch(); chapters.refetch(); availability.refetch() }} />

  const subject = subjects.data.find((item) => item.id === matrix.data.subjectId)
  const invalid = matrix.data.configs.some((config) => config.quantity > (availability.data.find((item) => item.chapterId === config.chapterId && item.difficulty === config.difficulty)?.available || 0))
  const total = matrix.data.configs.reduce((sum, config) => sum + config.quantity, 0)
  const canGenerate = availability.isSuccess && !availability.isFetching && !invalid && total === matrix.data.totalQuestions && Number.isInteger(Number(number)) && Number(number) >= 1 && Number(number) <= 20

  return <>
    <PageHeader title={matrix.data.title} description={`${subject?.name || ''} · ${examTypeLabel(examType)} · Cập nhật ${formatDate(matrix.data.updatedAt)}`} action={<Link className="button secondary" to="/ma-tran">Quay lại</Link>} />
    <section className="detail-summary"><div><span>Loại đề</span><strong>{examTypeLabel(examType)}</strong></div><div><span>Thời gian</span><strong>{matrix.data.duration} phút</strong></div><div><span>Tổng số câu</span><strong>{matrix.data.totalQuestions}</strong></div><div><span>Đề đã sinh</span><strong>{papers.data?.totalElements || 0}</strong></div></section>
    <section className="panel"><div className="panel-heading"><h2>Cấu hình ma trận</h2>{canGenerate ? <Badge tone="success">Hợp lệ</Badge> : <Badge tone="danger">Chưa hợp lệ</Badge>}</div><div className="table-wrap"><table><thead><tr><th>Chương</th><th>Độ khó</th><th>Số lượng</th><th>Khả dụng</th><th>Trạng thái</th></tr></thead><tbody>{matrix.data.configs.map((config) => { const available = availability.data.find((item) => item.chapterId === config.chapterId && item.difficulty === config.difficulty)?.available || 0; return <tr key={keyOf(config.chapterId, config.difficulty)}><td>{chapters.data.find((chapter) => chapter.id === config.chapterId)?.name}</td><td>{difficultyLabel(config.difficulty)}</td><td>{config.quantity}</td><td>{available}</td><td>{config.quantity <= available ? <Badge tone="success">Đủ câu hỏi</Badge> : <Badge tone="danger">Thiếu {config.quantity - available} câu</Badge>}</td></tr> })}</tbody></table></div>{total !== matrix.data.totalQuestions && <div className="alert error">Tổng cấu hình {total} không bằng tổng số câu {matrix.data.totalQuestions}.</div>}</section>
    <section className="panel generate-panel"><div><h2>Sinh đề {examType === 'ESSAY' ? 'tự luận' : 'trắc nghiệm'}</h2><p>Chỉ sử dụng câu hỏi phù hợp với loại ma trận và số lượng đã cấu hình.</p></div><label>Số mã đề<input type="number" min="1" max="20" value={number} onChange={(event) => setNumber(Math.max(1, Math.min(20, Math.floor(Number(event.target.value)) || 1)))} /></label><button className="button" disabled={!canGenerate || generate.isPending} onClick={() => generate.mutate()}>{generate.isPending ? 'Đang sinh đề...' : 'Sinh đề'}</button></section>
    <section className="panel"><div className="panel-heading"><h2>Danh sách đề đã sinh</h2><Link to={`/de-thi?matrixId=${id}`}>Xem tất cả</Link></div>{papers.isLoading ? <LoadingState /> : papers.isError ? <ErrorState error={papers.error} onRetry={papers.refetch} /> : papers.data?.content.length ? <div className="compact-list">{papers.data.content.slice(0, 5).map((paper) => <Link to={`/de-thi/${paper.id}`} key={paper.id}><strong>Mã đề {paper.examCode}</strong><span>{paper.questions.length} câu · {formatDate(paper.createdAt)}</span></Link>)}</div> : <EmptyState title="Chưa sinh đề" description="Nhập số lượng mã đề và chọn Sinh đề." />}</section>
  </>
}
