package com.banque.agence.web.dto;

import com.banque.agence.domain.enums.CheckbookOrderStatus;

public class CheckbookOrderFilterForm {

    private CheckbookOrderStatus status;

    public CheckbookOrderStatus getStatus() {
        return status;
    }

    public void setStatus(CheckbookOrderStatus status) {
        this.status = status;
    }
}
