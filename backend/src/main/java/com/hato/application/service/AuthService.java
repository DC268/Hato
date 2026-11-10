package com.hato.application.service;

import com.hato.api.dto.*;
import com.hato.api.exception.ApiException;
import com.hato.api.security.GoogleTokenVerifierService;
import com.hato.api.security.JwtTokenProvider;
import com.hato.domain.model.Role;
import com.hato.infrastructure.persistence.entity.UserEntity;
import com.hato.infrastructure.persistence.repository.SpringDataUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {

    private final SpringDataUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final GoogleTokenVerifierService googleVerifier;

    public AuthService(
            SpringDataUserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            GoogleTokenVerifierService googleVerifier
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.googleVerifier = googleVerifier;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new ApiException("EMAIL_ALREADY_EXISTS", "El correo electrónico ya está registrado", HttpStatus.CONFLICT);
        }

        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID().toString());
        user.setName(req.name());
        user.setEmail(req.email().toLowerCase().trim());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setPhone(req.phone());
        user.setRole(Role.OWNER);
        user.setCreatedAt(Instant.now());

        userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return AuthResponse.of(token, toDto(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        UserEntity user = userRepository.findByEmail(req.email().toLowerCase().trim())
                .orElseThrow(() -> new ApiException("INVALID_CREDENTIALS", "Credenciales incorrectas", HttpStatus.UNAUTHORIZED));

        if (user.getPasswordHash() == null || !passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new ApiException("INVALID_CREDENTIALS", "Credenciales incorrectas", HttpStatus.UNAUTHORIZED);
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return AuthResponse.of(token, toDto(user));
    }

    @Transactional
    public AuthResponse loginWithGoogle(GoogleLoginRequest req) {
        var profile = googleVerifier.verify(req.idToken());

        UserEntity user = userRepository.findByEmail(profile.email())
                .orElseGet(() -> {
                    UserEntity newUser = new UserEntity();
                    newUser.setId(UUID.randomUUID().toString());
                    newUser.setName(profile.name() != null ? profile.name() : "Usuario Google");
                    newUser.setEmail(profile.email().toLowerCase().trim());
                    newUser.setRole(Role.OWNER);
                    newUser.setAvatarUrl(profile.pictureUrl());
                    newUser.setCreatedAt(Instant.now());
                    return userRepository.save(newUser);
                });

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return AuthResponse.of(token, toDto(user));
    }

    private UserDto toDto(UserEntity entity) {
        return new UserDto(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getRole().name(),
                entity.getPhone()
        );
    }
}
