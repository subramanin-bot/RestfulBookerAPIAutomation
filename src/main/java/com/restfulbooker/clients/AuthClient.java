package com.restfulbooker.clients;

import com.restfulbooker.config.ConfigReader;
import com.restfulbooker.constants.FrameworkConstants;
import com.restfulbooker.models.AuthRequest;
import com.restfulbooker.models.AuthResponse;
import com.restfulbooker.specs.RequestSpecFactory;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

/** Client for POST /auth. */
public class AuthClient {

    /** Sends the credentials and returns the raw response so tests can assert on it. */
    public Response login(AuthRequest request) {
        return given()
                .spec(RequestSpecFactory.baseSpec())
                .body(request)
                .when()
                .post(FrameworkConstants.AUTH_ENDPOINT);
    }

    public Response login(String username, String password) {
        return login(new AuthRequest(username, password));
    }

    /** Logs in with the configured credentials and returns the token, or fails with a clear message. */
    public String fetchToken() {
        Response response = login(ConfigReader.getUsername(), ConfigReader.getPassword());
        AuthResponse body = response.statusCode() == 200 ? response.as(AuthResponse.class) : null;
        if (body == null || body.getToken() == null || body.getToken().isBlank()) {
            String reason = body == null ? "HTTP " + response.statusCode() : String.valueOf(body.getReason());
            throw new IllegalStateException("Could not obtain an auth token (" + reason
                    + "). Check RB_USERNAME / RB_PASSWORD and the base URL.");
        }
        return body.getToken();
    }
}
