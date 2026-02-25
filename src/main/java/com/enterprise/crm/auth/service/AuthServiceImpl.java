package com.enterprise.crm.auth.service;

import com.enterprise.crm.auth.dto.AuthResponse;
import com.enterprise.crm.auth.dto.LoginRequest;
import com.enterprise.crm.auth.dto.RegisterRequest;
import com.enterprise.crm.auth.entity.RefreshToken;
import com.enterprise.crm.auth.entity.Role;
import com.enterprise.crm.auth.entity.User;
import com.enterprise.crm.auth.repository.RefreshTokenRepository;
import com.enterprise.crm.auth.repository.RoleRepository;
import com.enterprise.crm.auth.repository.UserRepository;
import com.enterprise.crm.common.exception.BusinessException;
import com.enterprise.crm.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service      //@Service : Spring ko batata hai ki ye business layer component hai.
@RequiredArgsConstructor    // @RequiredArgsConstructor : Lombok constructor bana deta hai sab final fields ke liye.Isliye manual constructor likhne ki zarurat nahi.
public class AuthServiceImpl implements AuthService{

    // Ye sab dependency injection se aa rahe hain.
    private final UserRepository userRepository;    // UserRepository :   DB se user fetch/save karega.
    private final RoleRepository roleRepository;    // RoleRepository :  Default role assign karne ke liye.
    private final RefreshTokenRepository refreshTokenRepository;    // RefreshTokenRepository :  Refresh token DB me store karne ke liye.
    private final PasswordEncoder passwordEncoder;      // PasswordEncoder : Plain password ko hash karega (BCrypt ideally).
    private final JwtUtil jwtUtil;      // JwtUtil : Access token generate karega.

    @Override
    public void register(RegisterRequest request) {

        // check kar rahe hain email already exist karti hai. to exception throw karo.
        // Soft delete support bhi hai (isDeletedFalse).
        if(userRepository.findByEmailAndIsDeletedFalse(request.getEmail()).isPresent())
        {
            // Yahan pe Custom exception throw kar rahe hain.
            throw new BusinessException("Email already registered");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());

        // password ko hash kar rahe hain security ke liye.
        //Never store raw password.
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Default role assign kar rahe. ("USER")
        // Better practice: enum use ka sakte hain instead of hardcoded string.
        final String DEFAULT_ROLE = "USER";   // Hardcoded string avoid kar rahe hain -> Create constant
        Role role = roleRepository.findByName(DEFAULT_ROLE).orElseThrow(() -> new BusinessException("Default role not found"));

        user.setRoles(Set.of(role));     // User ke paas multiple roles ho sakte hain future me.

        userRepository.save(user);      //Finally DB me save.
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        // User fetch kar rahe hain.
        User user = userRepository.findByEmailAndIsDeletedFalse(request.getEmail()).orElseThrow(() -> new BusinessException("Invalid credentials"));

        // Raw password ko DB ke hashed password se compare kar rahe hain.
        if(!passwordEncoder.matches(request.getPassword(), user.getPassword()))
        {
            // Yahan pe Custom exception throw kar rahe hain.
            throw new BusinessException("Invalid credentials");
        }
        // Access Token generate.
        String accessToken = jwtUtil.generateAccessToken(user.getEmail());

        // Refresh token create.
        // Random secure string generate kar rahe hain.
        String refreshTokenValue = UUID.randomUUID().toString();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(refreshTokenValue);
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));  // 7 days valid expiry

        // Refresh token DB me store ho raha hai.
        // Ye good practice hai (stateless + controlled).
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .build();
    }

    @Override
    public AuthResponse refreshToken(String token) {

        RefreshToken invalidRefreshToken =
                refreshTokenRepository.findByTokenAndRevokedFalse(token)
                        .orElseThrow(() -> new BusinessException("Invalid refresh token"));

        // Expiry check kar rahe hain.
        if(invalidRefreshToken.getExpiryDate().isBefore(LocalDateTime.now()))
        {
            throw new BusinessException("Refresh token expired");
        }

        // yahan pe Token rotation kar rahe hain. : Old refresh token revoke ho raha hai.
        invalidRefreshToken.setRevoked(true);
        refreshTokenRepository.save(invalidRefreshToken);

        // New access token generate.
        String newAccesToken = jwtUtil.generateAccessToken(invalidRefreshToken.getUser().getEmail());

        // New refresh token generate.
        String newRefreshTokenValue = UUID.randomUUID().toString();

        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setToken(newRefreshTokenValue);
        newRefreshToken.setUser(invalidRefreshToken.getUser());
        newRefreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));

        refreshTokenRepository.save(newRefreshToken);

        return AuthResponse.builder()
                .accessToken(newAccesToken)
                .refreshToken(newRefreshTokenValue)
                .build();
    }
}

/**
 * 🔹 REGISTER
 *
 * Email duplicate check
 *
 * Password hash
 *
 * Default role assign
 *
 * Save user
 *
 * 🔹 LOGIN
 *
 * Email fetch
 *
 * Password match
 *
 * Access token generate (15 min)
 *
 * Refresh token DB me store
 *
 * 7 din expiry
 *
 * 🔹 REFRESH TOKEN
 *
 * Token validate
 *
 * Expiry check
 *
 * Old revoke
 *
 * Naya generate
 *
 * Ye hi Token Rotation Mechanism hai.
 * */
