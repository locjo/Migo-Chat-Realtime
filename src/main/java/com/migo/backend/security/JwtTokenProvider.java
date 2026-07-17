package com.migo.backend.security;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component // Đánh dấu là 1 Bean để Spring quản lý và cho phép @Autowired ở AuthController
public class JwtTokenProvider {

    // 1. Spring tự động nạp chuỗi secret từ file cấu hình vào đây
    @Value("${jwt.secret}")
    private String secretString;

    private Key SECRET_KEY;

    // 2. Hàm này chạy ngay sau khi Bean được khởi tạo để chuyển chuỗi string thành Key Object
    @PostConstruct
    public void init() {
        this.SECRET_KEY = Keys.hmacShaKeyFor(Decoders.BASE64.decode(
                io.jsonwebtoken.io.Encoders.BASE64.encode(secretString.getBytes())
        ));
    }
    // 2. Cấu hình thời gian sống của Access Token (1 giờ tính bằng mili giây)
    // Access Token nên có thời gian sống ngắn để giảm thiểu rủi ro nếu bị lộ
    private final long ACCESS_TOKEN_EXPIRATION = 1 * 60 * 60 * 1000; 

    // --- HÀM 1: SINH ACCESS TOKEN ---
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION);

        return Jwts.builder()
                .setSubject(username) // Lưu thông tin định danh (username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SECRET_KEY)
                .compact(); // Trả về chuỗi JWT String (Header.Payload.Signature)
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
                    .parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            // Token sai chữ ký, hết hạn hoặc không đúng định dạng
            return false;
        }
    }

    // --- HÀM 4: TẠO HTTPONLY COOKIE CHỨA REFRESH TOKEN ---
    public ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from("migo_refresh_token", refreshToken)
                .path("/api/auth")                    // Chỉ gửi cookie này khi gọi các API trong cụm /api/auth (như refresh, logout)
                .maxAge(7 * 24 * 60 * 60)             // Thời gian sống: 7 ngày (tính bằng giây)
                .httpOnly(true)                       // Khóa chặt JavaScript không cho đọc trộm (Chống XSS)
                .secure(false)                        // Để 'false' khi test localhost (http), đổi thành 'true' khi deploy (https)
                .sameSite("Lax")                      // Ngăn chặn tấn công CSRF chéo trang hiệu quả trên Web
                .build();
    }

    // --- HÀM 5: TẠO COOKIE RỖNG ĐỂ XÓA REFRESH TOKEN KHI LOGOUT ---
    public ResponseCookie cleanRefreshTokenCookie() {
        return ResponseCookie.from("migo_refresh_token", "")
                .path("/api/auth")
                .maxAge(0)                            // Đặt thời gian sống bằng 0 để trình duyệt tự hủy ngay lập tức
                .httpOnly(true)
                .build();
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