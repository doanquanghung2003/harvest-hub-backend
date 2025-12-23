#!/usr/bin/env python3
"""
Script để thêm sản phẩm mẫu vào hệ thống
"""
import requests
import json
import os
from pathlib import Path

# Cấu hình
API_BASE_URL = "http://localhost:8081"
LOGIN_USERNAME = "hungbanhang"  # Thay đổi username của bạn
LOGIN_PASSWORD = "your_password"  # Thay đổi password của bạn

# Dữ liệu sản phẩm mẫu
PRODUCT_DATA = {
    "name": "Bông cải xanh tươi",
    "shortDescription": "Súp lơ xanh tươi ngon",
    "description": "Bông cải xanh là một loại rau thuộc họ cải, được sử dụng làm thực phẩm, thường được luộc hoặc hấp, nhưng cũng có thể ăn sống trong salad.",
    "category": "Rau Củ",
    "price": 20000,
    "stock": 123,
    "weight": "1",
    "unit": "kg",
    "origin": "VN",
    "expiryDate": "1 tháng",
    "storageInstructions": "Bảo quản nơi khô ráo và thoáng mát",
    "status": "active"
}

# Specifications (sẽ được gửi dưới dạng JSON string)
SPECIFICATIONS = {
    "Kích thước": "10x10",
    "Thành phần": "Rau xanh",
    "Thương hiệu": "Việt Grap"
}

def login():
    """Đăng nhập để lấy token"""
    url = f"{API_BASE_URL}/api/auth/login"
    data = {
        "username": LOGIN_USERNAME,
        "password": LOGIN_PASSWORD
    }
    
    print(f"Đang đăng nhập với username: {LOGIN_USERNAME}...")
    response = requests.post(url, json=data)
    
    if response.status_code == 200:
        result = response.json()
        token = result.get("token")
        print(f"✅ Đăng nhập thành công!")
        return token
    else:
        print(f"❌ Lỗi đăng nhập: {response.status_code}")
        print(response.text)
        return None

def get_image_path():
    """Lấy đường dẫn đến một ảnh mẫu"""
    uploads_dir = Path("D:/harvest-hub-backend/uploads/products")
    if uploads_dir.exists():
        images = list(uploads_dir.glob("*.jpg")) + list(uploads_dir.glob("*.png"))
        if images:
            return str(images[0])
    
    # Nếu không có ảnh, tạo một file ảnh giả (1x1 pixel PNG)
    print("⚠️ Không tìm thấy ảnh mẫu, sẽ tạo ảnh giả...")
    return None

def create_product(token):
    """Tạo sản phẩm mới"""
    url = f"{API_BASE_URL}/api/products/upload"
    
    # Tạo FormData
    files = []
    form_data = {}
    
    # Thêm các trường text
    for key, value in PRODUCT_DATA.items():
        form_data[key] = str(value)
    
    # Thêm sellerId (có thể lấy từ token hoặc hardcode)
    form_data["sellerId"] = LOGIN_USERNAME  # Hoặc lấy từ user info
    
    # Thêm specifications dưới dạng JSON string
    form_data["specifications"] = json.dumps(SPECIFICATIONS)
    
    # Thêm ảnh
    image_path = get_image_path()
    if image_path and os.path.exists(image_path):
        with open(image_path, 'rb') as f:
            files.append(('images', (os.path.basename(image_path), f.read(), 'image/jpeg')))
        print(f"✅ Đã thêm ảnh: {image_path}")
    else:
        print("⚠️ Không có ảnh để upload")
    
    headers = {
        "Authorization": f"Bearer {token}"
    }
    
    print("\n=== Đang tạo sản phẩm ===")
    print(f"URL: {url}")
    print(f"Product name: {PRODUCT_DATA['name']}")
    print(f"Category: {PRODUCT_DATA['category']}")
    print(f"Price: {PRODUCT_DATA['price']}")
    
    try:
        if files:
            # Có ảnh - dùng multipart/form-data
            response = requests.post(url, headers=headers, data=form_data, files=files)
        else:
            # Không có ảnh - chỉ gửi form data
            response = requests.post(url, headers=headers, data=form_data)
        
        print(f"\nStatus Code: {response.status_code}")
        
        if response.status_code == 200:
            result = response.json()
            print("✅ Sản phẩm đã được tạo thành công!")
            print(f"Product ID: {result.get('product', {}).get('id', 'N/A')}")
            print(f"Message: {result.get('message', 'N/A')}")
            return True
        else:
            print(f"❌ Lỗi khi tạo sản phẩm: {response.status_code}")
            try:
                error = response.json()
                print(f"Error: {error}")
            except:
                print(f"Response: {response.text}")
            return False
            
    except Exception as e:
        print(f"❌ Exception: {str(e)}")
        import traceback
        traceback.print_exc()
        return False

def main():
    print("=" * 50)
    print("SCRIPT THÊM SẢN PHẨM MẪU")
    print("=" * 50)
    
    # Đăng nhập
    token = login()
    if not token:
        print("\n❌ Không thể đăng nhập. Vui lòng kiểm tra:")
        print("  1. Backend đang chạy trên port 8081")
        print("  2. Username và password đúng")
        print("  3. User có quyền thêm sản phẩm")
        return
    
    # Tạo sản phẩm
    success = create_product(token)
    
    if success:
        print("\n✅ Hoàn thành!")
    else:
        print("\n❌ Thất bại!")

if __name__ == "__main__":
    main()

