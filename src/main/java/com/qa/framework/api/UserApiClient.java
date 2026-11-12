package com.qa.framework.api;

import com.google.gson.JsonElement;
import com.microsoft.playwright.APIResponse;

public class UserApiClient {

    private final APIClient apiClient;

    public UserApiClient(APIClient apiClient){
        this.apiClient=apiClient;
    }

    public APIResponse getAllUsers(){
        return apiClient.get("/users");
    }

    public APIResponse getUserById(int userId){
        return apiClient.get("/users/" + userId);
    }

    public APIResponse addUser(JsonElement userData) {
        return apiClient.post("/users/add", userData);
    }

    public APIResponse updateUser(int userId, JsonElement userData) {
        return apiClient.put("/users/" + userId, userData);
    }

    public APIResponse patchUser(int userId, JsonElement userData) {
        return apiClient.patch("/users/" + userId, userData);
    }

    public APIResponse deleteUser(int userId) {
        return apiClient.delete("/users/" + userId);
    }
}
