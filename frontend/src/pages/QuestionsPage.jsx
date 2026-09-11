import { useMemo, useState } from 'react'
import { useFieldArray, useForm } from 'react-hook-form'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from '../api/service'
import { useAuth } from '../auth/AuthContext'
import { DIFFICULTIES, QUESTION_TYPES, difficultyLabel, typeLabel } from '../constants'
import { Badge, ConfirmModal, EmptyState, ErrorState, FieldError, LoadingState, Modal, PageHeader, Pagination } from '../components/UI'
import { useToast } from '../components/ToastProvider'

const blankAnswers = [{ content: '', isCorrect: true }, { content: '', isCorrect: false }]

function QuestionForm({ item, subjects, allChapters, onClose }) {
  const queryClient = useQueryClient()
  const { notify } = useToast()
  const [answerError, setAnswerError] = useState('')
  const initialSubjectId = item ? allChapters.find((chapter) => chapter.id === Number(item.chapterId))?.subjectId || '' : ''
  const defaultValues = item
    ? { ...structuredClone(item), subjectId: initialSubjectId, answers: Array.isArray(item.answers) ? structuredClone(item.answers) : [], referenceAnswer: item.referenceAnswer ?? '' }
    : { subjectId: '', chapterId: '', content: '', difficulty: 'MEDIUM', type: 'SINGLE_CHOICE', answers: structuredClone(blankAnswers), referenceAnswer: '' }
  const { register, control, watch, handleSubmit, setError, setValue, clearErrors, formState: { errors } } = useForm({ defaultValues })
  const { fields, append, remove, replace } = useFieldArray({ control, name: 'answers' })
  const type = watch('type')
  const subjectId = watch('subjectId')
  const answers = watch('answers') || []
  const chapters = useMemo(() => allChapters.filter((chapter) => chapter.subjectId === Number(subjectId)), [allChapters, subjectId])

  const mutation = useMutation({
    mutationFn: (body) => item ? api.updateQuestion(item.id, body) : api.createQuestion(body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['questions'] })
      queryClient.invalidateQueries({ queryKey: ['availability'] })
      queryClient.invalidateQueries({ queryKey: ['dashboard'] })
      notify(item ? 'Đã cập nhật câu hỏi' : 'Đã thêm câu hỏi')
      onClose()
    },
    onError: (error) => {
      const fieldErrors = error.fieldErrors || error.response?.data?.fieldErrors || {}
      Object.entries(fieldErrors).forEach(([name, message]) => setError(name, { message }))
      if (!Object.keys(fieldErrors).length) setError('root', { message: error.message || error.response?.data?.message || 'Không thể lưu câu hỏi' })
    },
  })

  const handleTypeChange = (nextType) => {
    setAnswerError('')
    clearErrors(['answers', 'referenceAnswer'])
    if (nextType === 'ESSAY') replace([])
    else if (nextType === 'TRUE_FALSE') replace([{ content: 'Đúng', isCorrect: true }, { content: 'Sai', isCorrect: false }])
    else if (type === 'ESSAY') replace(structuredClone(blankAnswers))
  }

  const submit = (values) => {
    setAnswerError('')
    if (values.type === 'ESSAY') {
      mutation.mutate({ chapterId: Number(values.chapterId), content: values.content.trim(), difficulty: values.difficulty, type: values.type, answers: [], referenceAnswer: values.referenceAnswer.trim() })
      return
    }
    const objectiveAnswers = values.answers || []
    const correct = objectiveAnswers.filter((answer) => answer.isCorrect).length
    if (objectiveAnswers.length < 2) return setAnswerError('Câu hỏi cần ít nhất hai đáp án')
    if (values.type !== 'MULTIPLE_CHOICE' && correct !== 1) return setAnswerError('Loại câu hỏi này phải có đúng một đáp án đúng')
    if (values.type === 'MULTIPLE_CHOICE' && correct < 1) return setAnswerError('Phải chọn ít nhất một đáp án đúng')
    mutation.mutate({ chapterId: Number(values.chapterId), content: values.content.trim(), difficulty: values.difficulty, type: values.type, answers: objectiveAnswers.map(({ content, isCorrect }) => ({ content: content.trim(), isCorrect: Boolean(isCorrect) })), referenceAnswer: null })
  }

  const toggleCorrect = (index) => {
    if (type !== 'MULTIPLE_CHOICE') answers.forEach((_, answerIndex) => setValue(`answers.${answerIndex}.isCorrect`, answerIndex === index))
  }

  return <Modal title={item ? 'Sửa câu hỏi' : 'Thêm câu hỏi'} onClose={onClose} wide>
    <form onSubmit={handleSubmit(submit)} noValidate>
      {errors.root && <div className="alert error">{errors.root.message}</div>}
      <div className="form-grid two">
        <label>Môn học<select value={subjectId} onChange={(event) => { setValue('subjectId', event.target.value); setValue('chapterId', '') }}><option value="">Chọn môn học</option>{subjects.map((subject) => <option key={subject.id} value={subject.id}>{subject.code} - {subject.name}</option>)}</select></label>
        <label>Chương<select {...register('chapterId', { required: 'Vui lòng chọn chương' })}><option value="">Chọn chương</option>{chapters.map((chapter) => <option key={chapter.id} value={chapter.id}>{chapter.orderIndex}. {chapter.name}</option>)}</select></label>
        <span />
        <FieldError>{errors.chapterId?.message}</FieldError>
        <label>Độ khó<select {...register('difficulty')}>{DIFFICULTIES.map((difficulty) => <option key={difficulty.value} value={difficulty.value}>{difficulty.label}</option>)}</select></label>
        <label>Loại câu hỏi<select {...register('type', { onChange: (event) => handleTypeChange(event.target.value) })}>{QUESTION_TYPES.map((questionType) => <option key={questionType.value} value={questionType.value}>{questionType.label}</option>)}</select></label>
        <label className="span-two">Nội dung câu hỏi<textarea rows="4" maxLength="3000" {...register('content', { required: 'Nội dung câu hỏi là bắt buộc', validate: (value) => Boolean(value.trim()) || 'Nội dung câu hỏi không được chỉ chứa khoảng trắng', maxLength: { value: 3000, message: 'Nội dung câu hỏi tối đa 3000 ký tự' } })} /></label>
        <FieldError>{errors.content?.message}</FieldError>
      </div>

      {type === 'ESSAY' ? <div className="essay-answer-field">
        <label>Đáp án tham khảo / Hướng dẫn chấm<textarea rows="8" maxLength="10000" {...register('referenceAnswer', { required: 'Đáp án tham khảo là bắt buộc', validate: (value) => Boolean(value?.trim()) || 'Đáp án tham khảo không được chỉ chứa khoảng trắng', maxLength: { value: 10000, message: 'Đáp án tham khảo tối đa 10000 ký tự' } })} /></label>
        <FieldError>{errors.referenceAnswer?.message}</FieldError>
        <p className="form-help">Tối đa 10000 ký tự. Nội dung được lưu dưới dạng văn bản thuần.</p>
      </div> : <fieldset className="answer-fieldset">
        <legend>Danh sách đáp án</legend>
        {fields.map((field, index) => <div className="answer-row" key={field.id}>
          <label><input type={type === 'MULTIPLE_CHOICE' ? 'checkbox' : 'radio'} name="correct-answer" checked={Boolean(answers[index]?.isCorrect)} onChange={(event) => type === 'MULTIPLE_CHOICE' ? setValue(`answers.${index}.isCorrect`, event.target.checked) : toggleCorrect(index)} /> Đáp án đúng</label>
          <div><input aria-label={`Nội dung đáp án ${index + 1}`} readOnly={type === 'TRUE_FALSE'} {...register(`answers.${index}.content`, { required: 'Không được để trống đáp án', validate: (value) => Boolean(value.trim()) || 'Đáp án không được chỉ chứa khoảng trắng' })} /><FieldError>{errors.answers?.[index]?.content?.message}</FieldError></div>
          {type !== 'TRUE_FALSE' && fields.length > 2 && <button type="button" className="text-button danger-text" onClick={() => remove(index)}>Xóa dòng</button>}
        </div>)}
        {type !== 'TRUE_FALSE' && <button type="button" className="button secondary small" onClick={() => append({ content: '', isCorrect: false })}>Thêm dòng đáp án</button>}
        <FieldError>{answerError}</FieldError>
      </fieldset>}

      <div className="modal-actions"><button type="button" className="button secondary" onClick={onClose}>Hủy</button><button className="button" disabled={mutation.isPending}>{mutation.isPending ? 'Đang lưu...' : 'Lưu câu hỏi'}</button></div>
    </form>
  </Modal>
}

function QuestionDetail({ item, chapter, subject, onClose }) {
  const isEssay = item.type === 'ESSAY'
  return <Modal title="Chi tiết câu hỏi" onClose={onClose} wide>
    <div className="detail-meta"><span>Môn học<strong>{subject?.name}</strong></span><span>Chương<strong>{chapter?.name}</strong></span><span>Độ khó<strong>{difficultyLabel(item.difficulty)}</strong></span><span>Loại<strong>{typeLabel(item.type)}</strong></span></div>
    <section className="question-detail-section"><h3>Nội dung câu hỏi</h3><div className="question-content">{item.content}</div></section>
    {isEssay ? <section className="question-detail-section"><h3>Đáp án tham khảo / Hướng dẫn chấm</h3><div className="reference-answer-content">{item.referenceAnswer || 'Chưa có đáp án tham khảo.'}</div></section> : <ol className="answer-detail">{(item.answers || []).map((answer) => <li key={answer.id || answer.content} className={answer.isCorrect ? 'correct-answer' : ''}>{answer.content}{answer.isCorrect && <Badge tone="success">Đáp án đúng</Badge>}</li>)}</ol>}
  </Modal>
}

export default function QuestionsPage() {
  const { user } = useAuth()
  const isAdmin = user?.role === 'ADMIN'
  const [filters, setFilters] = useState({ search: '', subjectId: '', chapterId: '', difficulty: '', type: '', page: 0, size: 8 })
  const [editing, setEditing] = useState(null)
  const [viewing, setViewing] = useState(null)
  const [deleting, setDeleting] = useState(null)
  const { notify } = useToast()
  const queryClient = useQueryClient()
  const subjects = useQuery({ queryKey: ['subjects'], queryFn: api.getSubjects })
  const chapters = useQuery({ queryKey: ['chapters', 'all'], queryFn: () => api.getChapters({}) })
  const questions = useQuery({ queryKey: ['questions', filters], queryFn: () => api.getQuestions(filters) })
  const deleteMutation = useMutation({ mutationFn: api.deleteQuestion, onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['questions'] }); queryClient.invalidateQueries({ queryKey: ['availability'] }); queryClient.invalidateQueries({ queryKey: ['dashboard'] }); notify('Đã xóa câu hỏi'); if (questions.data?.content.length === 1) setFilters((current) => ({ ...current, page: Math.max(0, current.page - 1) })); setDeleting(null) }, onError: (error) => notify(error.message, 'error') })
  const setFilter = (name, value) => setFilters((previous) => ({ ...previous, [name]: value, page: name === 'page' ? value : 0, ...(name === 'subjectId' ? { chapterId: '' } : {}) }))
  const filteredChapters = (chapters.data || []).filter((chapter) => !filters.subjectId || chapter.subjectId === Number(filters.subjectId))

  if (subjects.isLoading || chapters.isLoading) return <LoadingState />
  if (subjects.isError || chapters.isError) return <ErrorState error={subjects.error || chapters.error} onRetry={() => { subjects.refetch(); chapters.refetch() }} />

  return <>
    <PageHeader title="Ngân hàng câu hỏi" description="Tìm kiếm, phân loại và quản lý câu hỏi trắc nghiệm hoặc tự luận." action={<button className="button" onClick={() => setEditing({})}>Thêm câu hỏi</button>} />
    <div className="toolbar multi"><label className="search-field">Tìm nội dung<input value={filters.search} onChange={(event) => setFilter('search', event.target.value)} placeholder="Nhập nội dung câu hỏi" /></label><label>Môn học<select value={filters.subjectId} onChange={(event) => setFilter('subjectId', event.target.value)}><option value="">Tất cả</option>{subjects.data.map((subject) => <option key={subject.id} value={subject.id}>{subject.name}</option>)}</select></label><label>Chương<select value={filters.chapterId} onChange={(event) => setFilter('chapterId', event.target.value)}><option value="">Tất cả</option>{filteredChapters.map((chapter) => <option key={chapter.id} value={chapter.id}>{chapter.name}</option>)}</select></label><label>Độ khó<select value={filters.difficulty} onChange={(event) => setFilter('difficulty', event.target.value)}><option value="">Tất cả</option>{DIFFICULTIES.map((difficulty) => <option key={difficulty.value} value={difficulty.value}>{difficulty.label}</option>)}</select></label><label>Loại<select value={filters.type} onChange={(event) => setFilter('type', event.target.value)}><option value="">Tất cả</option>{QUESTION_TYPES.map((questionType) => <option key={questionType.value} value={questionType.value}>{questionType.label}</option>)}</select></label></div>
    {questions.isLoading ? <LoadingState /> : questions.isError ? <ErrorState error={questions.error} onRetry={questions.refetch} /> : !questions.data.content.length ? <EmptyState /> : <><div className="table-wrap"><table><thead><tr><th>Nội dung</th><th>Chương</th><th>Độ khó</th><th>Loại</th><th>Thao tác</th></tr></thead><tbody>{questions.data.content.map((item) => { const chapter = chapters.data.find((candidate) => candidate.id === item.chapterId); return <tr key={item.id}><td className="question-cell">{item.content}</td><td>{chapter?.name || '—'}</td><td><Badge tone={item.difficulty === 'HARD' ? 'danger' : item.difficulty === 'MEDIUM' ? 'warning' : 'success'}>{difficultyLabel(item.difficulty)}</Badge></td><td>{typeLabel(item.type)}</td><td><div className="table-actions"><button className="text-button" onClick={() => setViewing(item)}>Xem</button>{(isAdmin || item.teacherId === user?.id) && <><button className="text-button" onClick={() => setEditing(item)}>Sửa</button><button className="text-button danger-text" onClick={() => setDeleting(item)}>Xóa</button></>}</div></td></tr> })}</tbody></table></div><Pagination page={questions.data.page} totalPages={questions.data.totalPages} onChange={(page) => setFilter('page', page)} /></>}
    {editing && <QuestionForm item={editing.id ? editing : null} subjects={subjects.data} allChapters={chapters.data} onClose={() => setEditing(null)} />}
    {viewing && (() => { const chapter = chapters.data.find((candidate) => candidate.id === viewing.chapterId); return <QuestionDetail item={viewing} chapter={chapter} subject={subjects.data.find((subject) => subject.id === chapter?.subjectId)} onClose={() => setViewing(null)} /> })()}
    {deleting && <ConfirmModal message="Xóa câu hỏi này khỏi ngân hàng?" onClose={() => setDeleting(null)} onConfirm={() => deleteMutation.mutate(deleting.id)} busy={deleteMutation.isPending} />}
  </>
}
