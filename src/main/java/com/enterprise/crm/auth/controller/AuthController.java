package com.enterprise.crm.auth.controller;

import com.enterprise.crm.auth.dto.AuthResponse;
import com.enterprise.crm.auth.dto.LoginRequest;
import com.enterprise.crm.auth.dto.RegisterRequest;
import com.enterprise.crm.auth.service.AuthService;
import com.enterprise.crm.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

// @RestController : Ye class HTTP requests handle karegi
//@Controller + @ResponseBody ka combination hai
//Return value automatically JSON ban jayega
//Yani yaha se jo bhi return hoga, wo JSON me convert hoga.
@RestController
@RequestMapping("/api/v1/auth")     // Ye base URL define karta hai.
// Lombok automatically constructor bana deta hai:
// Manual constructor likhne ki zarurat nahi.
@RequiredArgsConstructor
public class AuthController {

    // yahan pe AuthService layer ki dependency inject ho rahi hai.
    // Kyunki Controller khud Register or logic nahi likhta.Wo service layer ko call karta hai.
    private final AuthService authService;

    // HTTP POST request handle karega.
    @PostMapping("/register")   //Final Endpoint: POST /api/v1/auth/register
    public ApiResponse<?> register(@Valid @RequestBody RegisterRequest request)
    {                            // Client jo bhi JSON bhejega, wo RegisterRequest DTO me convert ho jayega.
                                 // Spring automatically JSON → Java object me convert karta hai.

        authService.register(request); //Actual business logic yaha nahi hai.Service layer handle kar rahi hai.

        return ApiResponse.builder()
                .success(true)
                .message("User registered successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@RequestBody LoginRequest request)
    {

        //Service return karega: accessToken, refreshToken
        AuthResponse login = authService.login(request);

        return ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Login Successfully")
                .data(login)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refreshToken(@RequestParam String refreshToken)
    {                                           // @RequestParam : Iska matlab client URL me param bhejega.
                                                // Example : POST /api/v1/auth/refresh?refreshToken=abcd-123

        // old refresh token validate karega.
        //revoke karega.
        //new access + refresh generate karega.
        AuthResponse authResponse = authService.refreshToken(refreshToken);

        return ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Token refreshed")
                .data(authResponse)
                .timestamp(LocalDateTime.now())
                .build();
    }

}
