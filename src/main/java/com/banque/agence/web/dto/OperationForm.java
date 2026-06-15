package com.banque.agence.web.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class OperationForm {

    @NotNull(message = "Le compte est obligatoire.")
    private Long accountId;

    @NotNull(message = "Le montant est obligatoire.")
    @DecimalMin(value = "0.01", message = "Le montant doit être supérieur à zéro.")
    @DecimalMax(value = "999999999999999.9999", message = "Le montant est trop élevé.")
    @Digits(integer = 15, fraction = 4, message = "Format de montant invalide.")
    private BigDecimal amount;

    private String description;

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
