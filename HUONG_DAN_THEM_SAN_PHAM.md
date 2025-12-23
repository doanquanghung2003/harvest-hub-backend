# Hướng dẫn thêm sản phẩm mẫu

## Cách 1: Sử dụng endpoint test (Đơn giản nhất)

Tôi đã tạo một endpoint test đơn giản để thêm sản phẩm mẫu. Bạn có thể:

### Option A: Chạy file batch
```bash
# Chạy file batch script
add_sample_product.bat
```

### Option B: Sử dụng PowerShell
```powershell
Invoke-RestMethod -Uri 'http://localhost:8081/api/products/test-add-sample' -Method POST -ContentType 'application/json'
```

### Option C: Sử dụng trình duyệt hoặc Postman
- **URL**: `http://localhost:8081/api/products/test-add-sample`
- **Method**: POST
- **Headers**: `Content-Type: application/json`
- **Body**: (không cần body)

Endpoint này sẽ tự động tạo một sản phẩm mẫu với thông tin:
- Tên: "Bông cải xanh tươi"
- Danh mục: "Rau Củ"
- Giá: 20,000 VND
- Tồn kho: 123
- Seller ID: "hungbanhang"

## Cách 2: Sử dụng script Python (Nếu có Python)

1. Cài đặt Python (nếu chưa có)
2. Cài đặt requests: `pip install requests`
3. Sửa thông tin đăng nhập trong file `add_product_test.py`:
   ```python
   LOGIN_USERNAME = "your_username"
   LOGIN_PASSWORD = "your_password"
   ```
4. Chạy script:
   ```bash
   python add_product_test.py
   ```

## Cách 3: Sử dụng API thông thường (Với authentication)

Nếu bạn muốn thêm sản phẩm với đầy đủ tính năng (bao gồm upload ảnh), sử dụng endpoint `/api/products/upload`:

1. Đăng nhập để lấy token:
   ```bash
   POST http://localhost:8081/api/auth/login
   Body: {
     "username": "your_username",
     "password": "your_password"
   }
   ```

2. Tạo sản phẩm với token:
   ```bash
   POST http://localhost:8081/api/products/upload
   Headers: {
     "Authorization": "Bearer YOUR_TOKEN"
   }
   Body: FormData với các trường:
   - name
   - description
   - shortDescription
   - category
   - price
   - stock
   - weight
   - unit
   - origin
   - expiryDate
   - storageInstructions
   - specifications (JSON string)
   - images (file)
   - detailImages (file)
   - sellerId
   ```

## Lưu ý

- Đảm bảo backend đang chạy trên port 8081
- Endpoint test (`/test-add-sample`) không cần authentication, nhưng chỉ nên dùng cho development
- Sản phẩm được tạo sẽ có status "active" và approvalStatus "pending" (chờ duyệt)

