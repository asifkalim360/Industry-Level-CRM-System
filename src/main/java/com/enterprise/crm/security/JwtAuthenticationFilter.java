package com.enterprise.crm.security;

// ye 4 sevlet methods jo import huye hain -> wo HTTP request/response handle karne ke liye kiye jaaate hain.
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Component;

//Ye Spring Security ke core classes hain:
//UsernamePasswordAuthenticationToken, SecurityContextHolder, UserDetails
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

// OncePerRequestFilter kya karta hai?
// Guarantee karta hai: Ek request ke liye filter sirf ek baar chale.
// Agar forward ya include ho jaye request, tab bhi duplicate execution nahi hota.

import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component      // Spring automatically bean create karega.
@RequiredArgsConstructor        // Constructor auto generate karega:
// extends OncePerRequestFilter -> Iska matlab: Ye filter har request pe chalega.
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    //Dependencies:
    // JwtUtil: Token validate karega and Email extract karega
   private final JwtUtil jwtUtil;
   // CustomUserDetailsService: Database se user load karega
   private final CustomUserDetailsService customUserDetailsService;

    @Override       // ye ek Core Method hai.Ye method har incoming request pe execute hota hai.
                    // Flow: Client → Filter → Controller
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        // Step 1 — Authorization Header Read -> Client jab request bhejta hai:
        // Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9... -> Ye wahi header read ho raha hai.
        String authHeader = request.getHeader("Authorization");

        //Step 2 — Header Check -> Agar: Header nahi hai, Bearer prefix nahi hai -> Toh filter skip ho jayega.
        // Important: Public endpoints ke liye ye allow karta hai.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);   // JUST PASS
            return;
        }

        // Step 3 — Token Extract -> "Bearer " 7 characters ka hota hai.
        String token = authHeader.substring(7);

        //Step 4 — Token Validate: Ye check karega: -> Expired?, Tampered?, Wrong signature?
        //Agar invalid hua → authentication set nahi hoga.
        if(!jwtUtil.validateToken(token))
        {
            filterChain.doFilter(request, response);
        }

        // Step 5 — Email Extract: -> JWT payload se subject (email) nikal rahe hain.
        String email = jwtUtil.extractEmail(token);

        // Step 6 — Load User From DB: -> DB se fresh user load ho raha hai and Roles bhi load ho rahe hain.
        //Important: JWT me roles store nahi kiye the, Isliye DB call zaroori hai.
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // Step 7 — Authentication Object Create: -> Ye line bahut important hai: -> Ye represent karta hai ki User authenticated hai.
        //Parameters: -> Principal → userDetails, Credentials → null (kyuki password check ho chuka), Authorities → roles
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities());

        // Step 8 — Security Context Set: -> Ye bhi bhut important line hai -> Iska matlab: Current request ke liye user authenticated hai.
        // Ab controller me: -> @PreAuthorize("hasRole('ADMIN')") work karega sahi se.
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // Step 9 — Continue Filter Chain: -> Ye request ko next filter ya controller tak bhejta hai.
        // Agar ye call nahi karoge → request yahi ruk jayegi.
        filterChain.doFilter(request, response);

    }
}

//  Client Request
//       ↓
//  JwtAuthenticationFilter
//       ↓
//  Extract Token
//       ↓
//  Validate Token
//       ↓
//  Load User
//       ↓
//  Set Authentication
//       ↓
//   Controller
