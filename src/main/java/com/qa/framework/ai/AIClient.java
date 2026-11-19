
package com.qa.framework.ai;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.RequestOptions;
import com.qa.framework.config.ConfigManager;

import java.util.HashMap;
import java.util.Map;

public class AIClient implements AutoCloseable {

    private final String ollamaBaseUrl;
    private final String generationModel;
    private final String embeddingModel;

    private final Playwright playwright;
    private final APIRequestContext requestContext;
    private final Gson gson;

    public AIClient() {

        ollamaBaseUrl = ConfigManager.getProperty("ai_base_url");
        generationModel = ConfigManager.getProperty("ai_model");

        String configuredEmbeddingModel =
                ConfigManager.getProperty("ai_embedding_model");

        embeddingModel = configuredEmbeddingModel != null
                && !configuredEmbeddingModel.isBlank()
                ? configuredEmbeddingModel
                : "nomic-embed-text:latest";

        if (ollamaBaseUrl == null || ollamaBaseUrl.isBlank()) {
            throw new IllegalStateException(
                    "Missing ai_base_url configuration"
            );
        }

        if (generationModel == null || generationModel.isBlank()) {
            throw new IllegalStateException(
                    "Missing ai_model configuration"
            );
        }

        playwright = Playwright.create();

        APIRequest request = playwright.request();

        requestContext = request.newContext(
                new APIRequest.NewContextOptions()
                        .setBaseURL(ollamaBaseUrl)
                        .setExtraHTTPHeaders(
                                Map.of("Content-Type", "application/json")
                        )
        );

        gson = new Gson();
    }

    // Generate an LLM response using Qwen.
    public String generateResponse(String prompt) {

        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException(
                    "Prompt cannot be empty"
            );
        }

        Map<String, Object> requestData = new HashMap<>();
        requestData.put("model", generationModel);
        requestData.put("prompt", prompt);
        requestData.put("stream", false);

        String responseBody = postJson(
                "/api/generate",
                gson.toJson(requestData)
        );

        try {
            JsonObject response =
                    JsonParser.parseString(responseBody).getAsJsonObject();

            if (!response.has("response")
                    || response.get("response").isJsonNull()) {
                throw new IllegalStateException(
                        "Ollama response does not contain generated text"
                );
            }

            return response.get("response").getAsString();

        } catch (RuntimeException e) {
            throw new IllegalStateException(
                    "Could not parse Ollama generation response: "
                            + responseBody,
                    e
            );
        }
    }

    // Generate an embedding vector using Nomic.
    public float[] generateEmbedding(String input) {

        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException(
                    "Embedding input cannot be empty"
            );
        }

        Map<String, Object> requestData = new HashMap<>();
        requestData.put("model", embeddingModel);
        requestData.put("input", input);

        String responseBody = postJson(
                "/api/embed",
                gson.toJson(requestData)
        );

        try {
            JsonObject response =
                    JsonParser.parseString(responseBody).getAsJsonObject();

            JsonArray embeddings = response.getAsJsonArray("embeddings");

            if (embeddings == null || embeddings.isEmpty()) {
                throw new IllegalStateException(
                        "Ollama returned no embeddings"
                );
            }

            JsonArray vector = embeddings.get(0).getAsJsonArray();

            float[] result = new float[vector.size()];

            for (int i = 0; i < vector.size(); i++) {
                result[i] = vector.get(i).getAsFloat();
            }

            return result;

        } catch (RuntimeException e) {
            throw new IllegalStateException(
                    "Could not parse Ollama embedding response: "
                            + responseBody,
                    e
            );
        }
    }

    private String postJson(String endpoint, String requestBody) {

        APIResponse response = requestContext.post(
                endpoint,
                RequestOptions.create().setData(requestBody)
        );

        try {
            String responseBody = response.text();

            if (!response.ok()) {
                throw new RuntimeException(
                        "Ollama request failed. HTTP status: "
                                + response.status()
                                + ", response: " + responseBody
                );
            }

            return responseBody;

        } finally {
            response.dispose();
        }
    }

    @Override
    public void close() {
        try {
            requestContext.dispose();
        } finally {
            playwright.close();
        }
    }
}
