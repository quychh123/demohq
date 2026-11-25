# Server Đồng Bộ Render (HQ Apple ↔ Kho Hàng Min)

Thư mục này chứa backend Node.js để triển khai lên **Render.com** giúp 2 ứng dụng **Kho Hàng Min** và **HQ Apple** đăng ký, đăng nhập và đồng bộ dữ liệu tổng (`.hqbak`) lẫn nhau qua Internet.

## Triển khai lên Render
1. Đưa thư mục `render-server` lên GitHub (hoặc deploy từ repo hiện tại với Root Directory = `render-server`).
2. Trên Render: chọn **New + -> Web Service** (hoặc **Blueprint** dùng `render.yaml`).
   - Build Command: `npm install`
   - Start Command: `npm start`
   - Environment Variable: `SESSION_SECRET` (tự sinh chuỗi bảo mật ngẫu nhiên).
3. Sau khi Render deploy xong, copy URL (dạng `https://ten-app.onrender.com`) và dán vào mục **Đồng bộ -> URL Server Render** trong cả 2 ứng dụng **Kho Hàng Min** và **HQ Apple**.
