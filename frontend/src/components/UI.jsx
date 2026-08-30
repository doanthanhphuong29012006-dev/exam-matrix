import { useEffect, useRef } from 'react'

export function PageHeader({ title, description, action }) {
  return <div className="page-header"><div><h1>{title}</h1>{description && <p>{description}</p>}</div>{action}</div>
}

export function LoadingState({ label = 'Đang tải dữ liệu...' }) {
  return <div className="state-box" role="status"><span className="loader" />{label}</div>
}

export function EmptyState({ title = 'Chưa có dữ liệu', description = 'Không có bản ghi phù hợp với điều kiện hiện tại.' }) {
  return <div className="state-box"><strong>{title}</strong><span>{description}</span></div>
}

export function ErrorState({ error, onRetry }) {
  return <div className="state-box state-error" role="alert"><strong>Không thể tải dữ liệu</strong><span>{error?.message || 'Đã có lỗi xảy ra.'}</span>{onRetry && <button className="button secondary" onClick={onRetry}>Thử lại</button>}</div>
}

export function Badge({ children, tone = 'neutral' }) {
  return <span className={`badge badge-${tone}`}>{children}</span>
}

export function Modal({ title, children, onClose, wide = false }) {
  const dialogRef = useRef(null)
  useEffect(() => {
    const previous = document.activeElement
    const handle = (event) => { if (event.key === 'Escape') onClose() }
    document.addEventListener('keydown', handle)
    dialogRef.current?.focus()
    return () => { document.removeEventListener('keydown', handle); previous?.focus?.() }
  }, [onClose])
  return <div className="modal-backdrop" onMouseDown={(event) => event.target === event.currentTarget && onClose()}><section ref={dialogRef} tabIndex="-1" role="dialog" aria-modal="true" aria-labelledby="modal-title" className={`modal ${wide ? 'modal-wide' : ''}`}><div className="modal-header"><h2 id="modal-title">{title}</h2><button type="button" className="text-button" onClick={onClose} aria-label="Đóng hộp thoại">Đóng</button></div>{children}</section></div>
}

export function ConfirmModal({ title = 'Xác nhận thao tác', message, onConfirm, onClose, busy = false, confirmLabel = 'Xóa' }) {
  return <Modal title={title} onClose={onClose}><p>{message}</p><div className="modal-actions"><button type="button" className="button secondary" onClick={onClose}>Hủy</button><button type="button" className="button danger" disabled={busy} onClick={onConfirm}>{busy ? 'Đang xử lý...' : confirmLabel}</button></div></Modal>
}

export function Pagination({ page, totalPages, onChange }) {
  if (totalPages <= 1) return null
  return <nav className="pagination" aria-label="Phân trang"><button className="button secondary small" disabled={page <= 0} onClick={() => onChange(page - 1)}>Trang trước</button><span>Trang {page + 1} / {totalPages}</span><button className="button secondary small" disabled={page >= totalPages - 1} onClick={() => onChange(page + 1)}>Trang sau</button></nav>
}

export const FieldError = ({ children }) => children ? <span className="field-error">{children}</span> : null
