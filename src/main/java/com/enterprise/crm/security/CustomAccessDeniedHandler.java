package com.enterprise.crm.security;    // Ye batata hai class security module ke andar hai.

import com.fasterxml.jackson.databind.ObjectMapper; // Java object ko JSON me convert karta hai.
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;     // Client ke request  ko represent karta hai.
import jakarta.servlet.http.HttpServletResponse;    // Server response ko represent krta hai.
// Ye exception tab aata hai jab user authenticated hai lekin permission nahi hai.
import org.springframework.security.access.AccessDeniedException;
// Ye interface define karta hai ki 403 aane par kya karna hai.
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component; // Spring ko batata hai ki ye ek bean hai.

import javax.swing.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component  // Spring container me register ho gaya.
// implements AccessDeniedHandler: Spring Security ko bata rahe hain-> Agar role allowed nahi hua to ye class handle karegi.
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    // Ye tab call hota hai jab: User login hai BUT role allowed nahi hai.

    // Ye method automatically call hota hai jab: User authenticated ho -> Lekin required role nahi ho.
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException)
            throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);   //403 ka matlab: login hain lekin permission nahi hai.
        response.setContentType("application/json");        // Response JSON format me hoga.

        Map<String, Object> body = new HashMap<>();     // Ek Map banaya hai JSON data ke liye.
        body.put("success", false);     // Operation fail.
        body.put("message", "Forbidden - you don't have permission");  // Clear error message frontend ke liye hai.
        body.put("timestamp", LocalDateTime.now()); // Error ka time add kar rahe hain.

        //Map ko JSON me convert kar rahe hain: Direct response stream me likh diye hain.
        new ObjectMapper().writeValue(response.getOutputStream(), body);

        // OUTPUT: Frontend ko milega output
        // {
        //  "success": false,
        //  "message": "Forbidden - You don't have permission",
        //  "timestamp": "2026-02-25T16:50:00"
        //}

    }
}
