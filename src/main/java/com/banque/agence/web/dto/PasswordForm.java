package com.banque.agence.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PasswordForm {

    @NotBlank(message = "Le mot de passe est obligatoire.")
    @Size(min = 6, max = 100, message = "Le mot de passe doit contenir entre 6 et 100 caractères.")
    private String password;

    @NotBlank(message = "La confirmation est obligatoire.")
    private String confirmPassword;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public boolean isMatching() {
        return password != null && password.equals(confirmPassword);
    }
}
