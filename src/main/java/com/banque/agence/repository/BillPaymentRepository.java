package com.banque.agence.repository;

import com.banque.agence.domain.entity.BillPayment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BillPaymentRepository extends JpaRepository<BillPayment, Long> {

    @EntityGraph(attributePaths = {"billProvider", "account"})
    @Query("SELECT bp FROM BillPayment bp WHERE bp.transaction.id = :transactionId")
    Optional<BillPayment> findWithDetailsByTransactionId(@Param("transactionId") Long transactionId);
}
