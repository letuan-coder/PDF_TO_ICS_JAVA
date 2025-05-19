package com.example.demo_tttn.services;

import com.example.demo_tttn.dtos.User;
import com.example.demo_tttn.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public User getOrCreateUser(OAuth2AuthenticationToken authentication) {
        String username = authentication.getPrincipal().getAttribute("sub"); // Lấy ID Google
        return userRepository.findByUsername(username)
                .orElseGet(() -> {
                    User user = new User();
                    user.setUsername(username);
                    user.setToken(authentication.getPrincipal().getAttribute("access_token"));
                    user.setTokenExpiry(LocalDateTime.now().plusHours(1)); // Ví dụ: token hết hạn sau 1 giờ
                    return userRepository.save(user);
                });
    }
}
