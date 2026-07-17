package com.migo.backend.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;

@Data // Tự động sinh các hàm Getter, Setter, toString từ thư viện Lombok
@Document(collection = "users") // Định nghĩa tên Collection trong MongoDB Atlas
public class User {

    @Id // Đánh dấu đây là Khóa chính (pk) của bảng, Spring tự map với trường _id kiểu String của MongoDB
    private String id;

    @Indexed(unique = true) // Đảm bảo không bao giờ có 2 tài khoản trùng Username dưới Database
    private String username;

    @Field("hashedPassword") // Đặt tên trường rõ ràng trong DB
    private String hashedPassword;

    @Field("displayName")
    private String displayName;

    @Indexed(unique = true) // Đảm bảo email là duy nhất dưới DB
    private String email;

    @Field("role")
    private String role = "USER";

    @Field("avatarUrl")
    private String avatarUrl;

    @Field("avatarId")
    private String avatarId;

    private String bio;

    @Indexed(unique = true, sparse = true) 
    // sparse = true giúp tài khoản nào không nhập phone vẫn lưu được (null), nhưng nếu đã nhập thì không được trùng nhau
    @Field("phoneNumber")
    private String phoneNumber;

    @CreatedDate // Tự động ghi nhận thời gian hệ thống khi tài khoản được tạo mới
    @Field("createAt")
    private LocalDateTime createAt;

    @LastModifiedDate // Tự động cập nhật thời gian hệ thống mỗi khi sửa profile
    @Field("updateAt")
    private LocalDateTime updateAt;
}