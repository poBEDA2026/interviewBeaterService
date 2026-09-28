package com.github.interviewbeaterservice.auth;

public enum TokenType {
    ACCESS("access"),
    REFRESH("refresh");

    private final String claim;

    TokenType(String claim)
    {
        this.claim = claim;
    }

    String claim()
    {
        return claim;
    }
}
