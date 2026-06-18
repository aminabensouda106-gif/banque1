package com.banque.agence.service;

import com.banque.agence.domain.entity.BillPayment;
import com.banque.agence.domain.entity.Transaction;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class ReceiptFileNameBuilder {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final ZoneId ZONE = ZoneId.of("Africa/Casablanca");

    /**
     * Format demandé : reçu-NOMDERECU-date.pdf
     * NOMDERECU = type d'opération (ex. DEPOT, VIREMENT, PAIEMENT_FACTURE)
     */
    public String build(Transaction transaction) {
        String receiptName = transaction.getType().name();
        String date = DATE_FORMAT.format(transaction.getExecutedAt().atZone(ZONE));
        return "reçu-" + receiptName + "-" + date + ".pdf";
    }
}
