package com.migo.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    // 🌟 Thay "message" bằng tên chính xác của Enum Key trong ErrorCode.java
    @NotBlank(message = "USERNAME_INVALID") 
    @Size(min = 3, max = 30, message = "USERNAME_INVALID")
    @Pattern(regexp = "^[a-z0-9_.]+$", message = "USERNAME_INVALID")
    private String username;

    @NotBlank(message = "INVALID_KEY") // Nếu trống thì báo Invalid Key hoặc bạn tạo thêm một Enum EMAIL_INVALID
    @Email(message = "INVALID_KEY")
    private String email;

    @NotBlank(message = "INVALID_PASSWORD")
    @Size(min = 8, message = "INVALID_PASSWORD") 
    private String password;

    @NotBlank(message = "INVALID_KEY")
    private String displayName;
}