package com.qa.framework.api;

import com.google.gson.JsonElement;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.RequestOptions;
import com.qa.framework.config.ConfigManager;

import java.util.Map;

public class APIClient {

    private final Playwright playwright;
    private final APIRequestContext apiRequestContext;

    public APIClient() {

        String baseUrl = ConfigManager.getProperty("api_base_url");
        int timeout = ConfigManager.getIntProperty("api_timeout");

        playwright = Playwright.create();

        APIRequest.NewContextOptions options =
                new APIRequest.NewContextOptions()
                        .setBaseURL(baseUrl)
                        .setTimeout(timeout)
                        .setExtraHTTPHeaders(
                                Map.of("Accept", "application/json")
                        );

        apiRequestContext = playwright.request().newContext(options);
    }

    // GET - Retrieve data
    public APIResponse get(String endpoint) {
        return apiRequestContext.get(endpoint);
    }

    // POST - Create data
    public APIResponse post(String endpoint, JsonElement requestBody) {
        return apiRequestContext.post(
                endpoint,
                getJsonRequestOptions(requestBody)
        );
    }

    // PUT - Replace existing data
    public APIResponse put(String endpoint, JsonElement requestBody) {
        return apiRequestContext.put(
                endpoint,
                getJsonRequestOptions(requestBody)
        );
    }

    // PATCH - Partially update data
    public APIResponse patch(String endpoint, JsonElement requestBody) {
        return apiRequestContext.patch(
                endpoint,
                getJsonRequestOptions(requestBody)
        );
    }

    // DELETE - Delete data
    public APIResponse delete(String endpoint) {
        return apiRequestContext.delete(endpoint);
    }

    // Common JSON request configuration
    private RequestOptions getJsonRequestOptions(JsonElement requestBody) {

        return RequestOptions.create()
                .setData(requestBody.toString())
                .setHeader("Content-Type", "application/json");
    }

    // Expose raw context when advanced configuration is needed
    public APIRequestContext getApiRequestContext() {
        return apiRequestContext;
    }

    // Close resources
    public void close() {
        try {
            apiRequestContext.dispose();
        } finally {
            playwright.close();
        }
    }

    public APIResponse get(String endpoint, RequestOptions options) {
        return apiRequestContext.get(endpoint, options);
    }
}
