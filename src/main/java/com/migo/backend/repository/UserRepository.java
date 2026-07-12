package com.migo.backend.repository;

import com.migo.backend.entity.User;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

@Repository // Đánh dấu đây là một Bean tầng Repository để Spring quản lý
public interface UserRepository extends MongoRepository<User, String> {

    // 1. Tìm kiếm người dùng bằng Username
    // Trả về Optional<User> giúp tránh lỗi NullPointerException (Tương đương findOne trong Node.js)
    Optional<User> findByUsername(String username);

    // 2. Tìm kiếm người dùng bằng Email
    Optional<User> findByEmail(String email);
    
    // 3. Kiểm tra nhanh xem Username đã tồn tại hay chưa (Trả về true/false)
    boolean existsByUsername(String username);

    // 4. Kiểm tra nhanh xem Email đã tồn tại hay chưa (Trả về true/false)
    boolean existsByEmail(String email);
}