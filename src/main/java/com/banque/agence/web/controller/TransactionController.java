package com.banque.agence.web.controller;

import com.banque.agence.domain.entity.Transaction;
import com.banque.agence.domain.enums.TransactionType;
import com.banque.agence.repository.UserRepository;
import com.banque.agence.service.BillPaymentService;
import com.banque.agence.service.ReceiptFileNameBuilder;
import com.banque.agence.service.ReceiptPdfService;
import com.banque.agence.service.TransactionService;
import com.banque.agence.web.ReceiptDownloadResponse;
import com.banque.agence.web.dto.TransactionFilterForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/transactions")
public class TransactionController {

    private static final int PAGE_SIZE = 15;

    private final TransactionService transactionService;
    private final UserRepository userRepository;
    private final BillPaymentService billPaymentService;
    private final ReceiptPdfService receiptPdfService;
    private final ReceiptFileNameBuilder receiptFileNameBuilder;

    public TransactionController(TransactionService transactionService,
                                 UserRepository userRepository,
                                 BillPaymentService billPaymentService,
                                 ReceiptPdfService receiptPdfService,
                                 ReceiptFileNameBuilder receiptFileNameBuilder) {
        this.transactionService = transactionService;
        this.userRepository = userRepository;
        this.billPaymentService = billPaymentService;
        this.receiptPdfService = receiptPdfService;
        this.receiptFileNameBuilder = receiptFileNameBuilder;
    }

    @GetMapping
    public String list(
            @ModelAttribute TransactionFilterForm filter,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {
        Page<Transaction> transactions = transactionService.search(
                filter.getType(),
                filter.getAccountNumber(),
                filter.getUserId(),
                filter.getFromDate(),
                filter.getToDate(),
                PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "executedAt"))
        );

        model.addAttribute("pageTitle", "Historique des opérations");
        model.addAttribute("activePage", "transactions");
        model.addAttribute("transactions", transactions);
        model.addAttribute("filter", filter);
        model.addAttribute("transactionTypes", TransactionType.values());
        model.addAttribute("users", userRepository.findAll());
        return "transactions/list";
    }

    @GetMapping("/{id}/receipt")
    public String receipt(@PathVariable Long id, Model model) {
        var transaction = transactionService.findById(id);
        model.addAttribute("transaction", transaction);
        billPaymentService.findByTransactionId(id).ifPresent(bp -> model.addAttribute("billPayment", bp));
        model.addAttribute("backUrl", "/transactions");
        model.addAttribute("pdfUrl", "/transactions/" + id + "/receipt.pdf");
        model.addAttribute("receiptFileName", receiptFileNameBuilder.build(transaction));
        return "transactions/receipt";
    }

    @GetMapping("/{id}/receipt.pdf")
    public ResponseEntity<byte[]> receiptPdf(@PathVariable Long id) {
        var transaction = transactionService.findById(id);
        var billPayment = billPaymentService.findByTransactionId(id);
        byte[] pdf = receiptPdfService.generate(transaction, billPayment);
        return ReceiptDownloadResponse.pdf(pdf, transaction, receiptFileNameBuilder);
    }
}
