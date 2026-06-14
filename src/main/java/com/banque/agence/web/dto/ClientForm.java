package com.banque.agence.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ClientForm {

    @NotBlank(message = "Le prénom est obligatoire.")
    @Size(max = 50, message = "Le prénom ne doit pas dépasser 50 caractères.")
    private String firstName;

    @NotBlank(message = "Le nom est obligatoire.")
    @Size(max = 50, message = "Le nom ne doit pas dépasser 50 caractères.")
    private String lastName;

    @NotBlank(message = "Le CIN est obligatoire.")
    @Size(max = 20, message = "Le CIN ne doit pas dépasser 20 caractères.")
    private String cin;

    @Email(message = "L'adresse e-mail n'est pas valide.")
    @Size(max = 100, message = "L'e-mail ne doit pas dépasser 100 caractères.")
    private String email;

    @Size(max = 20, message = "Le téléphone ne doit pas dépasser 20 caractères.")
    private String phone;

    @Size(max = 255, message = "L'adresse ne doit pas dépasser 255 caractères.")
    private String address;

    @Size(max = 2000, message = "Les informations professionnelles sont trop longues.")
    private String professionalInfo;

    public static ClientForm fromClient(com.banque.agence.domain.entity.Client client) {
        ClientForm form = new ClientForm();
        form.setFirstName(client.getFirstName());
        form.setLastName(client.getLastName());
        form.setCin(client.getCin());
        form.setEmail(client.getEmail());
        form.setPhone(client.getPhone());
        form.setAddress(client.getAddress());
        form.setProfessionalInfo(client.getProfessionalInfo());
        return form;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getCin() {
        return cin;
    }

    public void setCin(String cin) {
        this.cin = cin;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getProfessionalInfo() {
        return professionalInfo;
    }

    public void setProfessionalInfo(String professionalInfo) {
        this.professionalInfo = professionalInfo;
    }
}
