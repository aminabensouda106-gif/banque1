package com.banque.agence.repository;

import com.banque.agence.domain.entity.Transaction;
import com.banque.agence.domain.enums.TransactionType;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class TransactionSpecifications {

    private TransactionSpecifications() {
    }

    public static Specification<Transaction> withFilters(TransactionType type,
                                                         String accountNumber,
                                                         Long userId,
                                                         Instant from,
                                                         Instant to) {
        return (root, query, cb) -> {
            if (!Long.class.equals(query.getResultType()) && !long.class.equals(query.getResultType())) {
                root.fetch("executedBy", JoinType.INNER);
                root.fetch("sourceAccount", JoinType.LEFT);
                root.fetch("destinationAccount", JoinType.LEFT);
                query.distinct(true);
            }

            List<Predicate> predicates = new ArrayList<>();

            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            if (userId != null) {
                predicates.add(cb.equal(root.get("executedBy").get("id"), userId));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("executedAt"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThan(root.get("executedAt"), to));
            }
            if (accountNumber != null && !accountNumber.isBlank()) {
                String pattern = "%" + accountNumber.trim().toLowerCase() + "%";
                var sourceJoin = root.join("sourceAccount", JoinType.LEFT);
                var destinationJoin = root.join("destinationAccount", JoinType.LEFT);
                predicates.add(cb.or(
                        cb.like(cb.lower(sourceJoin.get("accountNumber")), pattern),
                        cb.like(cb.lower(destinationJoin.get("accountNumber")), pattern)
                ));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    public static Specification<Transaction> forClient(Long clientId,
                                                         TransactionType type,
                                                         Instant from,
                                                         Instant to) {
        return (root, query, cb) -> {
            if (!Long.class.equals(query.getResultType()) && !long.class.equals(query.getResultType())) {
                root.fetch("executedBy", JoinType.INNER);
                root.fetch("sourceAccount", JoinType.LEFT);
                root.fetch("destinationAccount", JoinType.LEFT);
                query.distinct(true);
            }

            List<Predicate> predicates = new ArrayList<>();
            var sourceJoin = root.join("sourceAccount", JoinType.LEFT);
            var destinationJoin = root.join("destinationAccount", JoinType.LEFT);

            predicates.add(cb.or(
                    cb.equal(sourceJoin.get("client").get("id"), clientId),
                    cb.equal(destinationJoin.get("client").get("id"), clientId)
            ));

            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("executedAt"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThan(root.get("executedAt"), to));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
