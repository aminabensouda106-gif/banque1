package com.banque.agence.repository;

import com.banque.agence.domain.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    @EntityGraph(attributePaths = "client")
    @Query("SELECT a FROM Account a WHERE a.client.id = :clientId ORDER BY a.openedAt DESC")
    List<Account> findByClientIdOrderByOpenedAtDesc(@Param("clientId") Long clientId);

    @EntityGraph(attributePaths = "client")
    Page<Account> findAllByOrderByOpenedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = "client")
    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Optional<Account> findByIdWithClient(@Param("id") Long id);

    Optional<Account> findTopByOrderByIdDesc();
}
