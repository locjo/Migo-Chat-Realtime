package com.migo.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.migo.backend.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity // Kích hoạt tính năng bảo mật Spring Security
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // Tiêm bộ lọc Jwt Filter vào để cấu hình chuỗi bảo mật
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    // 1. CẤU HÌNH THUẬT TOÁN MÃ HÓA MẬT KHẨU (Sử dụng BCrypt)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. CẤU HÌNH QUẢN LÝ XÁC THỰC (Dùng cho tầng Service/Controller gọi xác thực thủ công nếu cần)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // 3. CHUỖI BỘ LỌC BẢO MẬT CHÍNH (Security Filter Chain)
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Đóng tính năng chống giả mạo request CSRF vì ứng dụng của ta chạy REST API độc lập
            .csrf(csrf -> csrf.disable()) // Rất quan trọng khi test Postman
            
            // Cấu hình phân quyền truy cập cho các URL
            .authorizeHttpRequests(auth -> auth
                // Cho phép mở tự do hoàn toàn các API liên quan đến Login/Register (Ai cũng vào được)
                .requestMatchers("/api/auth/**").permitAll()

                // 2. Chỉ tài khoản ADMIN mới được truy cập các API bắt đầu bằng /api/admin/**
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // 3. Tài khoản ADMIN hoặc USER đều có thể vào cụm /api/chat/**
                .requestMatchers("/api/chat/**").hasAnyRole("USER", "ADMIN")

                .requestMatchers("/api/users/me").authenticated() // 🌟 BẮT BUỘC ĐÃ ĐĂNG NHẬP (Phải gửi kèm Token)
                
                // ⚠️ QUAN TRỌNG: Mở tự do cho cổng kết nối Socket.IO (Thường là đường dẫn có chứa socket.io)
                .requestMatchers("/socket.io/**").permitAll()
                
                // Tất cả các API còn lại (lấy tin nhắn, thông tin user...) bắt buộc phải đăng nhập (có token)
                .anyRequest().authenticated()
            )
            
            // Ép Spring Security chạy ở chế độ STATELESS (Không lưu phiên làm việc bằng Session/Cookie)
            // Vì ta đang xác thực bằng cơ chế JWT Token
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Đút bộ lọc JwtAuthenticationFilter vào đứng TRƯỚC bộ lọc mặc định của Spring Security
            // Để nó check token và giải mã trước khi hệ thống ra quyết định cho đi qua hay không
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}