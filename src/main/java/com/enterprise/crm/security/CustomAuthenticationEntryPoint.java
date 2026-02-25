package com.enterprise.crm.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;
    // Ye method tab call hota hai jab:
    // 1. Token missing ho
    // 2. Token invalid ho
    // 3. User authenticated nahi ho
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException)
            throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);  // 401
        response.setContentType("application/json");

        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", "Unauthorized - Invalid or Missing Token");
        body.put("timestamp", LocalDateTime.now());

        objectMapper.writeValue(response.getOutputStream(), body);

    }
}
