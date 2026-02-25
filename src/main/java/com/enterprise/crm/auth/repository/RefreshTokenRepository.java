package com.enterprise.crm.auth.repository;

import com.enterprise.crm.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long>
{

    public  Optional<RefreshToken> findByTokenAndRevokedFalse(String token);
}