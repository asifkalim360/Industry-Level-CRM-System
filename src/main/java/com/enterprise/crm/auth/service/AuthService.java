package com.enterprise.crm.auth.service;

import com.enterprise.crm.auth.dto.AuthResponse;
import com.enterprise.crm.auth.dto.LoginRequest;
import com.enterprise.crm.auth.dto.RegisterRequest;

import java.util.Map;

public interface AuthService {

    public void register(RegisterRequest request);

    public AuthResponse login(LoginRequest request);

    public AuthResponse refreshToken(String refreshToken);

}
