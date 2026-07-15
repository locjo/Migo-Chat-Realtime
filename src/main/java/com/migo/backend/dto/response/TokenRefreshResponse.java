package com.migo.backend.dto.response;

import lombok.Data;

@Data
public class TokenRefreshResponse {
    private String accessToken;
    private String tokenType = "Bearer"; // Định dạng chuẩn để Frontend biết đây là chuỗi Bearer Token

    // Hàm khởi tạo không tham số (Bắt buộc phải có để Spring Boot tự động chuyển đổi JSON)
    public TokenRefreshResponse() {
    }

    // Hàm khởi tạo nhanh với tham số accessToken
    public TokenRefreshResponse(String accessToken) {
        this.accessToken = accessToken;
    }

    
}