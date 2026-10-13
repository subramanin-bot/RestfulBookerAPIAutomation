package com.restfulbooker.utils;

import com.restfulbooker.clients.AuthClient;

/** Fetches an auth token once and reuses it for the rest of the run. */
public final class TokenManager {

    private static String token;

    private TokenManager() {
    }

    public static synchronized String getToken() {
        if (token == null) {
            token = new AuthClient().fetchToken();
        }
        return token;
    }

    /** Forces the next {@link #getToken()} call to log in again. */
    public static synchronized void invalidate() {
        token = null;
    }
}
