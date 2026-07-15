package com.migo.backend.controller;

import com.migo.backend.dto.request.LoginRequest;
import com.migo.backend.dto.request.RegisterRequest;
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
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest request) {
        String username = request.getUsername().trim().toLowerCase();
        String password = request.getPassword();

        return userRepository.findByUsername(username)
                .map(user -> {
                    if (passwordEncoder.matches(password, user.getHashedPassword())) {
                        
                        // 2.1 Sinh Access Token (hết hạn nhanh)
                        String accessToken = jwtTokenProvider.generateToken(user.getUsername());
                        
                        // 2.2 Tạo Refresh Token và lưu vào MongoDB Atlas
                        RefreshToken refreshToken = new RefreshToken();
                        refreshToken.setToken(UUID.randomUUID().toString()); // Tạo chuỗi ngẫu nhiên duy nhất
                        refreshToken.setUser(user);
                        refreshToken.setExpiryDate(Instant.now().plusSeconds(7 * 24 * 60 * 60)); // Hết hạn sau 7 ngày
                        
                        // Xóa các Refresh Token cũ của user này trước khi lưu token mới (tránh rác DB)
                        refreshTokenRepository.deleteByUser(user);
                        refreshTokenRepository.save(refreshToken);

                        // 2.3 Bọc Refresh Token vào HttpOnly Cookie
                        ResponseCookie cookie = jwtTokenProvider.createRefreshTokenCookie(refreshToken.getToken());

                        // 2.4 Đóng gói dữ liệu trả về cho Frontend
                        Map<String, Object> response = new HashMap<>();
                        response.put("message", "Đăng nhập thành công!");
                        response.put("username", user.getUsername());
                        response.put("displayName", user.getDisplayName());
                        response.put("accessToken", accessToken); // 👈 Trả thẳng Access Token trong JSON

                        return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, cookie.toString()) // 👈 Dập cookie vào header
                                .body(response);
                    } else {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Mật khẩu không chính xác!"));
                    }
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Tài khoản không tồn tại!")));
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