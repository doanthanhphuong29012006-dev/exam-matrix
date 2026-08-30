export const DIFFICULTIES = [
  { value: 'EASY', label: 'Dễ' },
  { value: 'MEDIUM', label: 'Trung bình' },
  { value: 'HARD', label: 'Khó' },
]

export const QUESTION_TYPES = [
  { value: 'SINGLE_CHOICE', label: 'Một đáp án' },
  { value: 'MULTIPLE_CHOICE', label: 'Nhiều đáp án' },
  { value: 'TRUE_FALSE', label: 'Đúng / Sai' },
]

export const difficultyLabel = (value) => DIFFICULTIES.find((item) => item.value === value)?.label || value
export const typeLabel = (value) => QUESTION_TYPES.find((item) => item.value === value)?.label || value
export const roleLabel = (value) => (value === 'ADMIN' ? 'Quản trị viên' : 'Giáo viên')
