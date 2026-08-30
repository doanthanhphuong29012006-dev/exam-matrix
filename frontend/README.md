# Exam Matrix Frontend

Frontend ReactJS cho hệ thống quản lý ngân hàng câu hỏi, ma trận đề và sinh đề thi.

## Chạy dự án

Yêu cầu Node.js 20 trở lên và pnpm.

```bash
pnpm install
pnpm dev
```

Mặc định ứng dụng chạy tại `http://localhost:5173` và dùng dữ liệu mock trong `localStorage`.

Tài khoản mẫu:

- Quản trị viên: `admin` / `admin123`
- Giáo viên: `teacher` / `teacher123`

## Cấu hình môi trường

```env
VITE_API_BASE_URL=http://localhost:8080
VITE_USE_MOCK_API=true
```

Đổi `VITE_USE_MOCK_API=false` để sử dụng backend thật. Component không phụ thuộc vào chế độ dữ liệu; mọi request đều đi qua `src/api/service.js`.

## Kiểm tra

```bash
pnpm lint
pnpm build
```

Mock mode hỗ trợ một lần lỗi mô phỏng bằng cách đặt `sessionStorage.exam_mock_force_error = "true"` trong DevTools trước thao tác cần kiểm tra.
