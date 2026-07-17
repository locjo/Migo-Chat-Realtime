package com.migo.backend.repository;

import com.migo.backend.entity.RefreshToken;
import com.migo.backend.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository // Đánh dấu để Spring Boot tự động phát hiện và quản lý Bean này
public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {

    // 1. Tìm kiếm Refresh Token dưới Database (Dùng khi người dùng Logout hoặc đổi Access Token mới)
    Optional<RefreshToken> findByToken(String token);

    // 2. Xóa toàn bộ các Refresh Token liên kết với một User cụ thể
    // Hàm này cực kỳ quan trọng để đảm bảo mỗi user chỉ có duy nhất 1 phiên đăng nhập hợp lệ (tránh rác DB)
    void deleteByUser(User user);

    Optional<RefreshToken> findByUser(User user);
}