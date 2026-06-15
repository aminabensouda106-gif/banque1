package com.banque.agence.repository;

import com.banque.agence.domain.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @EntityGraph(attributePaths = "performedBy")
    Page<AuditLog> findAllByOrderByPerformedAtDesc(Pageable pageable);
}
