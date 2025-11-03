package com.safe_jeonse.server.service;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.Optional;

@Component
@RequestScope
public class AddressContext {

    private String lnbrMnnm;
    private String lnbrSlno;

    public Optional<String> getLnbrMnnm() {
        return Optional.ofNullable(lnbrMnnm);
    }

    public void setLnbrMnnm(String lnbrMnnm) {
        this.lnbrMnnm = lnbrMnnm;
    }

    public Optional<String> getLnbrSlno() {
        return Optional.ofNullable(lnbrSlno);
    }

    public void setLnbrSlno(String lnbrSlno) {
        this.lnbrSlno = lnbrSlno;
    }
}
