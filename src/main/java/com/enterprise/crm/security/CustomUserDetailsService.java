package com.enterprise.crm.security;

// Ye humlogo ka custom entity aur repository hai.
import com.enterprise.crm.auth.entity.User;     // User → database entity
import com.enterprise.crm.auth.repository.UserRepository;   // UserRepository → DB se user fetch karega
import lombok.RequiredArgsConstructor;

// Yaha se real security start hoti hai.
// UserDetailsService: Spring Security ka interface hai.
// User ko database se load karna during authentication.
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service; // Spring bean banane ke liye.

import javax.swing.*;
import java.security.Security;

@Service    // Spring automatically object banayega.
@RequiredArgsConstructor    // Constructor auto-generate karega:
// implements UserDetailsService: Ye sabse important line hai.
// Spring Security jab login karega, to internally ye method call karega: -> loadUserByUsername()
public class CustomUserDetailsService implements UserDetailsService {

    // Dependency : DB se user fetch karne ke liye.
    private final UserRepository userRepository;

                    // Ye method: Authentication ke time call hota hai: Username yaha email hai
     @Override      // Spring internally karega: userDetailsService.loadUserByUsername(username)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

         // Explanation: Database me email search, Soft delete false condition check, Agar nahi mila to → exception throw
         // Important: Spring expects UsernameNotFoundException -> Isliye yaha BusinessException nahi use karte hain
        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not fount"));

        // org.springframework.security.core.userdetails.User => Ye hahari entity class nahi hai. Ye Spring Security ka built-in User class hai.
        return org.springframework.security.core.userdetails.User
                .builder()      // Object banana start.
                .username(user.getEmail())      // Authentication ke liye email use ho raha hai.
                // Spring internally karega:-> (passwordEncoder.matches(rawPassword, encodedPassword))
                .password(user.getPassword())   // Important: Ye hashed password hai.
                .authorities(
                        user.getRoles() // Ye Set<Role> return karega: (Example -> [SALES, ADMIN])
                                .stream()   // Java Stream start.
                                // Har role ko convert kar rahe ho: (Example: ROLESALES, ROLEADMIN)
                                .map(role -> "ROLE_" + role.getName())
                                .toArray(String[]::new) // Stream ko String array me convert kar diya.
                                                        // Spring internally authority ko array format me leta hai.
                )
                .build();   // Final Build: Spring Security compatible UserDetails object return ho gaya.
    }
}
