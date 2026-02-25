package com.enterprise.crm.common.audit;

import jakarta.persistence.*;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@MappedSuperclass    // Ye table nahi banayega, lekin jo entity extend karegi usme ye fields aa jayengi.
@Getter
@Setter
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)    // Auto increment id.
    private Long id;

    // Record kab create hua
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // Record kab update hua
    private LocalDateTime updatedAt;

    // Soft delete ke liye
    private Boolean isDeleted = false;      // Hard delete nahi karenge. Data kabhi permanently delete nahi hota real systems me.

    @PrePersist     // Insert hone se pehle auto timestamp set karega.
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate      // Update hone se pehle updated time set karega.
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
