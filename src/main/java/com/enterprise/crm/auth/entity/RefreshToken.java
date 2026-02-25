package com.enterprise.crm.auth.entity;

import com.enterprise.crm.common.audit.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class RefreshToken extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne
    private User user;

    private LocalDateTime expiryDate;

    private Boolean revoked = false;
}
//🔥 Explanation
//Refresh token DB me store karte hain kyunki:
// 👉 Agar user logout kare
// 👉 Ya suspicious activity ho.
//To hum refresh token revoke kar sakte hain.

//Agar sirf JWT use kare:
//👉 Logout possible nahi hota properly
//👉 Token expire hone tak valid rahta
