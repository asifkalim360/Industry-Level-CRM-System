package com.enterprise.crm.security; // Security configuration related classes yaha rakhi jaati hain.

import lombok.RequiredArgsConstructor;
// ye dono Spring configuration aur bean banane ke liye.
import org.apache.tomcat.util.http.parser.Authorization;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
// ye dono AuthenticationManager login process handle karta hai.
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
// Session management define karne ke liye.
import org.springframework.security.config.http.SessionCreationPolicy;
// ye dono Password hashing ke liye.
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
// Yaha hum define karte hain: Kaunse endpoints secure hain: Kaunse public hain
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// ye dono ka use Filter chain define karne ke liye karte hain.
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration      // Ye Spring ko batata hai: Ye class configuration class hai.
@EnableWebSecurity
@RequiredArgsConstructor    // Constructor auto generate karega:
public class SecurityConfig {

    // yahan pe custom JWT filter inject ho raha hai.
    private final JwtAuthenticationFilter jwtAuthenticationFilter;


    @Bean               //Ye sabse important method hai: Spring Boot 3 me:
                        // WebSecurityConfigurerAdapter remove ho chuka hai. Ab ye modern configuration approach hai.
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                //CSRF ka use tab hota hai jab Server session use karta hai aur Browser form login hota hai
                //Humlog JWT + Stateless use kar rahe hain. Isliye CSRF disable karna correct hai.
                .csrf(csrf -> csrf.disable())

                // Ye line bahut important hai: STATELESS ka matlab.Server session store nahi karega
                //Har request me token required hoga. Ye JWT based system ke liye mandatory hai.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Authorization Rules: Yaha se access control start hota hai.
                .authorizeHttpRequests(auth -> auth

                        // Public endpoints: Iska matlab: Register, Login, Refresh
                        //Without token access ho sakta hai.
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/actuator/**"
                        ).permitAll()

                        // Admin Endpoints: Role based access -> (.requestMatchers("/api/v1/admin/**").hasRole("ADMIN"))
                        // Iska matlab: Agar URL /api/v1/admin/... hai -> To user ke paas ROLE_ADMIN hona chahiye
                        // Important: hasRole("ADMIN") internally check karega:(ROLE_ADMIN)
                        // Isliye humlogo ke  CustomUserDetailsService me "ROLE_" + role.getName() hona chahiye.
//                        .requestMatchers("/api/v1/admin/**")
//                        .hasRole("ADMIN")

                        // Baaki Sab Endpoints Jo upar define nahi hain Un sab ke liye token required hai.
                        .anyRequest().authenticated()
                )

                // Ye sabse critical line hai. Iska matlab hai ki JWT filter chalega BEFORE default username/password filter.
                // Flow ban gaya: Request -> JwtAuthenticationFilter -> UsernamePasswordAuthenticationFilter -> controller.
                // Agar esko yahan add nahi karte to JWT validation kabhi execute nahi hota.
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        // Return Build : Spring ko final filter chain de diya gaya.
        return http.build();
    }


    // Ye bean password hashing ke liye use hoga
    // Agar ye bean define nahi karte to Spring ko pata nahi hota kaunsa encoder use karna hai
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt: Password hash karta hai, Salt automatically add karta hai, Secure algorithm hai.
        return new BCryptPasswordEncoder();
        //Login ke time internally:(passwordEncoder.matches(raw, encoded)) use karta hai.
    }


    // Ye Spring ko AuthenticationManager provide karta hai.Agar tum manual authentication karna chaho future me:(authenticationManager.authenticate(...)) to bean jrury hotahai.
    @Bean       // AuthenticationManager Bean:
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

}


//  COMPLETE SECURITY FLOW:
//      Client Request
//              ↓
//      SecurityFilterChain
//              ↓
//      JwtAuthenticationFilter
//              ↓
//      Token Validate
//              ↓
//      SecurityContext Set
//              ↓
//      Authorization Check (hasRole)
//              ↓
//      Controller Access