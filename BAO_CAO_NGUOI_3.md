# 📄 BÁO CÁO TỔNG HỢP CÔNG VIỆC NGƯỜI 3: PHÂN HỆ SINH VÀ LƯU ĐỀ

> **Dự án:** Hệ thống Quản lý Ngân hàng Câu hỏi và Sinh đề thi (Exam Matrix)  
> **Người thực hiện:** Người 3 (Phụ trách Thuật toán sinh và lưu đề thi)  
> **Repository:** https://github.com/doanthanhphuong29012006-dev/exam-matrix  
> **Nhánh GitHub:** `feature/person-3-exam-generation`  

---

## 1. 📌 TỔNG QUAN PHẦN VIỆC ĐƯỢC GIAO (TAB 9)

Theo phân công trong **Tab 9** của tài liệu dự án, **Người 3** chịu trách nhiệm cho các thành phần cốt lõi:
1. **Phụ trách chính:** Thuật toán sinh đề thi ngẫu nhiên và lưu trữ đề thi vào Cơ sở dữ liệu PostgreSQL.
2. **API được giao:** `POST /exam-matrices/{id}/generate`
3. **Phân loại đề hỗ trợ:** Đề trắc nghiệm (`OBJECTIVE`) và Đề tự luận (`ESSAY`).
4. **Đồng bộ hợp đồng dữ liệu:** Bổ sung trường `referenceAnswer` (đáp án tham khảo) cho câu tự luận và `examType` cho ma trận đề thi.

---

## 2. 📂 DANH SÁCH FILE MÃ NGUỒN ĐÃ TRIỂN KHAI

Toàn bộ 12 file mã nguồn đã được tạo mới / cập nhật trực tiếp tại thư mục `d:\exam-matrix(1)\backend`:

### A. Data Models & Enums
- **`ExamType.java`** (`backend/src/main/java/com/exammatrix/backend/entity/enums/ExamType.java`): [TẠO MỚI] Enum gồm 2 giá trị `OBJECTIVE` (Trắc nghiệm) và `ESSAY` (Tự luận).
- **`QuestionType.java`** (`backend/src/main/java/com/exammatrix/backend/entity/enums/QuestionType.java`): [CẬP NHẬT] Thêm enum `ESSAY`.
- **`Question.java`** (`backend/src/main/java/com/exammatrix/backend/entity/Question.java`): [CẬP NHẬT] Thêm trường `referenceAnswer` (kiểu `TEXT`) lưu đáp án tham khảo.
- **`ExamMatrix.java`** (`backend/src/main/java/com/exammatrix/backend/entity/ExamMatrix.java`): [CẬP NHẬT] Thêm trường `examType` (`ExamType`).
- **`PaperQuestionResponse.java`** (`backend/src/main/java/com/exammatrix/backend/dto/response/PaperQuestionResponse.java`): [CẬP NHẬT] Thêm trường `referenceAnswer` trong DTO trả về cho client.

### B. Repository Layer
- **`QuestionRepository.java`** (`backend/src/main/java/com/exammatrix/backend/repository/QuestionRepository.java`): [CẬP NHẬT] Thêm phương thức `findAllByChapter_IdAndDifficultyAndTypeIn` hỗ trợ lọc câu hỏi theo chương, độ khó và danh sách loại câu hợp lệ.

### C. Service & Controller Layer
- **`ExamGenerationService.java`** (`backend/src/main/java/com/exammatrix/backend/service/ExamGenerationService.java`): [TẠO MỚI] Interface dịch vụ sinh đề.
- **`ExamGenerationServiceImpl.java`** (`backend/src/main/java/com/exammatrix/backend/service/impl/ExamGenerationServiceImpl.java`): [TẠO MỚI] Cài đặt thuật toán 10 bước sinh & lưu đề, hoán vị ngẫu nhiên, sinh mã đề 6 chữ số và quản lý `@Transactional`.
- **`ExamPaperServiceImpl.java`** (`backend/src/main/java/com/exammatrix/backend/service/impl/ExamPaperServiceImpl.java`): [CẬP NHẬT] Đồng bộ mapper dữ liệu `PaperQuestionResponse`.
- **`ExamMatrixController.java`** (`backend/src/main/java/com/exammatrix/backend/controller/ExamMatrixController.java`): [CẬP NHẬT] Tiêm `ExamGenerationService` vào endpoint `POST /exam-matrices/{id}/generate`.

### D. Unit Tests & Config
- **`ExamGenerationServiceTest.java`** (`backend/src/test/java/com/exammatrix/backend/service/ExamGenerationServiceTest.java`): [TẠO MỚI] Phủ 6 kịch bản kiểm thử đơn vị tự động.
- **`application.yml`** (`backend/src/main/resources/application.yml`): [CẬP NHẬT] Thêm fallback mặc định local cho kết nối CSDL và JWT.

---

## 3. 🌐 ĐỊNH NGHĨA API & QUY TRÌNH XỬ LÝ

### Endpoint: `POST /exam-matrices/{id}/generate`
* **Path Variable:** `id` (UUID của Ma trận đề thi).
* **Request Body:**
  ```json
  {
    "numberOfPapers": 2
  }
  ```
  *(Ràng buộc: `numberOfPapers` từ 1 đến 20).*
* **Response Status:** `201 Created`
* **Response Body Example:**
  ```json
  [
    {
      "id": "55555555-5555-4555-8555-555555555555",
      "matrixId": "44444444-4444-4444-8444-444444444444",
      "examCode": "102345",
      "createdAt": "2026-09-09T21:10:00",
      "questions": [
        {
          "id": "11111111-1111-4111-8111-111111111111",
          "questionOrder": 1,
          "content": "Phân biệt overloading và overriding.",
          "type": "ESSAY",
          "difficulty": "MEDIUM",
          "answers": [],
          "referenceAnswer": "Overloading là...\nOverriding là..."
        }
      ]
    }
  ]
  ```

---

## 4. ⚙️ THUẬT TOÁN SINH ĐỀ 10 BƯỚC

1. **Tìm ma trận:** Truy vấn `ExamMatrix` theo `id` (nếu thiếu $\rightarrow$ trả `404 NOT_FOUND`).
2. **Kiểm tra quyền:** Đảm bảo người gọi là `ADMIN` hoặc chủ sở hữu ma trận (`403 FORBIDDEN`).
3. **Đọc nhóm loại đề:** Xác định `examType` (`OBJECTIVE` hoặc `ESSAY`).
4. **Lọc kho câu hỏi:** 
   - Đề `OBJECTIVE`: Chỉ truy vấn các câu `SINGLE_CHOICE`, `MULTIPLE_CHOICE`, `TRUE_FALSE`.
   - Đề `ESSAY`: Chỉ truy vấn các câu `ESSAY`.
5. **Kiểm tra số lượng:** Đảm bảo kho câu hỏi có đủ số lượng theo cấu hình ma trận (`409 CONFLICT`).
6. **Rút trích ngẫu nhiên:** Chọn ngẫu nhiên câu hỏi không lặp ID trong cùng một đề thi.
7. **Hoán vị (Fisher-Yates Shuffle):**
   - Xáo trộn thứ tự các câu hỏi trong đề, gán `questionOrder` từ 1.
   - Trắc nghiệm: Xáo trộn các đáp án A/B/C/D.
   - Tự luận: Giữ `answers: []` và gắn `referenceAnswer`.
8. **Sinh mã đề (`examCode`):** Tạo mã đề ngẫu nhiên 6 chữ số duy nhất trong cùng một ma trận.
9. **Lưu CSDL:** Lưu `ExamPaper` và `PaperQuestion` trong **1 `@Transactional`** duy nhất.
10. **Trả kết quả:** Chuyển đổi và trả về `List<ExamPaperDetailResponse>`.

---

## 5. 🧪 KẾT QUẢ KIỂM THỬ ĐƠN VỊ (UNIT TESTS)

Bộ test tự động trong [`ExamGenerationServiceTest.java`](file:///d:/exam-matrix(1)/backend/src/test/java/com/exammatrix/backend/service/ExamGenerationServiceTest.java) đã được chạy thành công qua lệnh `mvn test`:

```txt
[INFO] Running com.exammatrix.backend.service.ExamGenerationServiceTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.330 s
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

| STT | Tên Kịch Bản Kiểm Thử | Mục Tiêu Kiểm Tra | Kết Quả |
| :---: | :--- | :--- | :---: |
| 1 | `generateExamPapers_Success_Objective` | Sinh đề trắc nghiệm (hoán vị câu/đáp án, mã 6 số) | **PASSED** |
| 2 | `generateExamPapers_Success_Essay` | Sinh đề tự luận (có `referenceAnswer`, `answers` rỗng) | **PASSED** |
| 3 | `generateExamPapers_NotFound` | Bắt lỗi `404 NOT_FOUND` khi không tìm thấy ma trận | **PASSED** |
| 4 | `generateExamPapers_Forbidden` | Bắt lỗi `403 FORBIDDEN` khi không có quyền truy cập | **PASSED** |
| 5 | `generateExamPapers_Conflict_NotEnoughQuestions` | Bắt lỗi `409 CONFLICT` khi thiếu câu hỏi trong kho | **PASSED** |
| 6 | `generateExamPapers_InvalidNumberOfPapers` | Bắt lỗi `400 BAD_REQUEST` khi số lượng mã đề sai | **PASSED** |

---

## 6. 🐙 THÔNG TIN PUSH CODE LÊN GITHUB

- **Repository Remote:** `https://github.com/doanthanhphuong29012006-dev/exam-matrix`
- **Nhánh Phụ Đã Push:** `feature/person-3-exam-generation`
- **Link Tạo Pull Request (PR):** https://github.com/doanthanhphuong29012006-dev/exam-matrix/pull/new/feature/person-3-exam-generation
- **Trạng thái:** Đã push an toàn lên nhánh phụ, tuân thủ đúng quy định **"KHÔNG PUSH CODE LÊN MAIN"**.

---

## 7. 📋 CHECKLIST TỔNG KẾT TẤT CẢ CÔNG VIỆC

- [x] Phân tích hợp đồng API và tài liệu Tab 9
- [x] Tạo Enum `ExamType` (`OBJECTIVE`, `ESSAY`)
- [x] Cập nhật Enum `QuestionType` (thêm `ESSAY`)
- [x] Cập nhật Entity `Question` (thêm `referenceAnswer`)
- [x] Cập nhật Entity `ExamMatrix` (thêm `examType`)
- [x] Cập nhật DTO `PaperQuestionResponse` (thêm `referenceAnswer`)
- [x] Cập nhật `QuestionRepository` hỗ trợ lọc câu theo loại
- [x] Định nghĩa Interface `ExamGenerationService`
- [x] Triển khai `ExamGenerationServiceImpl` với 10 bước thuật toán sinh & lưu đề
- [x] Kết nối `ExamMatrixController` cho API `POST /exam-matrices/{id}/generate`
- [x] Biên dịch Java sạch sẽ (`mvn compile` - **BUILD SUCCESS**)
- [x] Viết & Chạy bộ Unit Test tự động (**6/6 PASSED**)
- [x] Đẩy code lên nhánh phụ `feature/person-3-exam-generation` trên GitHub
