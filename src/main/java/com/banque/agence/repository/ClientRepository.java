package com.banque.agence.repository;

import com.banque.agence.domain.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    boolean existsByCin(String cin);

    boolean existsByCinAndIdNot(String cin, Long id);

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
