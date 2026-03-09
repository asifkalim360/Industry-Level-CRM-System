package com.enterprise.crm;

import jakarta.annotation.security.RolesAllowed;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

// Spring Security ko bolta hai:
// Controller ya Service ke methods pe jo security annotations lage hain, unko enable karo.
// @EnableMethodSecurity in sab Annotations ko activate karta hai: @PreAuthorize, @PostAuthorize, @Secured, @RolesAllowed
@EnableMethodSecurity
@EnableConfigurationProperties
@SpringBootApplication
public class CrmApplication {

	public static void main(String[] args) {
		SpringApplication.run(CrmApplication.class, args);
	}

}
