package com.enterprise.crm.admin.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    // Sirf ADMIN access karega
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDashboard() {
        return "Welcome Admin";
    }

    // ADMIN aur MANAGER dono access kar sakte hain
    @GetMapping("/reports")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String reports() {
        return "Reports Data";
    }
}
