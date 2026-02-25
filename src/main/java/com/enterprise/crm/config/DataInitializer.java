package com.enterprise.crm.config;

import com.enterprise.crm.auth.entity.Role;
import com.enterprise.crm.auth.entity.User;
import com.enterprise.crm.auth.repository.RoleRepository;
import com.enterprise.crm.auth.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {

        // Step 1: Roles create karo agar exist nahi karte
        createRoleIfNotExists("USER");
        createRoleIfNotExists("ADMIN");
        createRoleIfNotExists("MANAGER"); // future ready

        // Step 2: Default Admin create karo
        createAdminIfNotExists();
    }

    private void createRoleIfNotExists(String roleName) {
        roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(new Role(roleName)));
    }

    private void createAdminIfNotExists() {

        String adminEmail = "admin@crm.com";

        if (userRepository.findByEmailAndIsDeletedFalse(adminEmail).isEmpty()) {

            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseThrow(() -> new RuntimeException("ADMIN role not found"));

            User admin = new User();
            admin.setName("Super Admin");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            admin.setRoles(Set.of(adminRole));

            userRepository.save(admin);

            System.out.println("Default ADMIN created successfully");
        }
    }
}