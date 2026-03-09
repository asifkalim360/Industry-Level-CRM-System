package com.enterprise.crm.common.audit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;      // Kis user ne action kiya
    private String action;     // LOGIN_SUCCESS, LOGIN_FAILED, etc.
    private String ipAddress;  // Optional but good practice

    private LocalDateTime timestamp;
}

//DTO Banega Kya? -> Nahi Banega
//AuditLog ka DTO nahi banate because: Ye internal tracking system hai.
// Client ko directly expose nahi karte. Ye sirf DB me store hota hai
//Agar future me admin ko audit dekhna ho → tab DTO banega. Abhi need nahi hai.