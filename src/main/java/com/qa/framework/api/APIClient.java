
package com.qa.framework.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.RequestOptions;
import com.qa.framework.config.ConfigManager;
import io.qameta.allure.Allure;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class APIClient {

    private static final Logger logger =
            Logger.getLogger(APIClient.class.getName());

    private static final Gson gson =
            new GsonBuilder().setPrettyPrinting().create();

    private static final Path REPORT_FILE =
            Path.of("target", "reports", "api", "api-requests.json");

    private static final List<Map<String, Object>> requestRecords =
            new CopyOnWriteArrayList<>();

    // Associates API requests with the currently executing TestNG test.
    private static final ThreadLocal<String> CURRENT_TEST_NAME =
            new ThreadLocal<>();

    private static final Set<String> SENSITIVE_KEYS = Set.of(
            // Authentication
            "password",
            "accesstoken",
            "refreshtoken",
            "token",
            "authorization",
            "cookie",
            "secret",
            "apikey",

            // Personal information
            "email",
            "phone",
            "birthdate",
            "ssn",
            "ip",
            "macaddress",
            "address",

            // Financial information
            "cardnumber",
            "cardexpire",
            "iban",

            // Other sensitive information
            "wallet",
            "ein",
            "maidenname"
    );

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

    // --------------------------------------------------
    // Test-level reporting support
    // --------------------------------------------------

    public static void setCurrentTestName(String testName) {
        CURRENT_TEST_NAME.set(testName);
    }

    public static void clearCurrentTestName() {
        CURRENT_TEST_NAME.remove();
    }

    public static List<Map<String, Object>> getRequestRecords() {
        return new ArrayList<>(requestRecords);
    }

    public static void clearRequestRecords() {

        requestRecords.clear();

        try {
            Files.deleteIfExists(REPORT_FILE);
        } catch (Exception e) {
            logger.log(
                    Level.WARNING,
                    "Could not clear previous API report.",
                    e
            );
        }
    }

    // --------------------------------------------------
    // HTTP methods
    // --------------------------------------------------

    // GET
    public APIResponse get(String endpoint) {
        return execute("GET", endpoint, null,
                () -> apiRequestContext.get(endpoint));
    }

    // POST
    public APIResponse post(String endpoint, JsonElement requestBody) {
        return execute("POST", endpoint, requestBody,
                () -> apiRequestContext.post(
                        endpoint, getJsonRequestOptions(requestBody)));
    }

    // PUT
    public APIResponse put(String endpoint, JsonElement requestBody) {
        return execute("PUT", endpoint, requestBody,
                () -> apiRequestContext.put(
                        endpoint, getJsonRequestOptions(requestBody)));
    }

    // PATCH
    public APIResponse patch(String endpoint, JsonElement requestBody) {
        return execute("PATCH", endpoint, requestBody,
                () -> apiRequestContext.patch(
                        endpoint, getJsonRequestOptions(requestBody)));
    }

    // DELETE
    public APIResponse delete(String endpoint) {
        return execute("DELETE", endpoint, null,
                () -> apiRequestContext.delete(endpoint));
    }

    // GET with additional request options, e.g. Authorization header
    public APIResponse get(String endpoint, RequestOptions options) {
        return execute("GET", endpoint, null,
                () -> apiRequestContext.get(endpoint, options));
    }

    // --------------------------------------------------
    // Central API execution and reporting
    // --------------------------------------------------

    private APIResponse execute(
            String method,
            String endpoint,
            JsonElement requestBody,
            ApiCall apiCall) {

        long startTime = System.nanoTime();

        String safeRequest = sanitize(requestBody == null
                ? "No request body"
                : requestBody.toString());

        logger.info("API Request: " + method + " " + endpoint);

        try {

            APIResponse response = apiCall.execute();

            long durationMs =
                    (System.nanoTime() - startTime) / 1_000_000;

            String responseBody = response.text();
            String safeResponse = sanitize(responseBody);

            Map<String, Object> record = new LinkedHashMap<>();

            record.put("timestampUtc", Instant.now().toString());
            record.put("testName", CURRENT_TEST_NAME.get());
            record.put("method", method);
            record.put("endpoint", endpoint);
            record.put("requestBody", safeRequest);
            record.put("statusCode", response.status());
            record.put("durationMs", durationMs);
            record.put("responseBody", safeResponse);

            saveRecord(record);

            attachToAllure(
                    method,
                    endpoint,
                    safeRequest,
                    response.status(),
                    durationMs,
                    safeResponse
            );

            logger.info("API Response: " + response.status()
                    + " | Duration: " + durationMs + " ms");

            return response;

        } catch (Exception e) {

            long durationMs =
                    (System.nanoTime() - startTime) / 1_000_000;

            Map<String, Object> record = new LinkedHashMap<>();

            record.put("timestampUtc", Instant.now().toString());
            record.put("testName", CURRENT_TEST_NAME.get());
            record.put("method", method);
            record.put("endpoint", endpoint);
            record.put("requestBody", safeRequest);
            record.put("durationMs", durationMs);
            record.put("errorType", e.getClass().getName());
            record.put("errorMessage", sanitize(e.getMessage()));

            saveRecord(record);

            logger.log(
                    Level.SEVERE,
                    "API request failed: " + method + " " + endpoint,
                    e
            );

            throw e;
        }
    }

    // --------------------------------------------------
    // Sensitive-data sanitisation
    // --------------------------------------------------

    private String sanitize(String content) {

        if (content == null || content.isBlank()) {
            return content;
        }

        try {

            JsonElement json = JsonParser.parseString(content);
            sanitizeJson(json);

            return gson.toJson(json);

        } catch (Exception e) {

            // Also mask simple bearer-token strings in non-JSON text.
            return content.replaceAll(
                    "(?i)Bearer\\s+[^\\s\"']+",
                    "Bearer ********"
            );
        }
    }

    private void sanitizeJson(JsonElement element) {

        if (element.isJsonObject()) {

            JsonObject object = element.getAsJsonObject();

            for (String key : new ArrayList<>(object.keySet())) {

                String normalizedKey = key
                        .replaceAll("[^a-zA-Z0-9]", "")
                        .toLowerCase();

                if (SENSITIVE_KEYS.contains(normalizedKey)) {

                    object.addProperty(key, "********");

                } else {

                    sanitizeJson(object.get(key));
                }
            }

        } else if (element.isJsonArray()) {

            element.getAsJsonArray().forEach(this::sanitizeJson);
        }
    }

    // --------------------------------------------------
    // Save API request records
    // --------------------------------------------------

    private synchronized void saveRecord(Map<String, Object> record) {

        try {

            Files.createDirectories(REPORT_FILE.getParent());

            requestRecords.add(record);

            Files.writeString(
                    REPORT_FILE,
                    gson.toJson(requestRecords),
                    StandardCharsets.UTF_8
            );

        } catch (Exception e) {

            logger.log(
                    Level.WARNING,
                    "Could not save API reporting JSON.",
                    e
            );
        }
    }

    // --------------------------------------------------
    // Allure attachment
    // --------------------------------------------------

    private void attachToAllure(
            String method,
            String endpoint,
            String requestBody,
            int status,
            long durationMs,
            String responseBody) {

        String evidence =
                "Method: " + method
                        + "\nEndpoint: " + endpoint
                        + "\nStatus: " + status
                        + "\nDuration: " + durationMs + " ms"
                        + "\n\nRequest:\n" + requestBody
                        + "\n\nResponse:\n" + responseBody;

        Allure.addAttachment(
                "API Request and Response",
                "text/plain",
                new ByteArrayInputStream(
                        evidence.getBytes(StandardCharsets.UTF_8)
                ),
                ".txt"
        );
    }

    // --------------------------------------------------
    // Request options
    // --------------------------------------------------

    private RequestOptions getJsonRequestOptions(JsonElement requestBody) {

        return RequestOptions.create()
                .setData(requestBody.toString())
                .setHeader("Content-Type", "application/json");
    }

    // --------------------------------------------------
    // Get API request context
    // --------------------------------------------------

    public APIRequestContext getApiRequestContext() {
        return apiRequestContext;
    }

    // --------------------------------------------------
    // Cleanup
    // --------------------------------------------------

    public void close() {

        try {
            apiRequestContext.dispose();
        } finally {
            playwright.close();
        }
    }

    @FunctionalInterface
    private interface ApiCall {
        APIResponse execute();
    }
}
