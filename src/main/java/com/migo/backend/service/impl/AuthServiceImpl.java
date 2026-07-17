package com.migo.backend.service.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // 🌟 Thêm mã hóa mật khẩu
import org.springframework.stereotype.Service;

import com.migo.backend.dto.request.LoginRequest;
import com.migo.backend.dto.request.RegisterRequest;
import com.migo.backend.dto.response.AuthResponse;
import com.migo.backend.entity.RefreshToken;
import com.migo.backend.entity.User;
import com.migo.backend.exception.AppException;
import com.migo.backend.exception.ErrorCode;
import com.migo.backend.repository.RefreshTokenRepository;
import com.migo.backend.repository.UserRepository;
import com.migo.backend.security.JwtTokenProvider;
import com.migo.backend.service.AuthService; // 🌟 Import interface của bạn
@Service
public class AuthServiceImpl implements AuthService { // 🌟 BẮT BUỘC phải implements ở đây

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // 🌟 Tiêm Bean mã hóa mật khẩu đã cấu hình ở SecurityConfig

    @Autowired
    private RefreshTokenRepository refreshTokenRepository; // 🌟 Tiêm Repository của Refresh Token

    @Autowired
    private JwtTokenProvider jwtTokenProvider; // 🌟 Tiêm Bean JwtTokenProvider để

    @Override // 🌟 Đánh dấu override phương thức từ interface AuthService
    public void register(RegisterRequest request) {

        // 1. Kiểm tra trùng username và email (Giữ nguyên logic chuẩn của bạn)
        if (userRepository.existsByUsername(request.getUsername().trim().toLowerCase())) {
            throw new AppException(ErrorCode.USERNAME_EXISTED);
        }

        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }

        // 2. Tiến hành tạo mới User
        User newUser = new User();
        newUser.setUsername(request.getUsername().trim().toLowerCase());
        newUser.setEmail(request.getEmail().trim().toLowerCase());
        
        // 🌟 Mã hóa mật khẩu từ request trước khi lưu xuống MongoDB
        String encryptedPassword = passwordEncoder.encode(request.getPassword());
        newUser.setHashedPassword(encryptedPassword);
        
        // 🌟 Gán vai trò mặc định (Mặc dù trong Entity đã có mặc định là "USER", 
        // nhưng viết tường minh ở đây giúp code rõ ràng hơn)
        newUser.setRole("USER"); 

        // 3. Lưu xuống MongoDB Atlas (createdAt và updatedAt sẽ tự sinh nhờ @EnableMongoAuditing)
        userRepository.save(newUser);
    }
    @Override
    public AuthResponse login(LoginRequest request) {
        String username = request.getUsername().trim().toLowerCase();
        String password = request.getPassword();

        // 1. Tìm kiếm User
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USERNAME_INVALID)); // Hoặc lỗi tương ứng của bạn

        // 2. Kiểm tra mật khẩu
        if (!passwordEncoder.matches(password, user.getHashedPassword())) {
            throw new AppException(ErrorCode.INVALID_PASSWORD); // Hoặc lỗi tương ứng của bạn
        }

        // 3. Sinh Access Token
        String accessToken = jwtTokenProvider.generateToken(user.getUsername());

        // 4. Sinh & Lưu Refresh Token
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusSeconds(7 * 24 * 60 * 60));

        refreshTokenRepository.deleteByUser(user);
        refreshTokenRepository.save(refreshToken);

        // 🌟 5. Trả về đối tượng AuthResponse chuẩn hóa chứa thông tin cần thiết
        // Danh sách Role được bọc trong SingletonList để chuyển sang AuthResponse
        return new AuthResponse(
                accessToken,
                user.getUsername(),
                user.getDisplayName(),
                Collections.singletonList(user.getRole() != null ? user.getRole() : "USER")
        );
    }
}