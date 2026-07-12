package com.migo.backend.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.migo.backend.dto.request.LoginRequest;
import com.migo.backend.dto.request.RegisterRequest;
import com.migo.backend.entity.User;
import com.migo.backend.repository.UserRepository;
import com.migo.backend.security.JwtTokenProvider;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // Tiêm từ SecurityConfig để mã hóa mật khẩu

    @Autowired
    private JwtTokenProvider jwtTokenProvider; // Tiêm từ thư mục security để sinh Token thật

    // 1. ROUTE ĐĂNG KÝ TÀI KHOẢN (Sử dụng RegisterRequest)
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request) {
        
        // Dùng hàm existsBy để kiểm tra trùng nhanh và tối ưu bộ nhớ
        if (userRepository.existsByUsername(request.getUsername().trim().toLowerCase())) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Username này đã được sử dụng!");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Email này đã được sử dụng!");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Chuyển đổi dữ liệu từ DTO Request sang Entity để lưu xuống MongoDB
        User user = new User();
        user.setUsername(request.getUsername().trim().toLowerCase());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setDisplayName(request.getDisplayName());
        
        // 🔒 Thực hiện băm mật khẩu bằng BCrypt trước khi lưu
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        user.setHashedPassword(encodedPassword); 

        // Lưu vào MongoDB Atlas
        User savedUser = userRepository.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Đăng ký tài khoản thành công!");
        response.put("userId", savedUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2. ROUTE ĐĂNG NHẬP (Sử dụng LoginRequest)
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest request) {
        String username = request.getUsername().trim().toLowerCase();
        String password = request.getPassword();

        // Tìm kiếm User theo username trong DB
        return userRepository.findByUsername(username)
                .map(user -> {
                    // 🔒 Sử dụng passwordEncoder.matches để đối chiếu mật khẩu đã băm
                    if (passwordEncoder.matches(password, user.getHashedPassword())) {
                        
                        // 🔑 Sinh ra chuỗi JWT Token xịn từ file JwtTokenProvider của bạn
                        String token = jwtTokenProvider.generateToken(user.getUsername());
                        
                        Map<String, Object> response = new HashMap<>();
                        response.put("message", "Đăng nhập thành công!");
                        response.put("username", user.getUsername());
                        response.put("displayName", user.getDisplayName());
                        response.put("token", token); // Trả token thật về cho Flutter/React
                        
                        return ResponseEntity.ok(response);
                    } else {
                        Map<String, String> response = new HashMap<>();
                        response.put("error", "Mật khẩu không chính xác!");
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
                    }
                })
                .orElseGet(() -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("error", "Tài khoản không tồn tại!");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                });
    }
    // 3. ROUTE ĐĂNG XUẤT TÀI KHOẢN (POST: /api/auth/logout)
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@RequestBody Map<String, String> logoutRequest) {
        String username = logoutRequest.get("username");
        
        if (username != null) {
            // 🛠️ HÀNH ĐỘNG HỦY PHIÊN:
            // Bạn sẽ gọi xuống RefreshTokenRepository (hoặc Service) để xóa bỏ bản ghi token của user này dưới DB.
            // Ví dụ: refreshTokenService.deleteByUsername(username);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Đăng xuất thành công! Hãy xóa token ở phía Frontend.");
            return ResponseEntity.ok(response);
        }

        Map<String, String> response = new HashMap<>();
        response.put("error", "Yêu cầu đăng xuất không hợp lệ!");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}