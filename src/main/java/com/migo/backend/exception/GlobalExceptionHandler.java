package com.migo.backend.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.migo.backend.dto.request.ApiResponse;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 2. BẮT LỖI NGHIỆP VỤ TỰ ĐỊNH NGHĨA (AppException)
     * Xử lý các lỗi do bạn chủ động ném ra ở tầng Service (Ví dụ: trùng Email, trùng Username).
     * Tự động lấy Code và Message từ ErrorCode Enum tương ứng được truyền vào.
     */
    @ExceptionHandler(value = AppException.class)
    public ResponseEntity<ApiResponse<Object>> handlingAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        ApiResponse<Object> apiResponse = new ApiResponse<>();

        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());
        
        return ResponseEntity.badRequest().body(apiResponse);
    }

    /**
     * 3. BẮT LỖI VALIDATION DATA ĐẦU VÀO (@Valid)
     * Xử lý khi dữ liệu Client gửi lên vi phạm các Annotation như @NotBlank, @Size, @Min...
     * Cơ chế: Lấy chuỗi message ở DTO làm Key để tra cứu ngược lại trong ErrorCode Enum.
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handlingValidation(MethodArgumentNotValidException exception) {
        // Lấy thông báo lỗi mặc định được định nghĩa ở thuộc tính DTO
        String enumKey = exception.getFieldError().getDefaultMessage();

        // Mặc định nếu không tìm thấy key phù hợp trong Enum
        ErrorCode errorCode = ErrorCode.INVALID_KEY;

        try {
            // Chuyển chuỗi String (enumKey) thành đối tượng Enum tương ứng
            errorCode = ErrorCode.valueOf(enumKey);
        } catch (IllegalArgumentException e) {
            // Nếu viết sai tên Key ở DTO dẫn đến không tìm thấy trong Enum, code rơi vào đây và giữ nguyên INVALID_KEY
        }
        
        ApiResponse<Object> apiResponse = new ApiResponse<>();
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());

        return ResponseEntity.badRequest().body(apiResponse);
    }

    
}