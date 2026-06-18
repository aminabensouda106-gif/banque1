package com.banque.agence.service;

import com.banque.agence.domain.entity.BillPayment;
import com.banque.agence.domain.entity.Transaction;
import com.lowagie.text.BaseColor;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

@Service
public class ReceiptPdfService {

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.of("Africa/Casablanca"));

    private static final BaseColor BROWN_900 = new BaseColor(75, 54, 33);
    private static final BaseColor GOLD_500 = new BaseColor(166, 137, 58);
    private static final BaseColor BEIGE_100 = new BaseColor(249, 246, 241);
    private static final BaseColor BEIGE_300 = new BaseColor(221, 210, 195);
    private static final BaseColor TEXT_MUTED = new BaseColor(107, 91, 79);
    private static final BaseColor TEXT = new BaseColor(58, 46, 34);

    private static final DecimalFormat AMOUNT_FORMAT = amountFormat();

    private final Font receiptTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BROWN_900);
    private final Font receiptSubtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 9, TEXT_MUTED);
    private final Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.WHITE);
    private final Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, TEXT_MUTED);
    private final Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 10, TEXT);
    private final Font amountFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BROWN_900);
    private final Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 8, TEXT_MUTED);
    private final Font footerBrandFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, GOLD_500);

    public byte[] generate(Transaction transaction, Optional<BillPayment> billPayment) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            Document document = new Document(PageSize.A4, 52, 52, 48, 52);
            PdfWriter.getInstance(document, out);
            document.open();

            document.add(buildHeader(transaction));
            document.add(spacer(14f));
            document.add(buildDetailsTable(transaction, billPayment));
            document.add(spacer(22f));
            document.add(buildFooter());

            document.close();
            return out.toByteArray();
        } catch (DocumentException | IOException e) {
            throw new IllegalStateException("Impossible de générer le PDF du reçu.", e);
        }
    }

    private PdfPTable buildHeader(Transaction transaction) throws DocumentException, IOException {
        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);
        header.setSpacingAfter(0f);

        header.addCell(accentBar(GOLD_500, 3f));
        header.addCell(spacerCell(12f));

        Image logo = loadLogo();
        PdfPCell logoCell = new PdfPCell(logo, true);
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        logoCell.setPaddingBottom(6f);
        header.addCell(logoCell);

        PdfPCell titleCell = cell(new Phrase("Reçu d'opération", receiptTitleFont), Element.ALIGN_CENTER);
        titleCell.setPaddingBottom(2f);
        header.addCell(titleCell);

        PdfPCell refCell = cell(
                new Phrase("N° " + transaction.getId() + " · " + transaction.getType().getLabel(),
                        receiptSubtitleFont),
                Element.ALIGN_CENTER);
        refCell.setPaddingBottom(10f);
        header.addCell(refCell);

        header.addCell(accentBar(BROWN_900, 1.5f));
        return header;
    }

    private PdfPTable buildDetailsTable(Transaction transaction, Optional<BillPayment> billPayment)
            throws DocumentException {
        PdfPTable wrapper = new PdfPTable(1);
        wrapper.setWidthPercentage(100);

        PdfPCell sectionCell = new PdfPCell(new Phrase("Détails de l'opération", sectionFont));
        sectionCell.setBackgroundColor(BROWN_900);
        sectionCell.setBorder(Rectangle.NO_BORDER);
        sectionCell.setPaddingTop(7f);
        sectionCell.setPaddingBottom(7f);
        sectionCell.setPaddingLeft(10f);
        sectionCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        wrapper.addCell(sectionCell);

        PdfPTable rows = new PdfPTable(2);
        rows.setWidthPercentage(100);
        rows.setWidths(new float[]{38f, 62f});

        int rowIndex = 0;
        rowIndex = addDetailRow(rows, "Date", DATE_TIME_FORMAT.format(transaction.getExecutedAt()), rowIndex);
        rowIndex = addAmountRow(rows, formatAmount(transaction.getAmount()), rowIndex);

        if (transaction.getSourceAccount() != null) {
            rowIndex = addDetailRow(rows, "Compte débité",
                    transaction.getSourceAccount().getAccountNumber(), rowIndex);
        }
        if (billPayment.isPresent()) {
            BillPayment bp = billPayment.get();
            rowIndex = addDetailRow(rows, "Facturier", bp.getBillProvider().getName(), rowIndex);
            rowIndex = addDetailRow(rows, "Référence", bp.getClientReference(), rowIndex);
        }
        if (transaction.getDestinationAccount() != null) {
            rowIndex = addDetailRow(rows, "Compte destination",
                    transaction.getDestinationAccount().getAccountNumber(), rowIndex);
        }
        rowIndex = addDetailRow(rows, "Agent", transaction.getExecutedBy().getFullName(), rowIndex);
        if (transaction.getDescription() != null && !transaction.getDescription().isBlank()) {
            addDetailRow(rows, "Description", transaction.getDescription(), rowIndex);
        }

        PdfPCell rowsCell = new PdfPCell(rows);
        rowsCell.setBorder(Rectangle.BOX);
        rowsCell.setBorderColor(BEIGE_300);
        rowsCell.setPadding(0f);
        wrapper.addCell(rowsCell);

        return wrapper;
    }

    private PdfPTable buildFooter() {
        PdfPTable footer = new PdfPTable(1);
        footer.setWidthPercentage(100);

        footer.addCell(accentBar(BEIGE_300, 1f));

        PdfPCell generatedCell = cell(
                new Phrase("Document généré le " + DATE_TIME_FORMAT.format(Instant.now()), footerFont),
                Element.ALIGN_CENTER);
        generatedCell.setPaddingTop(10f);
        generatedCell.setPaddingBottom(4f);
        footer.addCell(generatedCell);

        PdfPCell brandCell = cell(new Phrase("AmanaBank — Votre banque de confiance", footerBrandFont),
                Element.ALIGN_CENTER);
        brandCell.setPaddingBottom(2f);
        footer.addCell(brandCell);

        return footer;
    }

    private int addDetailRow(PdfPTable table, String label, String value, int rowIndex) {
        BaseColor rowBg = rowIndex % 2 == 0 ? BEIGE_100 : BaseColor.WHITE;

        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        styleDataCell(labelCell, rowBg);
        labelCell.setPaddingLeft(10f);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        styleDataCell(valueCell, rowBg);
        valueCell.setPaddingRight(10f);

        table.addCell(labelCell);
        table.addCell(valueCell);
        return rowIndex + 1;
    }

    private int addAmountRow(PdfPTable table, String amount, int rowIndex) {
        BaseColor rowBg = rowIndex % 2 == 0 ? BEIGE_100 : BaseColor.WHITE;

        PdfPCell labelCell = new PdfPCell(new Phrase("Montant", labelFont));
        styleDataCell(labelCell, rowBg);
        labelCell.setPaddingLeft(10f);

        PdfPCell valueCell = new PdfPCell(new Phrase(amount, amountFont));
        styleDataCell(valueCell, rowBg);
        valueCell.setPaddingRight(10f);
        valueCell.setPaddingTop(6f);
        valueCell.setPaddingBottom(6f);

        table.addCell(labelCell);
        table.addCell(valueCell);
        return rowIndex + 1;
    }

    private void styleDataCell(PdfPCell cell, BaseColor background) {
        cell.setBackgroundColor(background);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setBorderWidthBottom(0.5f);
        cell.setBorderColorBottom(BEIGE_300);
        cell.setPaddingTop(7f);
        cell.setPaddingBottom(7f);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
    }

    private PdfPCell accentBar(BaseColor color, float height) {
        PdfPCell cell = new PdfPCell();
        cell.setFixedHeight(height);
        cell.setBackgroundColor(color);
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }

    private PdfPCell spacerCell(float height) {
        PdfPCell cell = new PdfPCell();
        cell.setFixedHeight(height);
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }

    private PdfPCell cell(Phrase phrase, int alignment) {
        PdfPCell cell = new PdfPCell(phrase);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(alignment);
        return cell;
    }

    private Paragraph spacer(float height) {
        Paragraph p = new Paragraph(" ");
        p.setSpacingBefore(0f);
        p.setSpacingAfter(height);
        p.setLeading(0f);
        return p;
    }

    private Image loadLogo() throws IOException, DocumentException {
        ClassPathResource resource = new ClassPathResource("static/images/amana-logo.png");
        try (InputStream in = resource.getInputStream()) {
            Image logo = Image.getInstance(in.readAllBytes());
            logo.scaleToFit(170, 38);
            logo.setAlignment(Element.ALIGN_CENTER);
            return logo;
        }
    }

    private static String formatAmount(BigDecimal amount) {
        return AMOUNT_FORMAT.format(amount) + " MAD";
    }

    private static DecimalFormat amountFormat() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.FRANCE);
        symbols.setDecimalSeparator('.');
        symbols.setGroupingSeparator(' ');
        return new DecimalFormat("#,##0.00", symbols);
    }
}
