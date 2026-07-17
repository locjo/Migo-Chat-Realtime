package com.migo.backend.dto.response;

import java.util.List;

import lombok.Data;

@Data
public class AuthResponse {
    private String accessToken;       // Token JWT dùng để đính kèm vào Header các request sau
    private String tokenType = "Bearer"; // Loại token mặc định
    private String username;          // Username để frontend hiển thị lời chào
    private String displayName;       // Tên hiển thị của người dùng (nếu có)
    private List<String> roles;       // Danh sách vai trò (ví dụ: ["ROLE_USER"]) để frontend ẩn/hiện nút Admin

    // Constructor không tham số (Bắt buộc cho việc serialize JSON)
    public AuthResponse() {
    }

    // Constructor đầy đủ tham số để khởi tạo nhanh
    public AuthResponse(String accessToken, String username, String displayName, List<String> roles) {
        this.accessToken = accessToken;
        this.username = username;
        this.displayName = displayName;
        this.roles = roles;
    }

    
}