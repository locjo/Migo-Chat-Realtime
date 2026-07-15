package com.migo.backend.entity;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "sessions") // Ánh xạ với collection "sessions" giống bên Node.js
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    private String id;

    // Liên kết tới thực thể User (tương ứng với ref: 'User' bên Node.js)
    @DocumentReference(lazy = true)
    private User user;

    @Indexed(unique = true)
    private String token; 

    // Lưu chuỗi token và đánh chỉ mục (Unique) để tìm kiếm nhanh hơn, không trùng lặp
    @Indexed(unique = true)
    private String refreshToken;

    // Thời gian hết hạn của token (Nên có để quản lý session tốt hơn)
    @Field("expiry_date")
    @Indexed(expireAfter = "0s")
    private Instant expiryDate;
}