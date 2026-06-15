package com.banque.agence.web.dto;

import com.banque.agence.domain.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UserCreateForm {

    @NotBlank(message = "L'identifiant est obligatoire.")
    @Size(max = 50, message = "L'identifiant ne doit pas dépasser 50 caractères.")
    private String username;

    @NotBlank(message = "Le mot de passe est obligatoire.")
    @Size(min = 6, max = 100, message = "Le mot de passe doit contenir entre 6 et 100 caractères.")
    private String password;

    @NotBlank(message = "Le nom complet est obligatoire.")
    @Size(max = 100, message = "Le nom complet ne doit pas dépasser 100 caractères.")
    private String fullName;

    @Email(message = "L'adresse e-mail n'est pas valide.")
    @Size(max = 100, message = "L'e-mail ne doit pas dépasser 100 caractères.")
    private String email;

    @NotNull(message = "Le rôle est obligatoire.")
    private UserRole role;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}
