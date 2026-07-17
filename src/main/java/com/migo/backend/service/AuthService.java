package com.migo.backend.service;



import com.migo.backend.dto.request.LoginRequest;
import com.migo.backend.dto.request.RegisterRequest;
import com.migo.backend.dto.response.AuthResponse;

public interface AuthService {
    
    // Hàm đăng ký tài khoản (Khớp với triển khai ở AuthServiceImpl của bạn)
    void register(RegisterRequest request);
    
    AuthResponse login(LoginRequest request);
}