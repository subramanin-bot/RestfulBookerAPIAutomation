package com.restfulbooker.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Response body of POST /auth.
 * A successful call returns {@code token}; invalid credentials return HTTP 200
 * with a {@code reason} field instead.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthResponse {

    private String token;
    private String reason;

    public AuthResponse() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "AuthResponse{token=" + (token == null ? "null" : "****") + ", reason='" + reason + "'}";
    }
}
