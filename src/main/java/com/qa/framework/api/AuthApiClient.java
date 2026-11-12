
package com.qa.framework.api;

import com.google.gson.JsonElement;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;

public class AuthApiClient {

    private final APIClient apiClient;

    public AuthApiClient(APIClient apiClient) {
        this.apiClient = apiClient;
    }

    public APIResponse login(JsonElement credentials) {
        return apiClient.post("/auth/login", credentials);
    }

    public APIResponse getCurrentUser(String accessToken) {

        RequestOptions options = RequestOptions.create()
                .setHeader("Authorization", "Bearer " + accessToken);

        return apiClient.get("/auth/me", options);
    }

    public APIResponse getCurrentUserWithoutToken() {
        return apiClient.get("/auth/me");
    }

    public APIResponse refreshToken(JsonElement refreshData) {
        return apiClient.post("/auth/refresh", refreshData);
    }
}
