//Ye batata hai ki class kis package me hai.CRM project me ye security module ka part hai.
package com.enterprise.crm.security;

import jakarta.servlet.ServletException;
// Incoming request ko represent karta hai.
import jakarta.servlet.http.HttpServletRequest;
// yeh outgoing response ko represent karta hai.
import jakarta.servlet.http.HttpServletResponse;
// Lombok annotation: Final fields ka constructor automatically generate karta hai.
import lombok.Lombok;
import lombok.RequiredArgsConstructor;
// Jab authentication fail hota hai tab ye exception aata hai.
import org.springframework.security.core.AuthenticationException;
// Ye interface define karta hai ki jab user unauthorized ho to kya karna hai.
import org.springframework.security.web.AuthenticationEntryPoint;
// Spring ko batata hai ki ye ek bean hai.
import org.springframework.stereotype.Component;
// JSON banane ke liye use hota hai.Object ko JSON me convert karta hai.
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component      // Spring container me register kar diya.
@RequiredArgsConstructor        // final fields ka constructor bana diya automatically.
//implements AuthenticationEntryPoint ->  Spring Security ko bol rahe: Jab authentication fail ho jaye tab ye class handle karegi
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    // Ye JSON banane ke liye inject ho raha hai. Lombok automatically constructor bana dega:
    private final ObjectMapper objectMapper;
    // Ye method tab call hota hai jab:
    // 1. Token missing ho
    // 2. Token invalid ho
    // 3. User authenticated nahi ho

    // Ye method automatically call hota hai jab authentication fail hota hai.
    //Parameters:
    //request → client ne kya bheja
    //response → hum kya bhejne wale
    //authException → fail hone ka reason
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException)
            throws IOException, ServletException {
        // Ye method automatically call hota hai jab authentication fail hota hai.
        //Parameters:
        //request → client ne kya bheja
        //response → hum kya bhejne wale
        //authException → fail hone ka reason

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);  // 401 status bhej rahe -> Matlab: User authorized nahi hai.
        response.setContentType("application/json");        // Response JSON format me hoga.

        Map<String, Object> body = new HashMap<>();     // Map banaya yahan pe JSON data ko store karne ke liye.
        body.put("success", false);         // Frontend ko bataya: Operation fail.
        body.put("message", "Unauthorized - Invalid or Missing Token"); // Clear error message.
        body.put("timestamp", LocalDateTime.now());     // Error kab hua wo time bhi bhej diya -> Production me helpful hota hai debugging ke liye.

        // Yahan magic ho raha hai: Map ko JSON me convert kiya
        //Direct response ke output stream me likh diya
        objectMapper.writeValue(response.getOutputStream(), body);


        //OUTPT -> Frontend ko milne wala response kuch aisa hoga:
        // {
        //  "success": false,
        //  "message": "Unauthorized - Invalid or Missing Token",
        //  "timestamp": "2026-02-25T16:30:00"
        //}

    }
}
