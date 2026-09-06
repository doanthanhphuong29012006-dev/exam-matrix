package com.exammatrix.backend.security;

import com.exammatrix.backend.entity.User;
import com.exammatrix.backend.entity.enums.UserStatus;
import com.exammatrix.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CurrentUserService {
    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication
                instanceof AnonymousAuthenticationToken) {

            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Bạn chưa đăng nhập"
            );
        }

        User user = userRepository.findByUsernameIgnoreCase(
                authentication.getName()
        ).orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Tài khoản không tồn tại"
                )
            );

        if (user.getStatus() == UserStatus.LOCKED) {
            throw new ResponseStatusException(
                HttpStatus.LOCKED,
                "Tài khoản đã bị khóa"
            );
        }

        return user;
    }
}