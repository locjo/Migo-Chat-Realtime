package com.migo.backend.security;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.migo.backend.entity.User;
import com.migo.backend.repository.UserRepository;

@Service // Đánh dấu là 1 Service để Spring Security tự động nhận diện và sử dụng
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository; // Tiêm UserRepository để truy vấn MongoDB Atlas

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Xuống MongoDB tìm User theo Username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với username: " + username));

        // 2. Lấy role từ DB và thêm tiền tố "ROLE_" cho đúng chuẩn Spring Security
        // Nếu user.getRole() chưa có hoặc null, mặc định sẽ gán vai trò là "USER"
        String roleName = user.getRole() != null ? user.getRole() : "USER";
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + roleName);


        // 3. Chuyển đổi Object User (của MongoDB) thành Object UserDetails (của Spring Security)
        // Cấu trúc mặc định của Spring Security yêu cầu: Username, Password, và Danh sách quyền hạn (Authorities)
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getHashedPassword(),
                Collections.singletonList(authority)
        );
    }
}