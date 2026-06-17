package com.banque.agence.repository;

import com.banque.agence.domain.entity.BillProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BillProviderRepository extends JpaRepository<BillProvider, Long> {

    List<BillProvider> findAllByActiveTrueOrderByNameAsc();

    Optional<BillProvider> findByIdAndActiveTrue(Long id);
}
