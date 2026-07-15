package com.migo.backend.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.migo.backend.dto.request.RegisterRequest; // Hoặc tên Request của bạn
import com.migo.backend.entity.User;
import com.migo.backend.exception.AppException;
import com.migo.backend.exception.ErrorCode;
import com.migo.backend.repository.UserRepository;

@Service
public class AuthServiceImpl {

    @Autowired
    private UserRepository userRepository;

    // Giả sử đây là hàm đăng ký tài khoản của bạn
    public void register(RegisterRequest request) {

        // 1. ĐẶT CODE KIỂM TRA TRÙNG Ở NGAY ĐẦU HÀM XỬ LÝ
        // Dùng hàm existsBy để kiểm tra trùng nhanh và tối ưu bộ nhớ
        if (userRepository.existsByUsername(request.getUsername().trim().toLowerCase())) {
            throw new AppException(ErrorCode.USERNAME_EXISTED);
        }

        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }

        // 2. NẾU KHÔNG TRÙNG THÌ MỚI CHẠY TIẾP LOGIC DƯỚI ĐÂY
        User newUser = new User();
        newUser.setUsername(request.getUsername().trim().toLowerCase());
        newUser.setEmail(request.getEmail().trim().toLowerCase());
        // mã hóa mật khẩu, set role...
        
        userRepository.save(newUser);
    }
}