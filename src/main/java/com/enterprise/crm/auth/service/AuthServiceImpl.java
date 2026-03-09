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
import com.enterprise.crm.common.audit.entity.AuditLog;
import com.enterprise.crm.common.audit.repository.AuditLogRepository;
import com.enterprise.crm.common.exception.BusinessException;
import com.enterprise.crm.security.JwtUtil;
import jakarta.validation.constraints.Email;
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

    private final AuditLogRepository auditLogRepository;

    //----------------------------REGISTRATION START ------------------------------------
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

        userRepository.save(user);       //Finally DB me save.
    }
    //---------------------------- REGISTRATION END -------------------------------

    //------------------------------ LOGIN START ------------------------------------
    // Ye service layer ka overridden method hai.
    //Controller yahan call karega jab /login hit hoga.
    //Return karega AuthResponse (accessToken + refreshToken).
    @Override
    public AuthResponse login(LoginRequest request) {

        // User fetch -> Email ke basis pe user fetch ho raha hai.
        // Sirf wahi user milega jo isDeleted = false hai (soft delete logic).
        // Agar user nahi mila → direct error throw.
        User user = userRepository
                .findByEmailAndIsDeletedFalse(request.getEmail())
                        //"Invalid credentials" generic message diya gaya —
                        //ye intentionally hai taki attacker ko pata na chale email exist karta hai ya nahi
                .orElseThrow(() -> new BusinessException("Invalid credentials"));

        // Account locked check: -> Agar account locked flag true hai to aage check karenge.
        if (user.isAccountLocked()) {

            // Optional auto unlock after 30 minutes
            // CHECK : -> Lock time null nahi hona chahiye : Lock time + 30 min current time se pehle hona chahye?
            // MEANS : -> check karega ki 30 minute complete ho gaye kya?
            if (user.getLockTime() != null &&
                    user.getLockTime().plusMinutes(30).isBefore(LocalDateTime.now())) {

                auditLogRepository.save(
                        AuditLog.builder()
                                .email(user.getEmail())
                                .action("ACCOUNT_AUTO_UNLOCKED")
                                .timestamp(LocalDateTime.now())
                                .build()
                );

                // Agar 30 min complete ho gaye hain to.
                // Account unlock: Lock false, Failed attempts reset, Lock time clear, DB me save.
                //Ye auto unlock mechanism hai
                user.setAccountLocked(false);
                user.setFailedAttempts(0);
                user.setLockTime(null);
                userRepository.save(user);

            } else {

                // 🔐 (Optional) Audit log for locked attempt
                auditLogRepository.save(
                        AuditLog.builder()
                                .email(user.getEmail())
                                .action("ACCOUNT_LOCKED_ATTEMPT")
                                .timestamp(LocalDateTime.now())
                                .build()
                );

                // Agar 30 min complete nahi hue to: -> User ko login nahi karne diya jayega.
                throw new BusinessException("Account locked. Try again later.");
            }
        }

        // Raw password ko DB ke hashed password se compare kar rahe hain.
        if(!passwordEncoder.matches(request.getPassword(), user.getPassword()))
        {
            // Agar match nahi hua:
            // Failed attempt increase hoga: -> Counter increase.
            user.setFailedAttempts(user.getFailedAttempts() + 1);


            // Agar 5 ya zyada attempts ho gaye to -> Account lock, Lock time store
            //Ye brute force attack prevent karta hai
            if(user.getFailedAttempts() >= 5)
            {
                user.setAccountLocked(true);
                user.setLockTime(LocalDateTime.now());

                // 🔐 ACCOUNT LOCKED AUDIT LOG ← YAHAN ADD KARNA HOGA.
                auditLogRepository.save(
                        AuditLog.builder()
                                .email(user.getEmail())
                                .action("ACCOUNT_LOCKED")
                                .timestamp(LocalDateTime.now())
                                .build()
                );
            }

            // Important yahan pe — DB update ho raha hai.
            userRepository.save(user);

            // 🔐 AUDIT LOG — LOGIN FAILED
            auditLogRepository.save(
                    AuditLog.builder()
                            .email(user.getEmail())
                            .action("LOGIN_FAILED")
                            .timestamp(LocalDateTime.now())
                            .build()
            );

            // Yahan pe Custom exception throw kar rahe hain.
            // Again generic message — security best practice
            throw new BusinessException("Invalid credentials");
        }

        // Successfull login:-> Agar password sahi hai.
        // Counter reset kar diya:-> Agar reset nahi karte to future me accidental lock ho sakta tha.
        user.setFailedAttempts(0);
        userRepository.save(user);

        // Access Token generate: JWT access token generate kar rahe hain.
        //Usually: Email subject hota hai, Expiry 15–30 min hoti hai
        //Ye har request me Authorization header me jayega.
        String accessToken = jwtUtil.generateAccessToken(user.getEmail());

        // Refresh token generate -> Random secure string generate kar rahe hain.
        //Refresh token: Long expiry, DB me store hota hai
        String refreshTokenValue = UUID.randomUUID().toString();

        // RefreshToken Entity Create:
        // Yahan: Token value, User relation, Expiry 7 days, sabkuch Set kar rahe hain.
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(refreshTokenValue);
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));  // 7 days valid expiry

        // Refresh token DB me store ho raha hai.
        // Ye good practice hai (stateless + controlled).
        //Security advantage: Logout pe delete kar sakte hain, Token revoke kar sakte hain.
        refreshTokenRepository.save(refreshToken);

        // 🔐 AUDIT LOG — LOGIN SUCCESS
        auditLogRepository.save(
                AuditLog.builder()
                        .email(user.getEmail())
                        .action("LOGIN_SUCCESS")
                        .timestamp(LocalDateTime.now())
                        .build()
        );

        // Final Response: Controller ko return kar rahe hain.
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .build();
    }
    //----------------------------LOGIN END ------------------------------------
    //----------------------------REFRESH-TOKEN START ------------------------------------
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
    //----------------------------REFRESH-TOKEN END ------------------------------------
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
