# HƯỚNG DẪN CHẠY DỰ ÁN HARVEST HUB

## 1. TỔNG QUAN DỰ ÁN

Dự án Harvest Hub bao gồm 2 phần chính:
- **Backend**: Spring Boot (Java 21) - Chạy trên port 8081
- **Frontend**: React + TypeScript + Vite - Chạy trên port 8082

---

## 2. YÊU CẦU HỆ THỐNG

### 2.1. Phần mềm cần cài đặt

| Phần mềm | Yêu cầu | Link tải |
|----------|---------|----------|
| Java Development Kit (JDK) | Phiên bản 21 trở lên | https://www.oracle.com/java/technologies/downloads/ |
| Maven | Phiên bản 3.6+ (hoặc sử dụng Maven Wrapper có sẵn trong dự án) | https://maven.apache.org/download.cgi |
| Node.js | Phiên bản 18 trở lên | https://nodejs.org/ |
| npm hoặc yarn | Đi kèm với Node.js | - |
| MongoDB | Phiên bản 6.0+ (Local hoặc MongoDB Atlas) | https://www.mongodb.com/try/download/community |
| IDE (Tùy chọn) | IntelliJ IDEA, Eclipse, VS Code | - |

### 2.2. Cài đặt MongoDB Local

Dự án đang được cấu hình để sử dụng MongoDB Local (không cần internet).

**Các bước cài đặt:**
1. Tải MongoDB Community Edition từ trang chủ MongoDB
2. Cài đặt MongoDB theo hướng dẫn
3. Khởi động MongoDB service (trên Windows: chạy mongod.exe hoặc start service)
4. Kiểm tra MongoDB đang chạy trên port 27017 (mặc định)

---

## 3. CÀI ĐẶT VÀ CHẠY BACKEND

### 3.1. Kiểm tra Java và Maven

Mở Command Prompt hoặc Terminal và chạy các lệnh sau để kiểm tra:

```bash
java -version
mvn -version
```

Nếu chưa cài đặt, vui lòng cài đặt Java 21 và Maven trước.

### 3.2. Cấu hình MongoDB

Mở file: **`src/main/resources/application.properties`**

Đảm bảo dòng cấu hình MongoDB như sau:

```
spring.data.mongodb.uri=mongodb://localhost:27017/harvesthub
```

Nếu bạn muốn sử dụng MongoDB Atlas (cloud), comment dòng trên và uncomment dòng MongoDB Atlas trong file.

### 3.3. Cài đặt dependencies và chạy Backend

**Mở Command Prompt/Terminal tại thư mục backend:**

```bash
cd harvest-hub-backend
```

**Cài đặt dependencies bằng Maven:**

```bash
mvn clean install
```

**Hoặc sử dụng Maven Wrapper (khuyến nghị):**

```bash
mvnw.cmd clean install
```

**Chạy ứng dụng Backend:**

```bash
mvnw.cmd spring-boot:run
```

**Hoặc:**

```bash
mvn spring-boot:run
```

Backend sẽ chạy trên: **http://localhost:8081**

Kiểm tra API documentation tại: **http://localhost:8081/swagger-ui.html**

---

## 4. CÀI ĐẶT VÀ CHẠY FRONTEND

### 4.1. Kiểm tra Node.js

Mở Command Prompt hoặc Terminal và chạy:

```bash
node -v
npm -v
```

### 4.2. Cài đặt dependencies và chạy Frontend

**Mở Command Prompt/Terminal tại thư mục frontend:**

```bash
cd harvest-hub-emporium-main
```

**Cài đặt dependencies:**

```bash
npm install
```

**Chạy ứng dụng Frontend ở chế độ development:**

```bash
npm run dev
```

Frontend sẽ chạy trên: **http://localhost:8082**

---

## 5. CẤU HÌNH BỔ SUNG

### 5.1. Cấu hình Email (Tùy chọn)

Nếu bạn muốn sử dụng tính năng gửi email, cần cấu hình trong file `application.properties`:

```
spring.mail.username=${MAIL_USERNAME:}
spring.mail.password=${MAIL_PASSWORD:}
```

**Hoặc set environment variables (Windows):**

```bash
set MAIL_USERNAME=your-email@gmail.com
set MAIL_PASSWORD=your-app-password
```

**Hoặc set environment variables (Linux/Mac):**

```bash
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-app-password
```

### 5.2. Cấu hình CORS và WebSocket

Cấu hình đã được thiết lập sẵn trong file `application.properties`. Frontend (port 8082) đã được thêm vào danh sách allowed origins.

---

## 6. XỬ LÝ LỖI THƯỜNG GẶP

### 6.1. Lỗi kết nối MongoDB

- Kiểm tra MongoDB đã được khởi động chưa
- Kiểm tra port 27017 có đang được sử dụng không
- Kiểm tra lại cấu hình trong `application.properties`

### 6.2. Lỗi port đã được sử dụng

- **Backend (8081)**: Kiểm tra xem có ứng dụng nào đang chạy trên port này không
- **Frontend (8082)**: Kiểm tra xem có ứng dụng nào đang chạy trên port này không
- Có thể thay đổi port trong `application.properties` (backend) hoặc `vite.config.ts` (frontend)

### 6.3. Lỗi dependencies không tải được

- Kiểm tra kết nối internet
- Xóa thư mục `node_modules` và chạy lại `npm install` (frontend)
- Xóa thư mục `.m2` và chạy lại `mvn clean install` (backend)

### 6.4. Lỗi Java version

- Đảm bảo đã cài đặt Java 21
- Kiểm tra `JAVA_HOME` environment variable
- Chạy `java -version` để xác nhận phiên bản

---

## 7. KIỂM TRA HOẠT ĐỘNG

Sau khi chạy cả Backend và Frontend, kiểm tra:

1. **Backend API**: Mở trình duyệt và truy cập http://localhost:8081/api-docs
2. **Swagger UI**: Truy cập http://localhost:8081/swagger-ui.html
3. **Frontend**: Truy cập http://localhost:8082

---

## 8. THỨ TỰ CHẠY DỰ ÁN

Để đảm bảo dự án hoạt động đúng, hãy chạy theo thứ tự sau:

1. **Khởi động MongoDB** (nếu dùng local)
2. **Chạy Backend** (Spring Boot)
3. **Chạy Frontend** (React)
4. **Mở trình duyệt** và truy cập http://localhost:8082

---

## 9. TÓM TẮT CÁC LỆNH QUAN TRỌNG

| Thao tác | Lệnh |
|----------|------|
| Kiểm tra Java | `java -version` |
| Kiểm tra Maven | `mvn -version` |
| Kiểm tra Node.js | `node -v` |
| Cài đặt Backend dependencies | `cd harvest-hub-backend`<br>`mvnw.cmd clean install` |
| Chạy Backend | `cd harvest-hub-backend`<br>`mvnw.cmd spring-boot:run` |
| Cài đặt Frontend dependencies | `cd harvest-hub-emporium-main`<br>`npm install` |
| Chạy Frontend | `cd harvest-hub-emporium-main`<br>`npm run dev` |

---

## 10. THÔNG TIN BỔ SUNG

### Cấu trúc dự án

**Backend:**
- Framework: Spring Boot 3.5.3
- Java Version: 21
- Database: MongoDB
- Port: 8081
- Build tool: Maven

**Frontend:**
- Framework: React 18
- Language: TypeScript
- Build tool: Vite
- Port: 8082
- UI Library: shadcn-ui, Tailwind CSS

### Các tính năng chính

- Đăng ký/Đăng nhập người dùng
- Quản lý sản phẩm
- Quản lý giỏ hàng
- Đặt hàng và thanh toán
- Quản lý đơn hàng
- Đánh giá sản phẩm
- Chat/WebSocket
- Flash Sale
- Quản lý người bán
- Dashboard quản trị

---

**Chúc bạn thành công trong việc chạy dự án!**

Nếu gặp vấn đề, vui lòng kiểm tra lại các bước trên hoặc liên hệ với team phát triển.

