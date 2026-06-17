package com.banque.agence.web.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class BillPaymentForm {

    @NotNull(message = "Le compte est obligatoire.")
    private Long accountId;

    @NotNull(message = "Le facturier est obligatoire.")
    private Long providerId;

    @NotBlank(message = "La référence client est obligatoire.")
    @Size(max = 50, message = "La référence ne peut pas dépasser 50 caractères.")
    private String clientReference;

    @NotNull(message = "Le montant est obligatoire.")
    @DecimalMin(value = "0.01", message = "Le montant doit être supérieur à zéro.")
    @DecimalMax(value = "999999999999999.9999", message = "Le montant est trop élevé.")
    @Digits(integer = 15, fraction = 4, message = "Format de montant invalide.")
    private BigDecimal amount;

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }

    public String getClientReference() {
        return clientReference;
    }

    public void setClientReference(String clientReference) {
        this.clientReference = clientReference;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
