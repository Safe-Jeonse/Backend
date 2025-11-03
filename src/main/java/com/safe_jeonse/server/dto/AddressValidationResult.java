package com.safe_jeonse.server.dto;

public record AddressValidationResult(boolean valid, String lnbrMnnm, String lnbrSlno) {

    public boolean isValid() {
        return valid;
    }

    public String getLnbrMnnm() {
        return lnbrMnnm;
    }

    public String getLnbrSlno() {
        return lnbrSlno;
    }
}

