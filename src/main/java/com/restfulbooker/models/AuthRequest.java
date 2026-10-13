package com.restfulbooker.models;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Request body for POST /auth. Null fields are omitted so tests can send incomplete credentials. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthRequest {

    private String username;
    private String password;

    public AuthRequest() {
    }

    public AuthRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

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

    /** The password is deliberately masked so it never reaches logs or reports. */
    @Override
    public String toString() {
        return "AuthRequest{username='" + username + "', password='****'}";
    }
}
