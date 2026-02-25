package com.enterprise.crm.auth.entity;

import com.enterprise.crm.common.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor   // 🔥 VERY IMPORTANT for Hibernate
@AllArgsConstructor
@Table(name = "roles",
        indexes = {
                // @Index → Role name pe fast search ke liye.
                @Index(name = "idx_role_name", columnList = "name")
            })
// Role extend kar raha hai BaseEntity → Soft delete + timestamps automatically milenge.
public class Role extends BaseEntity {

    // unique=true → Same role 2 baar insert nahi hoga.
    @Column(unique = true, nullable = false)
    private String name;

//    // Optional custom constructor
//    public Role(String roleName) {
//        super();
//    }
}

// Agar index nahi lagate: 👉 Large data me role lookup slow ho jata.