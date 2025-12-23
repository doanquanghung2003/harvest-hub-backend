@echo off
echo ========================================
echo THEM SAN PHAM MAU
echo ========================================
echo.

echo Dang goi API de them san pham mau...
echo.

powershell -Command "Invoke-RestMethod -Uri 'http://localhost:8081/api/products/test-add-sample' -Method POST -ContentType 'application/json' | ConvertTo-Json -Depth 10"

echo.
echo ========================================
echo Hoan thanh!
echo ========================================
pause

