package com.migo.backend.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component // Đánh dấu là 1 Bean để Spring Security có thể nhúng vào file SecurityConfig
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider jwtTokenProvider; // Dùng để giải mã và validate token

    @Autowired
    private CustomUserDetailsService customUserDetailsService; // Dùng để nạp thông tin User vào hệ thống bảo mật

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 1. Lấy chuỗi mã hóa JWT từ Header của request gửi lên
            String jwt = getJwtFromRequest(request);

            // 2. Kiểm tra xem Token có tồn tại và hợp lệ không
            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
                
                // 3. Nếu token hợp lệ, bóc tách lấy username ra
                String username = jwtTokenProvider.getUsernameFromJWT(jwt);

                // 4. Lấy toàn bộ thông tin chi tiết của User (Quyền hạn, mật khẩu...) dựa vào username
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
                
                if (userDetails != null) {
                    // 5. Tạo một chứng chỉ xác thực hợp lệ của Spring Security
                    UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 6. Đóng dấu "ĐÃ XÁC THỰC THÀNH CÔNG" vào hệ thống (SecurityContext)
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception ex) {
            // Ghi log nếu có lỗi xảy ra trong quá trình xác thực
            logger.error("Không thể xác thực người dùng trong Security Context", ex);
        }

        // 7. Cho phép request tiếp tục đi tiếp đến Controller
        filterChain.doFilter(request, response);
    }

    // --- HÀM BỔ TRỢ: BÓC TÁCH CHUỖI TOKEN TỪ HEADER "Authorization: Bearer <token>" ---
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        // Theo tiêu chuẩn, Token gửi lên phải bắt đầu bằng chữ "Bearer "
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Cắt bỏ 7 ký tự đầu "Bearer " để lấy đúng chuỗi mã hóa lõi
        }
        return null;
    }
}