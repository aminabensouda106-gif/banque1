package com.banque.agence.repository;

import com.banque.agence.domain.entity.Client;
import com.banque.agence.domain.enums.ClientStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    @Query("""
            SELECT c FROM Client c
            WHERE c.portalEnabled = true
              AND c.passwordHash IS NOT NULL
              AND c.status = :status
              AND (LOWER(c.clientNumber) = LOWER(:login) OR LOWER(c.cin) = LOWER(:login))
            """)
    Optional<Client> findActivePortalClientByLogin(@Param("login") String login, @Param("status") ClientStatus status);

    default Optional<Client> findActivePortalClientByLogin(String login) {
        return findActivePortalClientByLogin(login, ClientStatus.ACTIVE);
    }

    long countByStatus(ClientStatus status);

    boolean existsByCin(String cin);

    boolean existsByCinAndIdNot(String cin, Long id);

    Optional<Client> findByCin(String cin);

    Optional<Client> findTopByOrderByIdDesc();

    @Query("""
            SELECT c FROM Client c
            WHERE :q IS NULL OR :q = '' OR
                  LOWER(c.firstName) LIKE LOWER(CONCAT('%', :q, '%')) OR
                  LOWER(c.lastName) LIKE LOWER(CONCAT('%', :q, '%')) OR
                  LOWER(CONCAT(c.firstName, ' ', c.lastName)) LIKE LOWER(CONCAT('%', :q, '%')) OR
                  LOWER(c.cin) LIKE LOWER(CONCAT('%', :q, '%')) OR
                  LOWER(c.clientNumber) LIKE LOWER(CONCAT('%', :q, '%')) OR
                  LOWER(c.phone) LIKE LOWER(CONCAT('%', :q, '%'))
            """)
    Page<Client> search(@Param("q") String q, Pageable pageable);
}
