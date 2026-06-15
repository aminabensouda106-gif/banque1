package com.banque.agence.web.dto;

import com.banque.agence.domain.entity.User;
import com.banque.agence.domain.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UserEditForm {

    @NotBlank(message = "Le nom complet est obligatoire.")
    @Size(max = 100, message = "Le nom complet ne doit pas dépasser 100 caractères.")
    private String fullName;

    @Email(message = "L'adresse e-mail n'est pas valide.")
    @Size(max = 100, message = "L'e-mail ne doit pas dépasser 100 caractères.")
    private String email;

    @NotNull(message = "Le rôle est obligatoire.")
    private UserRole role;

    public static UserEditForm fromUser(User user) {
        UserEditForm form = new UserEditForm();
        form.setFullName(user.getFullName());
        form.setEmail(user.getEmail());
        form.setRole(user.getRole());
        return form;
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
