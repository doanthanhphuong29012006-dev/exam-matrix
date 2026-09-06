package com.exammatrix.backend.security;

import com.exammatrix.backend.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtEncoder jwtEncoder;

    @Value("${app.jwt.expiration-minutes}")
    private long expirationMinutes;

    public String generateToken(User user) {
        Instant now = Instant.now();

        String authority = "ROLE_" + user.getRole()
            .getName()
            .trim()
            .toUpperCase(Locale.ROOT);

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("exam-matrix")
            .issuedAt(now)
            .expiresAt(
                now.plus(
                    expirationMinutes,
                    ChronoUnit.MINUTES
                )
            )
            .subject(user.getUsername())
            .claim("userId", user.getId().toString())
            .claim("authorities", List.of(authority))
            .build();

        JwsHeader header = JwsHeader
            .with(MacAlgorithm.HS256)
            .type("JWT")
            .build();

        JwtEncoderParameters parameters = JwtEncoderParameters.from(header, claims);

        return jwtEncoder
            .encode(parameters)
            .getTokenValue();
    }
}