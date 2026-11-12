package com.qa.framework.api;

import com.google.gson.JsonElement;
import com.microsoft.playwright.APIResponse;

public class ProductApiClient {

    private final APIClient apiClient;

    public ProductApiClient(APIClient apiClient) {
        this.apiClient = apiClient;
    }

    public APIResponse getAllProducts() {
        return apiClient.get("/products");
    }

    public APIResponse getProductById(int productId) {
        return apiClient.get("/products/" + productId);
    }

    public APIResponse addProduct(JsonElement productData) {
        return apiClient.post("/products/add", productData);
    }

    public APIResponse updateProduct(int productId, JsonElement productData) {
        return apiClient.put("/products/" + productId, productData);
    }

    public APIResponse patchProduct(int productId, JsonElement productData) {
        return apiClient.patch("/products/" + productId, productData);
    }

    public APIResponse deleteProduct(int productId) {
        return apiClient.delete("/products/" + productId);
    }
}