package com.enterprise.crm.auth.entity;

import com.enterprise.crm.common.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Getter
@Setter
                                                    // Email pe index → Login fast hoga.
@Table(name = "users", indexes = { @Index(name="idx_user_email", columnList = "email")})
public class User extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private Boolean isActive = true;        // isActive → Future me account disable kar sakte hain.

    // ManyToMany → Ek user multiple roles le sakta hai.
    @ManyToMany(fetch = FetchType.EAGER)    // FetchType.EAGER → Roles login ke time immediately load ho jayenge.
                            //Agar eager nahi karte: 👉 Lazy loading me security context me problem aa sakti hai.
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

}
