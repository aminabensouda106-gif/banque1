package com.banque.agence.domain.enums;

public enum CheckbookSheetCount {
    FEUILLES_20(20),
    FEUILLES_40(40);

    private final int sheetCount;

    CheckbookSheetCount(int sheetCount) {
        this.sheetCount = sheetCount;
    }

    public int getSheetCount() {
        return sheetCount;
    }

    public String getLabel() {
        return sheetCount + " feuillets";
    }
}
