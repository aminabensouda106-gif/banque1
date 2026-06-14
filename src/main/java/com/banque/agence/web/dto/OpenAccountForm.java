package com.banque.agence.web.dto;

import com.banque.agence.domain.enums.AccountType;
import jakarta.validation.constraints.NotNull;

public class OpenAccountForm {

    @NotNull(message = "Le type de compte est obligatoire.")
    private AccountType type;

    public AccountType getType() {
        return type;
    }

    public void setType(AccountType type) {
        this.type = type;
    }
}
