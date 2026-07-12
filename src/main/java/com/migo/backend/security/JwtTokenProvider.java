package com.migo.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Component // Đánh dấu là 1 Bean để Spring quản lý và cho phép @Autowired ở AuthController
public class JwtTokenProvider {

    // 1. Tạo khóa bí mật an toàn cho thuật toán HS256
    // Ghi chú: Trong dự án thực tế, bạn nên cấu hình chuỗi Secret Key cố định trong file application.yml
    private final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // 2. Cấu hình thời gian sống của Token (Ví dụ: 24 giờ tính bằng mili giây)
    private final long JWT_EXPIRATION = 24 * 60 * 60 * 1000;

    // --- HÀM 1: SINH TOKEN KHI ĐĂNG NHẬP THÀNH CÔNG ---
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + JWT_EXPIRATION);

        return Jwts.builder()
                .setSubject(username) // Lưu thông tin định danh (username) vào lõi token
                .setIssuedAt(now) // Thời gian tạo
                .setExpiration(expiryDate) // Thời gian hết hạn
                .signWith(SECRET_KEY) // Ký tên mã hóa bằng khóa bí mật
                .compact(); // Băm thành chuỗi String dạng: Header.Payload.Signature
    }

    // --- HÀM 2: LẤY USERNAME TỪ TRONG CHUỖI TOKEN (GIẢI MÃ) ---
    public String getUsernameFromJWT(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // --- HÀM 3: KIỂM TRA TOKEN CÓ HỢP LỆ KHÔNG ---
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token); // Nếu token bị sửa đổi hoặc hết hạn, hàm này sẽ tự ném ra ngoại lệ
            return true;
        } catch (Exception ex) {
            // Các ngoại lệ có thể xảy ra: ExpiredJwtException, MalformedJwtException, SignatureException...
            return false;
        }
    }

    // --- CÁC HÀM BỔ TRỢ BÓC TÁCH DỮ LIỆU NGẦM (CLAIMS) ---
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}