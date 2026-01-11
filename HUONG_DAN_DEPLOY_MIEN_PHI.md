# 🚀 Hướng Dẫn Deploy Dự Án Miễn Phí

Hướng dẫn chi tiết để public dự án backend Spring Boot của bạn lên internet **hoàn toàn miễn phí** và có thể truy cập từ bất kỳ WiFi nào.

---

## 📋 Mục Lục

1. [Render.com (Khuyến nghị)](#1-rendercom-khuyến-nghị)
2. [Railway.app](#2-railwayapp)
3. [Fly.io](#3-flyio)
4. [MongoDB Atlas (Database miễn phí)](#4-mongodb-atlas-database-miễn-phí)
5. [Cấu hình Environment Variables](#5-cấu-hình-environment-variables)

---

## 1. Render.com (Khuyến nghị) ⭐

**Ưu điểm:**
- ✅ Miễn phí 750 giờ/tháng (đủ dùng)
- ✅ Tự động deploy từ GitHub
- ✅ Hỗ trợ Java/Spring Boot tốt
- ✅ SSL/HTTPS tự động
- ✅ Dễ sử dụng

### Các bước:

#### Bước 1: Tạo MongoDB Atlas (Database miễn phí)
1. Truy cập: https://www.mongodb.com/cloud/atlas/register
2. Đăng ký tài khoản miễn phí
3. Tạo cluster miễn phí (M0 - Free tier)
4. Chọn region gần Việt Nam (Singapore hoặc Mumbai)
5. Tạo database user và password
6. Whitelist IP: `0.0.0.0/0` (cho phép mọi IP)
7. Copy connection string (sẽ dùng ở bước sau)

#### Bước 2: Deploy lên Render
1. Truy cập: https://render.com
2. Đăng nhập bằng GitHub
3. Click **"New +"** → **"Web Service"**
4. Connect repository GitHub của bạn
5. Cấu hình:
   - **Name**: `harvest-hub-backend`
   - **Environment**: `Java`
   - **Build Command**: `mvn clean package -DskipTests`
   - **Start Command**: `java -jar target/harvest-hub-backend-0.0.1-SNAPSHOT.jar`
   - **Instance Type**: `Free`

6. Thêm Environment Variables:
   ```
   JAVA_VERSION=21
   SPRING_PROFILES_ACTIVE=production
   SERVER_PORT=10000
   SPRING_DATA_MONGODB_URI=<MongoDB connection string từ Atlas>
   GOOGLE_CLIENT_ID=<Your Google Client ID>
   GOOGLE_CLIENT_SECRET=<Your Google Client Secret>
   FACEBOOK_CLIENT_ID=<Your Facebook App ID>
   FACEBOOK_CLIENT_SECRET=<Your Facebook App Secret>
   MAIL_USERNAME=<Your Gmail>
   MAIL_PASSWORD=<Your Gmail App Password>
   FRONTEND_BASE_URL=<URL frontend của bạn>
   ```

7. Click **"Create Web Service"**
8. Đợi build và deploy (5-10 phút)
9. URL của bạn sẽ là: `https://harvest-hub-backend.onrender.com`

---

## 2. Railway.app

**Ưu điểm:**
- ✅ $5 credit miễn phí/tháng
- ✅ Deploy cực nhanh
- ✅ Hỗ trợ Docker

### Các bước:

1. Truy cập: https://railway.app
2. Đăng nhập bằng GitHub
3. Click **"New Project"** → **"Deploy from GitHub repo"**
4. Chọn repository của bạn
5. Railway sẽ tự động detect Java project
6. Thêm Environment Variables (giống như Render)
7. Deploy tự động!

---

## 3. Fly.io

**Ưu điểm:**
- ✅ 3 VMs miễn phí
- ✅ Deploy toàn cầu
- ✅ Performance tốt

### Các bước:

1. Cài đặt Fly CLI:
   ```bash
   # Windows (PowerShell)
   powershell -Command "iwr https://fly.io/install.ps1 -useb | iex"
   ```

2. Đăng nhập:
   ```bash
   fly auth login
   ```

3. Tạo app:
   ```bash
   fly launch
   ```

4. Deploy:
   ```bash
   fly deploy
   ```

5. Xem URL:
   ```bash
   fly open
   ```

---

## 4. MongoDB Atlas (Database miễn phí)

### Tạo Cluster miễn phí:

1. **Đăng ký**: https://www.mongodb.com/cloud/atlas/register
2. **Tạo Cluster**:
   - Chọn **M0 Sandbox** (Free tier)
   - Chọn region: **Singapore** hoặc **Mumbai** (gần VN)
   - Click **"Create Cluster"**

3. **Tạo Database User**:
   - Vào **"Database Access"**
   - Click **"Add New Database User"**
   - Chọn **"Password"** authentication
   - Username: `harvesthub` (hoặc tên bạn muốn)
   - Password: Tạo password mạnh
   - Database User Privileges: **"Atlas admin"**
   - Click **"Add User"**

4. **Whitelist IP**:
   - Vào **"Network Access"**
   - Click **"Add IP Address"**
   - Chọn **"Allow Access from Anywhere"** (0.0.0.0/0)
   - Click **"Confirm"**

5. **Lấy Connection String**:
   - Vào **"Database"** → Click **"Connect"**
   - Chọn **"Connect your application"**
   - Copy connection string, ví dụ:
     ```
     mongodb+srv://harvesthub:<password>@cluster0.xxxxx.mongodb.net/harvesthub?retryWrites=true&w=majority
     ```
   - Thay `<password>` bằng password bạn đã tạo

---

## 5. Cấu hình Environment Variables

Sau khi deploy, bạn cần set các biến môi trường sau trong dashboard của platform:

### Bắt buộc:
```bash
SPRING_DATA_MONGODB_URI=mongodb+srv://user:password@cluster.mongodb.net/harvesthub?retryWrites=true&w=majority
```

### OAuth (nếu dùng):
```bash
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
FACEBOOK_CLIENT_ID=your-facebook-app-id
FACEBOOK_CLIENT_SECRET=your-facebook-app-secret
```

### Email (nếu dùng):
```bash
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password  # Gmail App Password, không phải password thường
```

### Frontend URL:
```bash
FRONTEND_BASE_URL=https://your-frontend-url.com
```

### VNPay (nếu dùng):
```bash
VNPAY_TMN_CODE=your-tmn-code
VNPAY_HASH_SECRET=your-hash-secret
VNPAY_RETURN_URL=https://your-frontend-url.com/payment/return
```

---

## 🔧 Cập nhật application.properties cho Production

File `application.properties` hiện tại đã dùng environment variables, nhưng bạn có thể tạo `application-production.properties`:

```properties
# Production profile
spring.profiles.active=production

# Server
server.port=${PORT:8081}
server.address=0.0.0.0

# MongoDB - dùng từ environment variable
spring.data.mongodb.uri=${SPRING_DATA_MONGODB_URI}

# OAuth - dùng từ environment variables
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}
spring.security.oauth2.client.registration.facebook.client-id=${FACEBOOK_CLIENT_ID}
spring.security.oauth2.client.registration.facebook.client-secret=${FACEBOOK_CLIENT_SECRET}

# Mail
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}

# Frontend URL
frontend.base-url=${FRONTEND_BASE_URL:http://localhost:8082}
```

---

## ✅ Kiểm tra sau khi deploy

1. **Health Check**: Truy cập `https://your-app-url.com/api-docs` (Swagger UI)
2. **API Test**: Test các endpoint qua Swagger hoặc Postman
3. **Logs**: Kiểm tra logs trong dashboard để debug nếu có lỗi

---

## 🆘 Troubleshooting

### Lỗi: "Cannot connect to MongoDB"
- ✅ Kiểm tra MongoDB Atlas IP whitelist (phải có 0.0.0.0/0)
- ✅ Kiểm tra connection string có đúng password không
- ✅ Kiểm tra network access trong Atlas

### Lỗi: "Port already in use"
- ✅ Render: Dùng biến `PORT` (tự động set)
- ✅ Railway: Tương tự
- ✅ Fly.io: Cấu hình trong `fly.toml`

### Lỗi: "Out of memory"
- ✅ Tăng instance size (có thể mất phí)
- ✅ Hoặc optimize code, giảm dependencies

---

## 📝 Lưu ý

1. **Free tier có giới hạn**:
   - Render: 750 giờ/tháng, sleep sau 15 phút không dùng
   - Railway: $5 credit/tháng
   - Fly.io: 3 VMs nhỏ

2. **Database**: MongoDB Atlas free tier có 512MB storage (đủ cho dự án nhỏ)

3. **Secrets**: KHÔNG commit secrets vào code, luôn dùng environment variables

4. **HTTPS**: Tất cả platform đều tự động cung cấp HTTPS miễn phí

---

## 🎉 Kết quả

Sau khi deploy thành công, bạn sẽ có:
- ✅ URL public: `https://your-app-name.onrender.com`
- ✅ Có thể truy cập từ bất kỳ WiFi nào
- ✅ HTTPS tự động
- ✅ Tự động deploy khi push code lên GitHub

**Chúc bạn deploy thành công! 🚀**
