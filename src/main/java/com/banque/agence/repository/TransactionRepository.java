package com.banque.agence.repository;

import com.banque.agence.domain.entity.Transaction;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    @EntityGraph(attributePaths = {"executedBy", "sourceAccount", "destinationAccount"})
    @Query("SELECT t FROM Transaction t WHERE t.id = :id")
    Optional<Transaction> findByIdWithDetails(@Param("id") Long id);

    @EntityGraph(attributePaths = {"executedBy", "sourceAccount", "destinationAccount"})
    List<Transaction> findTop10ByOrderByExecutedAtDesc();

    long countByExecutedAtGreaterThanEqualAndExecutedAtLessThan(Instant from, Instant to);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.executedAt >= :from AND t.executedAt < :to")
    BigDecimal sumAmountByExecutedAtGreaterThanEqualAndExecutedAtLessThan(@Param("from") Instant from,
                                                                            @Param("to") Instant to);
}
