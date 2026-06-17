package com.banque.agence.web.dto;

import com.banque.agence.domain.enums.CheckbookSheetCount;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CheckbookOrderForm {

    @NotNull(message = "Le compte est obligatoire.")
    private Long accountId;

    @Min(value = 1, message = "La quantité doit être au moins 1.")
    @Max(value = 10, message = "La quantité ne peut pas dépasser 10.")
    private int quantity = 1;

    @NotNull(message = "Le format du chéquier est obligatoire.")
    private CheckbookSheetCount sheetCount = CheckbookSheetCount.FEUILLES_20;

    @Size(max = 255, message = "Les notes ne peuvent pas dépasser 255 caractères.")
    private String notes;

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public CheckbookSheetCount getSheetCount() {
        return sheetCount;
    }

    public void setSheetCount(CheckbookSheetCount sheetCount) {
        this.sheetCount = sheetCount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
