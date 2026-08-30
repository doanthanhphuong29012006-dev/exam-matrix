import { useMemo, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { Link } from 'react-router-dom'
import { api } from '../api/service'
import { ConfirmModal, EmptyState, ErrorState, FieldError, LoadingState, Modal, PageHeader } from '../components/UI'
import { useToast } from '../components/ToastProvider'

function SubjectForm({ item, onClose }) {
  const queryClient = useQueryClient(); const { notify } = useToast()
  const { register, handleSubmit, setError, formState: { errors } } = useForm({ defaultValues: item || { code: '', name: '', description: '' } })
  const mutation = useMutation({ mutationFn: (body) => item ? api.updateSubject(item.id, body) : api.createSubject(body), onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['subjects'] }); queryClient.invalidateQueries({ queryKey: ['dashboard'] }); notify(item ? 'Đã cập nhật môn học' : 'Đã thêm môn học'); onClose() }, onError: (error) => { Object.entries(error.fieldErrors || {}).forEach(([name, message]) => setError(name, { message })); if (!error.fieldErrors || !Object.keys(error.fieldErrors).length) setError('root', { message: error.message }) } })
  return <Modal title={item ? 'Sửa môn học' : 'Thêm môn học'} onClose={onClose}><form onSubmit={handleSubmit((values) => mutation.mutate(values))} noValidate>{errors.root && <div className="alert error">{errors.root.message}</div>}<div className="form-grid two"><label>Mã môn học<input autoFocus maxLength="10" {...register('code', { required: 'Mã môn học là bắt buộc', maxLength: { value: 10, message: 'Tối đa 10 ký tự' } })} /></label><label>Tên môn học<input maxLength="50" {...register('name', { required: 'Tên môn học là bắt buộc', maxLength: { value: 50, message: 'Tối đa 50 ký tự' } })} /></label><FieldError>{errors.code?.message}</FieldError><FieldError>{errors.name?.message}</FieldError><label className="span-two">Mô tả<textarea rows="4" {...register('description')} /></label></div><div className="modal-actions"><button type="button" className="button secondary" onClick={onClose}>Hủy</button><button className="button" disabled={mutation.isPending}>{mutation.isPending ? 'Đang lưu...' : 'Lưu môn học'}</button></div></form></Modal>
}

export default function SubjectsPage() {
  const [search, setSearch] = useState(''); const [editing, setEditing] = useState(null); const [deleting, setDeleting] = useState(null); const { notify } = useToast(); const queryClient = useQueryClient()
  const query = useQuery({ queryKey: ['subjects'], queryFn: api.getSubjects })
  const deleteMutation = useMutation({ mutationFn: api.deleteSubject, onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['subjects'] }); notify('Đã xóa môn học'); setDeleting(null) }, onError: (error) => notify(error.message, 'error') })
  const items = useMemo(() => (query.data || []).filter((x) => `${x.code} ${x.name}`.toLocaleLowerCase('vi').includes(search.toLocaleLowerCase('vi'))), [query.data, search])
  return <><PageHeader title="Quản lý môn học" description="Tổ chức môn học và truy cập nhanh danh sách chương." action={<button className="button" onClick={() => setEditing({})}>Thêm môn học</button>} />
    <div className="toolbar"><label className="search-field">Tìm kiếm<input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Nhập mã hoặc tên môn học" /></label></div>
    {query.isLoading ? <LoadingState /> : query.isError ? <ErrorState error={query.error} onRetry={query.refetch} /> : !items.length ? <EmptyState /> : <div className="table-wrap"><table><thead><tr><th>Mã</th><th>Tên môn học</th><th>Mô tả</th><th>Thao tác</th></tr></thead><tbody>{items.map((item) => <tr key={item.id}><td><strong>{item.code}</strong></td><td>{item.name}</td><td className="long-cell">{item.description || '—'}</td><td><div className="table-actions"><Link className="text-button" to={`/chuong?subjectId=${item.id}`}>Xem chương</Link><button className="text-button" onClick={() => setEditing(item)}>Sửa</button><button className="text-button danger-text" onClick={() => setDeleting(item)}>Xóa</button></div></td></tr>)}</tbody></table></div>}
    {editing && <SubjectForm item={editing.id ? editing : null} onClose={() => setEditing(null)} />}{deleting && <ConfirmModal message={`Xóa môn học “${deleting.name}”? Dữ liệu đã xóa không thể khôi phục.`} onClose={() => setDeleting(null)} onConfirm={() => deleteMutation.mutate(deleting.id)} busy={deleteMutation.isPending} />}
  </>
}
