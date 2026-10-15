package com.example.ganpativargani.repository;

import com.example.ganpativargani.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByUsernameAndDeletedFalse(String username);
    boolean existsByDeletedFalse();
}
