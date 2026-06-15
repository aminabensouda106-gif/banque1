package com.banque.agence.repository;

import com.banque.agence.domain.entity.User;
import com.banque.agence.domain.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByUsernameAndIdNot(String username, Long id);

    long countByRoleAndEnabled(UserRole role, boolean enabled);
}
