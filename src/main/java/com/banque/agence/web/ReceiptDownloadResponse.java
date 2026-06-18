package com.banque.agence.web;

import com.banque.agence.domain.entity.Transaction;
import com.banque.agence.service.ReceiptFileNameBuilder;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;

public final class ReceiptDownloadResponse {

    private ReceiptDownloadResponse() {
    }

    public static ResponseEntity<byte[]> pdf(byte[] pdfBytes, Transaction transaction,
                                             ReceiptFileNameBuilder fileNameBuilder) {
        String fileName = fileNameBuilder.build(transaction);

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(fileName, StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
