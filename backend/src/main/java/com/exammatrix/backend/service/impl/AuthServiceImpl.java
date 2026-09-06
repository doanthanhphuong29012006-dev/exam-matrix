package com.exammatrix.backend.service.impl;

import com.exammatrix.backend.dto.request.ChangePasswordRequest;
import com.exammatrix.backend.dto.request.LoginRequest;
import com.exammatrix.backend.dto.request.UpdateProfileRequest;
import com.exammatrix.backend.dto.response.AuthResponse;
import com.exammatrix.backend.dto.response.UserResponse;
import com.exammatrix.backend.entity.User;
import com.exammatrix.backend.repository.UserRepository;
import com.exammatrix.backend.security.CurrentUserService;
import com.exammatrix.backend.security.JwtService;
import com.exammatrix.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CurrentUserService currentUserService;

    @Override
    public AuthResponse login(LoginRequest request) {
        String username = request.getUsername()
                .trim()
                .toLowerCase(Locale.ROOT);

        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    username,
                    request.getPassword()
                )
            );
        } catch (LockedException exception) {
            throw new ResponseStatusException(
                HttpStatus.LOCKED,
                "Tài khoản đã bị khóa"
            );
        } catch (AuthenticationException exception) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Username hoặc password không chính xác"
            );
        }

        User user = userRepository.findByUsernameIgnoreCase(username)
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Tài khoản không tồn tại"
                )
            );

        String accessToken = jwtService.generateToken(user);

        return new AuthResponse(
            accessToken,
            convertToResponse(user)
        );
    }

    @Override
    public UserResponse getCurrentUser() {
        User user = currentUserService.getCurrentUser();

        return convertToResponse(user);
    }

    @Override
    public UserResponse updateProfile(UpdateProfileRequest request) {
        User user = currentUserService.getCurrentUser();

        String normalizedEmail = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        boolean emailExisted = userRepository.existsByEmailIgnoreCaseAndIdNot(
                normalizedEmail,
                user.getId()
        );

        if (emailExisted) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Email đã được sử dụng"
            );
        }

        user.setFullName(request.getFullName().trim());
        user.setEmail(normalizedEmail);

        User savedUser = userRepository.save(user);

        return convertToResponse(savedUser);
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {
        User user =
                currentUserService.getCurrentUser();

        boolean currentPasswordCorrect = passwordEncoder.matches(
                    request.getCurrentPassword(),
                    user.getPassword()
                );

        if (!currentPasswordCorrect) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Mật khẩu hiện tại không chính xác"
            );
        }

        boolean sameAsCurrentPassword = passwordEncoder.matches(
                    request.getNewPassword(),
                    user.getPassword()
                );

        if (sameAsCurrentPassword) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Mật khẩu mới phải khác mật khẩu hiện tại"
            );
        }

        user.setPassword(
            passwordEncoder.encode(
                request.getNewPassword()
            )
        );

        userRepository.save(user);
    }

    @Override
    public void logout() {
        /*
         * JWT là stateless nên backend hiện chưa lưu session
         * để xóa. Frontend chỉ cần xóa accessToken.
         *
         * Sau này có thể làm refresh token hoặc blacklist.
         */
    }

    private UserResponse convertToResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFullName(),
            user.getRole().getName(),
            user.getStatus(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}