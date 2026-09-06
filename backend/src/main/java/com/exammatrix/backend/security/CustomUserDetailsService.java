package com.exammatrix.backend.security;

import com.exammatrix.backend.entity.User;
import com.exammatrix.backend.entity.enums.UserStatus;
import com.exammatrix.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByUsernameIgnoreCase(username.trim())
            .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản"));

        String authority = "ROLE_" + user.getRole()
            .getName()
            .trim()
            .toUpperCase(Locale.ROOT);

        boolean accountNonLocked = user.getStatus() != UserStatus.LOCKED;

        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            true,
            true,
            true,
            accountNonLocked,
            List.of(new SimpleGrantedAuthority(authority))
        );
    }
}