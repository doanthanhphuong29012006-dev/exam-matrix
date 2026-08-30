const now = new Date().toISOString()

export const seedData = {
  subjects: [
    { id: 1, code: 'MATH', name: 'Toán học', description: 'Chương trình Toán trung học phổ thông' },
    { id: 2, code: 'PHYS', name: 'Vật lý', description: 'Chương trình Vật lý trung học phổ thông' },
    { id: 3, code: 'CHEM', name: 'Hóa học', description: 'Chương trình Hóa học trung học phổ thông' },
  ],
  chapters: [
    { id: 1, subjectId: 1, name: 'Hàm số và đồ thị', orderIndex: 1 },
    { id: 2, subjectId: 1, name: 'Mũ và lôgarit', orderIndex: 2 },
    { id: 3, subjectId: 1, name: 'Nguyên hàm và tích phân', orderIndex: 3 },
    { id: 4, subjectId: 2, name: 'Dao động cơ', orderIndex: 1 },
    { id: 5, subjectId: 2, name: 'Dòng điện xoay chiều', orderIndex: 2 },
    { id: 6, subjectId: 3, name: 'Cấu tạo nguyên tử', orderIndex: 1 },
  ],
  users: [
    { id: 'u-admin', username: 'admin', password: 'admin123', email: 'admin@exammatrix.vn', fullName: 'Quản trị hệ thống', role: 'ADMIN', status: 'ACTIVE', createdAt: now },
    { id: 'u-teacher', username: 'teacher', password: 'teacher123', email: 'teacher@exammatrix.vn', fullName: 'Nguyễn Minh Anh', role: 'TEACHER', status: 'ACTIVE', createdAt: now },
    { id: 'u-teacher-2', username: 'lan.pham', password: 'teacher123', email: 'lan.pham@exammatrix.vn', fullName: 'Phạm Thu Lan', role: 'TEACHER', status: 'LOCKED', createdAt: now },
  ],
  questions: [
    { id: 'q-1', chapterId: 1, teacherId: 'u-teacher', content: 'Hàm số y = x² đồng biến trên khoảng nào?', difficulty: 'EASY', type: 'SINGLE_CHOICE', answers: [{ id: 'a-1', content: '(0; +∞)', isCorrect: true }, { id: 'a-2', content: '(-∞; 0)', isCorrect: false }, { id: 'a-3', content: 'R', isCorrect: false }], createdAt: now },
    { id: 'q-2', chapterId: 1, teacherId: 'u-teacher', content: 'Đạo hàm của hàm số y = x³ - 3x là biểu thức nào?', difficulty: 'MEDIUM', type: 'SINGLE_CHOICE', answers: [{ id: 'a-4', content: '3x² - 3', isCorrect: true }, { id: 'a-5', content: 'x² - 3', isCorrect: false }, { id: 'a-6', content: '3x²', isCorrect: false }], createdAt: now },
    { id: 'q-3', chapterId: 1, teacherId: 'u-teacher', content: 'Một hàm số có thể có nhiều điểm cực trị.', difficulty: 'EASY', type: 'TRUE_FALSE', answers: [{ id: 'a-7', content: 'Đúng', isCorrect: true }, { id: 'a-8', content: 'Sai', isCorrect: false }], createdAt: now },
    { id: 'q-4', chapterId: 2, teacherId: 'u-teacher', content: 'Chọn các đẳng thức đúng với a > 0, a khác 1.', difficulty: 'MEDIUM', type: 'MULTIPLE_CHOICE', answers: [{ id: 'a-9', content: 'logₐ(xy) = logₐx + logₐy', isCorrect: true }, { id: 'a-10', content: 'logₐ(x + y) = logₐx + logₐy', isCorrect: false }, { id: 'a-11', content: 'logₐ(xⁿ) = n logₐx', isCorrect: true }], createdAt: now },
    { id: 'q-5', chapterId: 2, teacherId: 'u-teacher', content: 'Giải phương trình 2ˣ = 16.', difficulty: 'EASY', type: 'SINGLE_CHOICE', answers: [{ id: 'a-12', content: 'x = 4', isCorrect: true }, { id: 'a-13', content: 'x = 8', isCorrect: false }], createdAt: now },
    { id: 'q-6', chapterId: 3, teacherId: 'u-teacher', content: 'Tính tích phân của hàm số f(x) = 2x trên đoạn [0; 1].', difficulty: 'MEDIUM', type: 'SINGLE_CHOICE', answers: [{ id: 'a-14', content: '1', isCorrect: true }, { id: 'a-15', content: '2', isCorrect: false }, { id: 'a-16', content: '0', isCorrect: false }], createdAt: now },
    { id: 'q-7', chapterId: 4, teacherId: 'u-teacher', content: 'Chu kỳ dao động điều hòa phụ thuộc vào đại lượng nào?', difficulty: 'HARD', type: 'SINGLE_CHOICE', answers: [{ id: 'a-17', content: 'Các thông số của hệ dao động', isCorrect: true }, { id: 'a-18', content: 'Biên độ dao động', isCorrect: false }], createdAt: now },
    { id: 'q-8', chapterId: 4, teacherId: 'u-teacher', content: 'Vận tốc trong dao động điều hòa biến thiên tuần hoàn.', difficulty: 'EASY', type: 'TRUE_FALSE', answers: [{ id: 'a-19', content: 'Đúng', isCorrect: true }, { id: 'a-20', content: 'Sai', isCorrect: false }], createdAt: now },
    { id: 'q-9', chapterId: 5, teacherId: 'u-teacher', content: 'Giá trị hiệu dụng của dòng điện xoay chiều liên hệ thế nào với giá trị cực đại?', difficulty: 'MEDIUM', type: 'SINGLE_CHOICE', answers: [{ id: 'a-21', content: 'I = I₀/√2', isCorrect: true }, { id: 'a-22', content: 'I = I₀√2', isCorrect: false }], createdAt: now },
    { id: 'q-10', chapterId: 6, teacherId: 'u-teacher', content: 'Hạt nào không mang điện trong nguyên tử?', difficulty: 'EASY', type: 'SINGLE_CHOICE', answers: [{ id: 'a-23', content: 'Nơtron', isCorrect: true }, { id: 'a-24', content: 'Proton', isCorrect: false }, { id: 'a-25', content: 'Electron', isCorrect: false }], createdAt: now },
  ],
  matrices: [
    { id: 'm-1', teacherId: 'u-teacher', subjectId: 1, title: 'Kiểm tra giữa kỳ Toán 12', duration: 45, totalQuestions: 3, configs: [{ chapterId: 1, difficulty: 'EASY', quantity: 2 }, { chapterId: 1, difficulty: 'MEDIUM', quantity: 1 }], createdAt: now, updatedAt: now },
  ],
  papers: [],
}
