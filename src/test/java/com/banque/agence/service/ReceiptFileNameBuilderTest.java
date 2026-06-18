package com.banque.agence.service;

import com.banque.agence.domain.entity.Transaction;
import com.banque.agence.domain.enums.TransactionType;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ReceiptFileNameBuilderTest {

    private final ReceiptFileNameBuilder builder = new ReceiptFileNameBuilder();

    @Test
    void buildsReceiptFileNameWithTypeAndDate() {
        Transaction transaction = new Transaction();
        transaction.setType(TransactionType.VIREMENT);
        transaction.setExecutedAt(Instant.parse("2026-06-14T10:30:00Z"));

        assertThat(builder.build(transaction)).isEqualTo("reçu-VIREMENT-2026-06-14.pdf");
    }
}
