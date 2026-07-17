package com.migo.backend.controller;

import com.migo.backend.dto.request.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Object>> getMyInfo(
            // 🌟 Annotation này tự động bốc đối tượng User đang đăng nhập hợp lệ từ SecurityContext ra cho bạn dùng
            @AuthenticationPrincipal UserDetails userDetails) {
        
        ApiResponse<Object> apiResponse = new ApiResponse<>();
        
        if (userDetails == null) {
            apiResponse.setCode(1001);
            apiResponse.setMessage("Access token hết hạn hoặc không đúng");
            return ResponseEntity.status(403).body(apiResponse);
        }

        // Tạo map chứa thông tin trả về cho client hiển thị giống như ảnh mẫu
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("username", userDetails.getUsername());
        userInfo.put("authorities", userDetails.getAuthorities());

        apiResponse.setCode(1000); // Mã thành công mặc định của bạn
        apiResponse.setMessage("Lấy thông tin cá nhân thành công");
        apiResponse.setResult(userInfo);

        return ResponseEntity.ok(apiResponse);
    }
}