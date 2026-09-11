import { seedData } from './seed'
import { newId } from '../utils'

const STORAGE_KEY = 'exam_matrix_mock_db_v1'
const CURRENT_SCHEMA_VERSION = 3
const wait = (value, delay = 220) => new Promise((resolve, reject) => setTimeout(() => {
  if (sessionStorage.getItem('exam_mock_force_error') === 'true') {
    sessionStorage.removeItem('exam_mock_force_error')
    reject(new Error('Lỗi mô phỏng. Vui lòng thử lại.'))
  } else resolve(structuredClone(value))
}, delay))

const read = () => {
  const saved = localStorage.getItem(STORAGE_KEY)
  if (saved) {
    const current = JSON.parse(saved)
    if (Number(current.schemaVersion || 1) < CURRENT_SCHEMA_VERSION) {
      const essayQuestions = seedData.questions.filter((item) => item.type === 'ESSAY' && !(current.questions || []).some((question) => question.id === item.id))
      const essayMatrices = seedData.matrices.filter((item) => item.examType === 'ESSAY' && !(current.matrices || []).some((matrix) => matrix.id === item.id))
      const upgraded = {
        ...current,
        schemaVersion: CURRENT_SCHEMA_VERSION,
        questions: [...(current.questions || []).map((item) => ({ ...item, answers: Array.isArray(item.answers) ? item.answers : [], referenceAnswer: item.referenceAnswer ?? null })), ...structuredClone(essayQuestions)],
        matrices: [...(current.matrices || []).map((item) => ({ ...item, examType: item.examType || 'OBJECTIVE' })), ...structuredClone(essayMatrices)],
      }
      localStorage.setItem(STORAGE_KEY, JSON.stringify(upgraded))
      return upgraded
    }
    return current
  }
  localStorage.setItem(STORAGE_KEY, JSON.stringify(seedData))
  return structuredClone(seedData)
}
const write = (db) => localStorage.setItem(STORAGE_KEY, JSON.stringify(db))
const publicUser = ({ password: _password, ...user }) => user
const fail = (message, fieldErrors = {}) => Promise.reject({ message, fieldErrors })
const page = (items, params = {}) => {
  const current = Math.max(Number(params.page) || 0, 0)
  const size = Math.max(Number(params.size) || 10, 1)
  return { content: items.slice(current * size, current * size + size), page: current, size, totalElements: items.length, totalPages: Math.ceil(items.length / size) }
}
const textMatch = (value, search) => String(value || '').toLocaleLowerCase('vi').includes(String(search || '').trim().toLocaleLowerCase('vi'))
const normalizeQuestion = (item) => item ? { ...item, answers: Array.isArray(item.answers) ? item.answers : [], referenceAnswer: item.referenceAnswer ?? null } : item
const normalizeMatrix = (item) => item ? { ...item, examType: item.examType || 'OBJECTIVE' } : item
const normalizePaper = (item) => item ? { ...item, questions: (item.questions || []).map(normalizeQuestion) } : item
const matchesExamType = (question, examType) => examType === 'ESSAY' ? question.type === 'ESSAY' : question.type !== 'ESSAY'
const validateQuestion = (body) => {
  if (!String(body.content || '').trim()) return { message: 'Dữ liệu câu hỏi không hợp lệ', fieldErrors: { content: 'Nội dung câu hỏi là bắt buộc' } }
  if (String(body.content).trim().length > 3000) return { message: 'Dữ liệu câu hỏi không hợp lệ', fieldErrors: { content: 'Nội dung câu hỏi tối đa 3000 ký tự' } }
  if (body.type === 'ESSAY') {
    const referenceAnswer = String(body.referenceAnswer || '').trim()
    if (!referenceAnswer) return { message: 'Dữ liệu câu hỏi không hợp lệ', fieldErrors: { referenceAnswer: 'Đáp án tham khảo là bắt buộc' } }
    if (referenceAnswer.length > 10000) return { message: 'Dữ liệu câu hỏi không hợp lệ', fieldErrors: { referenceAnswer: 'Đáp án tham khảo tối đa 10000 ký tự' } }
    if ((body.answers || []).length) return { message: 'Câu hỏi tự luận không được có danh sách đáp án' }
    return null
  }
  const answers = body.answers || []
  const correct = answers.filter((answer) => answer.isCorrect).length
  if (answers.length < 2) return { message: 'Câu hỏi trắc nghiệm cần ít nhất hai đáp án' }
  if (body.type === 'MULTIPLE_CHOICE' ? correct < 1 : correct !== 1) return { message: 'Số đáp án đúng không hợp lệ' }
  return null
}

export const mockApi = {
  async login(body) {
    const user = read().users.find((item) => item.username === body.username && item.password === body.password)
    if (!user) return fail('Tên đăng nhập hoặc mật khẩu không đúng')
    if (user.status !== 'ACTIVE') return fail('Tài khoản đã bị khóa')
    return wait({ accessToken: `mock-token-${user.id}`, user: publicUser(user) }, 450)
  },
  me: () => {
    const token = localStorage.getItem('exam_access_token')
    const user = read().users.find((item) => token === `mock-token-${item.id}`)
    if (!user) return fail('Phiên đăng nhập không hợp lệ')
    if (user.status !== 'ACTIVE') return fail('Tài khoản đã bị khóa')
    return wait(publicUser(user))
  },
  logout: () => wait({ success: true }),
  async updateProfile(body) {
    const db = read(); const current = JSON.parse(localStorage.getItem('exam_current_user') || '{}')
    const index = db.users.findIndex((item) => item.id === current.id)
    if (index < 0) return fail('Không tìm thấy người dùng')
    if (db.users.some((item) => item.id !== current.id && item.email === body.email)) return fail('Email đã được sử dụng', { email: 'Email đã được sử dụng' })
    db.users[index] = { ...db.users[index], fullName: body.fullName, email: body.email }
    write(db); return wait(publicUser(db.users[index]))
  },
  async changePassword(body) {
    const db = read(); const current = JSON.parse(localStorage.getItem('exam_current_user') || '{}')
    const user = db.users.find((item) => item.id === current.id)
    if (user?.password !== body.currentPassword) return fail('Mật khẩu hiện tại không đúng', { currentPassword: 'Mật khẩu hiện tại không đúng' })
    user.password = body.newPassword; write(db); return wait({ success: true })
  },
  getRoles: () => wait([{ id: 1, name: 'ADMIN' }, { id: 2, name: 'TEACHER' }]),
  getUsers: async (params = {}) => {
    let items = read().users.map(publicUser)
    if (params.search) items = items.filter((u) => textMatch(u.username, params.search) || textMatch(u.fullName, params.search) || textMatch(u.email, params.search))
    if (params.role) items = items.filter((u) => u.role === params.role)
    if (params.status) items = items.filter((u) => u.status === params.status)
    return wait(page(items, params))
  },
  getUser: (id) => wait(publicUser(read().users.find((item) => item.id === id))),
  async createUser(body) {
    const db = read()
    if (db.users.some((item) => item.username === body.username)) return fail('Tên đăng nhập đã tồn tại', { username: 'Tên đăng nhập đã tồn tại' })
    if (db.users.some((item) => item.email === body.email)) return fail('Email đã được sử dụng', { email: 'Email đã được sử dụng' })
    const item = { id: newId(), ...body, role: 'TEACHER', status: 'ACTIVE', createdAt: new Date().toISOString() }
    db.users.push(item); write(db); return wait(publicUser(item))
  },
  async updateUser(id, body) {
    const db = read(); const index = db.users.findIndex((item) => item.id === id)
    if (index < 0) return fail('Không tìm thấy người dùng')
    db.users[index] = { ...db.users[index], ...body, password: db.users[index].password }; write(db)
    return wait(publicUser(db.users[index]))
  },
  async updateUserStatus(id, status) {
    const db = read(); const user = db.users.find((item) => item.id === id)
    if (!user) return fail('Không tìm thấy người dùng')
    user.status = status; write(db); return wait(publicUser(user))
  },
  getSubjects: () => wait(read().subjects),
  getSubject: (id) => wait(read().subjects.find((item) => item.id === Number(id))),
  async createSubject(body) {
    const db = read(); const code = body.code.trim().toUpperCase()
    if (db.subjects.some((item) => item.code === code)) return fail('Mã môn học đã tồn tại', { code: 'Mã môn học đã tồn tại' })
    const item = { id: Math.max(0, ...db.subjects.map((x) => x.id)) + 1, ...body, code }; db.subjects.push(item); write(db); return wait(item)
  },
  async updateSubject(id, body) {
    const db = read(); const index = db.subjects.findIndex((item) => item.id === Number(id)); const code = body.code.trim().toUpperCase()
    if (db.subjects.some((item) => item.id !== Number(id) && item.code === code)) return fail('Mã môn học đã tồn tại', { code: 'Mã môn học đã tồn tại' })
    db.subjects[index] = { ...db.subjects[index], ...body, code }; write(db); return wait(db.subjects[index])
  },
  async deleteSubject(id) {
    const db = read(); const subjectId = Number(id)
    if (db.chapters.some((item) => item.subjectId === subjectId)) return fail('Không thể xóa môn học đang có chương')
    db.subjects = db.subjects.filter((item) => item.id !== subjectId); write(db); return wait({ success: true })
  },
  async getChapters(params = {}) {
    let items = read().chapters
    if (params.subjectId) items = items.filter((item) => item.subjectId === Number(params.subjectId))
    return wait([...items].sort((a, b) => a.orderIndex - b.orderIndex))
  },
  getChapter: (id) => wait(read().chapters.find((item) => item.id === Number(id))),
  async createChapter(body) {
    const db = read(); const data = { ...body, subjectId: Number(body.subjectId), orderIndex: Number(body.orderIndex) }
    if (db.chapters.some((item) => item.subjectId === data.subjectId && item.orderIndex === data.orderIndex)) return fail('Số thứ tự đã tồn tại trong môn học', { orderIndex: 'Số thứ tự đã tồn tại trong môn học' })
    const item = { id: Math.max(0, ...db.chapters.map((x) => x.id)) + 1, ...data }; db.chapters.push(item); write(db); return wait(item)
  },
  async updateChapter(id, body) {
    const db = read(); const data = { ...body, subjectId: Number(body.subjectId), orderIndex: Number(body.orderIndex) }
    if (db.chapters.some((item) => item.id !== Number(id) && item.subjectId === data.subjectId && item.orderIndex === data.orderIndex)) return fail('Số thứ tự đã tồn tại trong môn học', { orderIndex: 'Số thứ tự đã tồn tại trong môn học' })
    const index = db.chapters.findIndex((item) => item.id === Number(id)); db.chapters[index] = { ...db.chapters[index], ...data }; write(db); return wait(db.chapters[index])
  },
  async deleteChapter(id) {
    const db = read(); const chapterId = Number(id)
    if (db.questions.some((item) => item.chapterId === chapterId)) return fail('Không thể xóa chương đang có câu hỏi')
    db.chapters = db.chapters.filter((item) => item.id !== chapterId); write(db); return wait({ success: true })
  },
  async getQuestions(params = {}) {
    const db = read(); let items = db.questions
    if (params.search) items = items.filter((item) => textMatch(item.content, params.search))
    if (params.subjectId) { const ids = db.chapters.filter((c) => c.subjectId === Number(params.subjectId)).map((c) => c.id); items = items.filter((q) => ids.includes(q.chapterId)) }
    if (params.chapterId) items = items.filter((q) => q.chapterId === Number(params.chapterId))
    if (params.difficulty) items = items.filter((q) => q.difficulty === params.difficulty)
    if (params.type) items = items.filter((q) => q.type === params.type)
    return wait({ ...page(items.map(normalizeQuestion), params) })
  },
  getQuestion: (id) => wait(normalizeQuestion(read().questions.find((item) => item.id === id))),
  async createQuestion(body) {
    const validation = validateQuestion(body); if (validation) return fail(validation.message, validation.fieldErrors)
    const db = read(); const isEssay = body.type === 'ESSAY'; const item = { ...body, id: newId(), chapterId: Number(body.chapterId), answers: isEssay ? [] : body.answers.map((a) => ({ ...a, id: newId() })), referenceAnswer: isEssay ? body.referenceAnswer.trim() : null, teacherId: JSON.parse(localStorage.getItem('exam_current_user') || '{}').id, createdAt: new Date().toISOString() }
    db.questions.push(item); write(db); return wait(item)
  },
  async updateQuestion(id, body) {
    const validation = validateQuestion(body); if (validation) return fail(validation.message, validation.fieldErrors)
    const db = read(); const index = db.questions.findIndex((item) => item.id === id); const currentUser = JSON.parse(localStorage.getItem('exam_current_user') || '{}')
    if (index < 0) return fail('Không tìm thấy câu hỏi')
    if (currentUser.role !== 'ADMIN' && db.questions[index].teacherId !== currentUser.id) return fail('Bạn không có quyền cập nhật câu hỏi này')
    const isEssay = body.type === 'ESSAY'
    db.questions[index] = { ...db.questions[index], ...body, chapterId: Number(body.chapterId), answers: isEssay ? [] : body.answers.map((a) => ({ ...a, id: a.id || newId() })), referenceAnswer: isEssay ? body.referenceAnswer.trim() : null }; write(db); return wait(db.questions[index])
  },
  async deleteQuestion(id) { const db = read(); const question = db.questions.find((item) => item.id === id); const currentUser = JSON.parse(localStorage.getItem('exam_current_user') || '{}'); if (!question) return fail('Không tìm thấy câu hỏi'); if (currentUser.role !== 'ADMIN' && question.teacherId !== currentUser.id) return fail('Bạn không có quyền xóa câu hỏi này'); db.questions = db.questions.filter((item) => item.id !== id); write(db); return wait({ success: true }) },
  async getAvailability(params = {}) {
    const db = read(); const examType = params.examType || 'OBJECTIVE'; const chapters = db.chapters.filter((c) => !params.subjectId || c.subjectId === Number(params.subjectId))
    return wait(chapters.flatMap((chapter) => ['EASY', 'MEDIUM', 'HARD'].map((difficulty) => ({ chapterId: chapter.id, difficulty, available: db.questions.filter((q) => q.chapterId === chapter.id && q.difficulty === difficulty && matchesExamType(q, examType)).length }))))
  },
  async getMatrices(params = {}) {
    let items = read().matrices.map(normalizeMatrix)
    if (params.search) items = items.filter((item) => textMatch(item.title, params.search))
    if (params.subjectId) items = items.filter((item) => item.subjectId === Number(params.subjectId))
    if (params.examType) items = items.filter((item) => item.examType === params.examType)
    return wait(page(items, params))
  },
  getMatrix: (id) => wait(normalizeMatrix(read().matrices.find((item) => item.id === id))),
  async createMatrix(body) {
    const db = read(); const current = JSON.parse(localStorage.getItem('exam_current_user') || '{}'); const now = new Date().toISOString()
    const item = { ...body, id: newId(), subjectId: Number(body.subjectId), examType: body.examType || 'OBJECTIVE', teacherId: current.id, createdAt: now, updatedAt: now }; db.matrices.push(item); write(db); return wait(item)
  },
  async updateMatrix(id, body) { const db = read(); const index = db.matrices.findIndex((item) => item.id === id); db.matrices[index] = { ...db.matrices[index], ...body, subjectId: Number(body.subjectId), examType: body.examType || db.matrices[index].examType || 'OBJECTIVE', updatedAt: new Date().toISOString() }; write(db); return wait(db.matrices[index]) },
  async deleteMatrix(id) { const db = read(); if (db.papers.some((p) => p.matrixId === id)) return fail('Không thể xóa ma trận đã sinh đề'); db.matrices = db.matrices.filter((item) => item.id !== id); write(db); return wait({ success: true }) },
  async generatePapers(id, body) {
    const db = read(); const matrix = normalizeMatrix(db.matrices.find((item) => item.id === id))
    if (!matrix) return fail('Không tìm thấy ma trận')
    for (const config of matrix.configs) if (db.questions.filter((q) => q.chapterId === config.chapterId && q.difficulty === config.difficulty && matchesExamType(q, matrix.examType)).length < config.quantity) return fail('Số câu hỏi khả dụng không đủ để sinh đề')
    const existing = db.papers.filter((p) => p.matrixId === id).length
    const generated = Array.from({ length: Number(body.numberOfPapers) }, (_, index) => {
      const questions = matrix.configs.flatMap((config) => db.questions.filter((q) => q.chapterId === config.chapterId && q.difficulty === config.difficulty && matchesExamType(q, matrix.examType)).sort(() => Math.random() - 0.5).slice(0, config.quantity)).sort(() => Math.random() - 0.5).map((q, order) => ({ id: q.id, questionOrder: order + 1, content: q.content, type: q.type, difficulty: q.difficulty, answers: q.type === 'ESSAY' ? [] : (q.answers || []).map((answer) => ({ ...answer })).sort(() => Math.random() - 0.5), referenceAnswer: q.referenceAnswer ?? null }))
      return { id: newId(), matrixId: id, examCode: `M${String(existing + index + 1).padStart(2, '0')}`, createdAt: new Date().toISOString(), questions }
    })
    db.papers.push(...generated); write(db); return wait(generated, 600)
  },
  async getPapers(params = {}) { let items = read().papers.map(normalizePaper); if (params.matrixId) items = items.filter((item) => item.matrixId === params.matrixId); return wait(page(items, params)) },
  getPaper: (id) => wait(normalizePaper(read().papers.find((item) => item.id === id))),
  async deletePaper(id) { const db = read(); db.papers = db.papers.filter((item) => item.id !== id); write(db); return wait({ success: true }) },
  async getDashboard() {
    const db = read(); const difficulty = ['EASY', 'MEDIUM', 'HARD'].map((key) => ({ difficulty: key, count: db.questions.filter((q) => q.difficulty === key).length }))
    return wait({ totalSubjects: db.subjects.length, totalChapters: db.chapters.length, totalQuestions: db.questions.length, totalMatrices: db.matrices.length, totalPapers: db.papers.length, difficulty, recentMatrices: [...db.matrices].sort((a, b) => String(b.updatedAt).localeCompare(String(a.updatedAt))).slice(0, 5) })
  },
}
