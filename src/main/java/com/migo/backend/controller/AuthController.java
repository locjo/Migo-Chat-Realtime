package com.migo.backend.controller;

import com.migo.backend.dto.request.ApiResponse;
import com.migo.backend.dto.request.LoginRequest;
import com.migo.backend.dto.request.RegisterRequest;
import com.migo.backend.dto.response.AuthResponse;
import com.migo.backend.entity.User;
import com.migo.backend.entity.RefreshToken;
import com.migo.backend.repository.UserRepository;
import com.migo.backend.repository.RefreshTokenRepository;
import com.migo.backend.security.JwtTokenProvider;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository; // Inject thêm Repository của Refresh Token

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private com.migo.backend.service.AuthService authService; // Inject Service của bạn để gọi

    // 1. ROUTE ĐĂNG KÝ (Giữ nguyên như cũ)
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername().trim().toLowerCase())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Username này đã được sử dụng!"));
        }
        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Email này đã được sử dụng!"));
        }

        User user = new User();
        user.setUsername(request.getUsername().trim().toLowerCase());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setDisplayName(request.getDisplayName());
        user.setHashedPassword(passwordEncoder.encode(request.getPassword()));
        
        User savedUser = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Đăng ký thành công!", "userId", savedUser.getId()));
    }

    // 2. ROUTE ĐĂNG NHẬP (Cập nhật trả về Access Token qua Body và Refresh Token qua Cookie)
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> loginUser(@Valid @RequestBody LoginRequest request) {
        
        // 1. Gọi Service thực hiện nghiệp vụ đăng nhập & sinh token
        AuthResponse authResponse = authService.login(request);

        // 2. Lấy Refresh Token vừa lưu dưới DB lên để bọc vào HttpOnly Cookie
        // (Tìm theo username vì mỗi user tại một thời điểm chỉ giữ 1 Refresh Token duy nhất)
        // 1. Tìm User từ database trước bằng username (vì authResponse có chứa username)
        User user = userRepository.findByUsername(authResponse.getUsername())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // 2. 🌟 Truyền trực tiếp đối tượng `user` vào hàm findByUser mới cập nhật
        RefreshToken refreshToken = refreshTokenRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Lỗi hệ thống khi khởi tạo phiên đăng nhập"));

        ResponseCookie cookie = jwtTokenProvider.createRefreshTokenCookie(refreshToken.getToken());

        // 3. Đóng gói kết quả gửi về cho Frontend
        ApiResponse<AuthResponse> apiResponse = new ApiResponse<>();
        apiResponse.setCode(1000);
        apiResponse.setMessage("Đăng nhập thành công!");
        apiResponse.setResult(authResponse);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString()) // Đính kèm cookie an toàn
                .body(apiResponse);
    }

    // 3. ROUTE ĐĂNG XUẤT (Xóa token dưới DB và xóa cookie ở trình duyệt)
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@CookieValue(name = "migo_refresh_token", required = false) String refreshTokenString) {
        
        if (refreshTokenString != null) {
            // Tìm và xóa Refresh Token này dưới MongoDB Atlas để vô hiệu hóa hoàn toàn
            refreshTokenRepository.findByToken(refreshTokenString)
                    .ifPresent(token -> refreshTokenRepository.delete(token));
        }

        // Tạo cookie rỗng có maxAge = 0 để xóa cookie phía Client
        ResponseCookie cleanCookie = jwtTokenProvider.cleanRefreshTokenCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cleanCookie.toString())
                .body(Map.of("message", "Đăng xuất thành công!"));
    }
}