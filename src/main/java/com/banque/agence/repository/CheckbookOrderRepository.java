package com.banque.agence.repository;

import com.banque.agence.domain.entity.CheckbookOrder;
import com.banque.agence.domain.enums.CheckbookOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CheckbookOrderRepository extends JpaRepository<CheckbookOrder, Long> {

    boolean existsByAccountIdAndStatus(Long accountId, CheckbookOrderStatus status);

    @EntityGraph(attributePaths = {"account", "account.client", "client", "requestedBy"})
    @Query("SELECT o FROM CheckbookOrder o WHERE o.id = :id")
    Optional<CheckbookOrder> findByIdWithDetails(@Param("id") Long id);

    @EntityGraph(attributePaths = {"account", "client", "requestedBy"})
    Page<CheckbookOrder> findAllByOrderByRequestedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"account", "client", "requestedBy"})
    Page<CheckbookOrder> findAllByStatusOrderByRequestedAtDesc(CheckbookOrderStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"account", "requestedBy"})
    List<CheckbookOrder> findByClientIdOrderByRequestedAtDesc(Long clientId);

    @EntityGraph(attributePaths = {"requestedBy"})
    List<CheckbookOrder> findByAccountIdOrderByRequestedAtDesc(Long accountId);

    Optional<CheckbookOrder> findTopByOrderByIdDesc();
}
