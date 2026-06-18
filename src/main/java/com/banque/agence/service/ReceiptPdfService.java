package com.banque.agence.service;

import com.banque.agence.domain.entity.BillPayment;
import com.banque.agence.domain.entity.Transaction;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class ReceiptPdfService {

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.of("Africa/Casablanca"));

    private final Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
    private final Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private final Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

    public byte[] generate(Transaction transaction, Optional<BillPayment> billPayment) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            Paragraph title = new Paragraph("AmanaBank — Reçu d'opération", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(16f);
            document.add(title);

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            addRow(table, "N° opération", String.valueOf(transaction.getId()));
            addRow(table, "Date", DATE_TIME_FORMAT.format(transaction.getExecutedAt()));
            addRow(table, "Type", transaction.getType().getLabel());
            addRow(table, "Montant", transaction.getAmount() + " MAD");

            if (transaction.getSourceAccount() != null) {
                addRow(table, "Compte débité", transaction.getSourceAccount().getAccountNumber());
            }
            billPayment.ifPresent(bp -> {
                addRow(table, "Facturier", bp.getBillProvider().getName());
                addRow(table, "Référence", bp.getClientReference());
            });
            if (transaction.getDestinationAccount() != null) {
                addRow(table, "Compte destination", transaction.getDestinationAccount().getAccountNumber());
            }
            addRow(table, "Agent", transaction.getExecutedBy().getFullName());
            if (transaction.getDescription() != null && !transaction.getDescription().isBlank()) {
                addRow(table, "Description", transaction.getDescription());
            }

            document.add(table);

            Paragraph footer = new Paragraph(
                    "Document généré le " + DATE_TIME_FORMAT.format(java.time.Instant.now()),
                    valueFont);
            footer.setSpacingBefore(20f);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return out.toByteArray();
        } catch (DocumentException e) {
            throw new IllegalStateException("Impossible de générer le PDF du reçu.", e);
        }
    }

    private void addRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(PdfPCell.NO_BORDER);
        labelCell.setPadding(4f);
        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(PdfPCell.NO_BORDER);
        valueCell.setPadding(4f);
        table.addCell(labelCell);
        table.addCell(valueCell);
    }
}
