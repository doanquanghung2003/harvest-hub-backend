# ⚙️ Hướng Dẫn Cấu Hình Render Sau Khi Deploy

## 📋 Sau khi build thành công

Khi bạn thấy status chuyển từ **"Building"** → **"Live"**, bạn cần cấu hình Environment Variables:

### Bước 1: Vào Settings
1. Trên dashboard Render, click **"Settings"** (bên trái)
2. Scroll xuống phần **"Environment Variables"**
3. Click **"Add Environment Variable"**

### Bước 2: Thêm các biến môi trường

#### 🔴 Bắt buộc (Phải có để app chạy):

**1. MongoDB Connection String:**
```
Key: SPRING_DATA_MONGODB_URI
Value: mongodb+srv://username:password@cluster0.xxxxx.mongodb.net/harvesthub?retryWrites=true&w=majority
```
*(Thay username, password, và cluster URL bằng giá trị từ MongoDB Atlas)*

**2. Java Version:**
```
Key: JAVA_VERSION
Value: 21
```

**3. Profile:**
```
Key: SPRING_PROFILES_ACTIVE
Value: production
```

#### 🟡 Tùy chọn (Nếu dùng tính năng này):

**OAuth - Google:**
```
Key: GOOGLE_CLIENT_ID
Value: your-google-client-id.apps.googleusercontent.com

Key: GOOGLE_CLIENT_SECRET
Value: your-google-client-secret
```

**OAuth - Facebook:**
```
Key: FACEBOOK_CLIENT_ID
Value: your-facebook-app-id

Key: FACEBOOK_CLIENT_SECRET
Value: your-facebook-app-secret
```

**Email (Gmail):**
```
Key: MAIL_USERNAME
Value: your-email@gmail.com

Key: MAIL_PASSWORD
Value: your-gmail-app-password
```
*(Lưu ý: Dùng App Password, không phải password thường - xem hướng dẫn tạo App Password bên dưới)*

**Frontend URL:**
```
Key: FRONTEND_BASE_URL
Value: https://your-frontend-url.com
```

**VNPay:**
```
Key: VNPAY_TMN_CODE
Value: your-tmn-code

Key: VNPAY_HASH_SECRET
Value: your-hash-secret

Key: VNPAY_RETURN_URL
Value: https://your-frontend-url.com/payment/return
```

### Bước 3: Save và Restart
1. Click **"Save Changes"** sau mỗi biến môi trường
2. Render sẽ tự động restart service
3. Đợi status chuyển về **"Live"**

---

## 🔍 Kiểm tra sau khi cấu hình

### 1. Kiểm tra logs
- Vào tab **"Logs"**
- Xem có lỗi không (màu đỏ)
- Tìm dòng "Started HarvestHubBackendApplication" = thành công

### 2. Test API
- Truy cập: `https://harvest-hub-backend-0po1.onrender.com/api-docs`
- Nếu thấy Swagger UI → ✅ API đã chạy!
- Nếu không → kiểm tra logs

### 3. Test Health Check
- Truy cập: `https://harvest-hub-backend-0po1.onrender.com/actuator/health`
- (Nếu có endpoint này)

---

## 🗄️ Tạo MongoDB Atlas (Nếu chưa có)

### 1. Đăng ký
- Truy cập: https://www.mongodb.com/cloud/atlas/register
- Đăng ký tài khoản miễn phí

### 2. Tạo Cluster
- Chọn **M0 Sandbox** (Free)
- Region: **Singapore** (gần VN nhất)
- Click **"Create Cluster"**
- Đợi 3-5 phút

### 3. Tạo Database User
- Vào **"Database Access"** → **"Add New Database User"**
- Authentication: **Password**
- Username: `harvesthub` (hoặc tên bạn muốn)
- Password: Tạo password mạnh (lưu lại!)
- Database User Privileges: **"Atlas admin"**
- Click **"Add User"**

### 4. Whitelist IP
- Vào **"Network Access"** → **"Add IP Address"**
- Chọn **"Allow Access from Anywhere"** (0.0.0.0/0)
- Click **"Confirm"**

### 5. Lấy Connection String
- Vào **"Database"** → Click **"Connect"**
- Chọn **"Connect your application"**
- Driver: **Java**
- Version: **5.5 or later**
- Copy connection string, ví dụ:
  ```
  mongodb+srv://harvesthub:<password>@cluster0.xxxxx.mongodb.net/?retryWrites=true&w=majority
  ```
- **Quan trọng**: Thay `<password>` bằng password bạn đã tạo ở bước 3
- Thêm database name vào cuối: `/harvesthub`
- Kết quả:
  ```
  mongodb+srv://harvesthub:yourpassword@cluster0.xxxxx.mongodb.net/harvesthub?retryWrites=true&w=majority
  ```

---

## 📧 Tạo Gmail App Password (Nếu dùng email)

1. Vào: https://myaccount.google.com/security
2. Bật **2-Step Verification** (nếu chưa bật)
3. Vào: https://myaccount.google.com/apppasswords
4. Select app: **"Mail"**
5. Select device: **"Other (Custom name)"** → nhập "Render"
6. Click **"Generate"**
7. Copy password 16 ký tự (ví dụ: `abcd efgh ijkl mnop`)
8. Dùng password này cho `MAIL_PASSWORD` (bỏ dấu cách: `abcdefghijklmnop`)

---

## 🆘 Xử lý lỗi thường gặp

### Lỗi: "Cannot connect to MongoDB"
- ✅ Kiểm tra MongoDB Atlas IP whitelist (phải có 0.0.0.0/0)
- ✅ Kiểm tra connection string có đúng password không
- ✅ Kiểm tra database name trong connection string
- ✅ Đảm bảo đã thay `<password>` trong connection string

### Lỗi: "Application failed to respond"
- ✅ Kiểm tra logs để xem lỗi cụ thể
- ✅ Kiểm tra Environment Variables đã set đúng chưa
- ✅ Kiểm tra PORT (Render tự động set, không cần set thủ công)

### Lỗi: "Out of memory"
- ✅ Free tier có giới hạn 512MB RAM
- ✅ Nếu lỗi này, có thể cần upgrade plan (mất phí)

### Service bị "Sleep"
- ✅ Free tier sẽ sleep sau 15 phút không có request
- ✅ Request đầu tiên sau khi sleep sẽ mất ~50 giây để wake up
- ✅ Đây là bình thường với free tier

---

## ✅ Checklist

- [ ] Build thành công (status = "Live")
- [ ] Đã tạo MongoDB Atlas cluster
- [ ] Đã set `SPRING_DATA_MONGODB_URI`
- [ ] Đã set `JAVA_VERSION=21`
- [ ] Đã set `SPRING_PROFILES_ACTIVE=production`
- [ ] Đã test API: `/api-docs`
- [ ] Logs không có lỗi
- [ ] (Nếu dùng OAuth) Đã set Google/Facebook credentials
- [ ] (Nếu dùng Email) Đã set Gmail App Password

---

## 🎉 Sau khi hoàn tất

URL của bạn: `https://harvest-hub-backend-0po1.onrender.com`

Bạn có thể:
- ✅ Truy cập từ bất kỳ WiFi nào
- ✅ Share URL với người khác
- ✅ Tích hợp với frontend
- ✅ Tự động deploy khi push code lên GitHub

**Chúc bạn deploy thành công! 🚀**
