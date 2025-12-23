@echo off
echo ========================================
echo THEM NHIEU SAN PHAM TRAI CAY VIET NAM
echo ========================================
echo.

set /p USERNAME="Nhap username cua seller (vi du: hungbanhang): "

if "%USERNAME%"=="" (
    echo Loi: Vui long nhap username!
    pause
    exit /b 1
)

echo.
echo Dang them cac san pham trai cay Viet Nam cho seller: %USERNAME%
echo.

powershell -Command "Invoke-RestMethod -Uri 'http://localhost:8081/api/products/test-add-vietnam-fruits?username=%USERNAME%' -Method POST -ContentType 'application/json' | ConvertTo-Json -Depth 10"

echo.
echo ========================================
echo Hoan thanh!
echo ========================================
pause

