package com.performiq.auth.repository;

import com.performiq.auth.entity.AppUser;
import com.performiq.auth.entity.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, UUID> {
    Optional<AppUser> findByUsername(String username);
    Optional<AppUser> findByUsernameIgnoreCase(String username);
    Optional<AppUser> findByEmployeeId(UUID employeeId);
    boolean existsByUsername(String username);
    Page<AppUser> findByRole(UserRole role, Pageable pageable);
}
