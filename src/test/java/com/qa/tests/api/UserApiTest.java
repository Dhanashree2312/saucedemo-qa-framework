package com.qa.tests.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.APIResponse;
import org.testng.Assert;
import org.testng.annotations.Test;

public class UserApiTest extends BaseApiTest {

    @Test
    public void verifyGetAllUsers() {

        APIResponse response = userApiClient.getAllUsers();

        Assert.assertEquals(response.status(), 200);

        JsonObject responseJson =
                JsonParser.parseString(response.text()).getAsJsonObject();

        Assert.assertTrue(responseJson.has("users"));
        Assert.assertTrue(
                responseJson.getAsJsonArray("users").size() > 0
        );
    }

    @Test
    public void verifyGetUserById() {

        APIResponse response = userApiClient.getUserById(1);

        Assert.assertEquals(response.status(), 200);

        JsonObject responseJson =
                JsonParser.parseString(response.text()).getAsJsonObject();

        Assert.assertEquals(responseJson.get("id").getAsInt(), 1);
        Assert.assertTrue(responseJson.has("firstName"));
    }

    @Test
    public void verifyAddUser() {

        JsonObject user = new JsonObject();
        user.addProperty("firstName", "Test");
        user.addProperty("lastName", "Automation");
        user.addProperty("age", 30);

        APIResponse response = userApiClient.addUser(user);

        Assert.assertEquals(response.status(), 201);

        JsonObject responseJson =
                JsonParser.parseString(response.text()).getAsJsonObject();

        Assert.assertEquals(
                responseJson.get("firstName").getAsString(),
                "Test"
        );
    }

    @Test
    public void verifyUpdateUser() {

        JsonObject user = new JsonObject();
        user.addProperty("firstName", "Updated");

        APIResponse response = userApiClient.updateUser(1, user);

        Assert.assertEquals(response.status(), 200);

        JsonObject responseJson =
                JsonParser.parseString(response.text()).getAsJsonObject();

        Assert.assertEquals(
                responseJson.get("firstName").getAsString(),
                "Updated"
        );
    }

    @Test
    public void verifyPatchUser() {

        JsonObject user = new JsonObject();
        user.addProperty("lastName", "Patched");

        APIResponse response = userApiClient.patchUser(1, user);

        Assert.assertEquals(response.status(), 200);

        JsonObject responseJson =
                JsonParser.parseString(response.text()).getAsJsonObject();

        Assert.assertEquals(
                responseJson.get("lastName").getAsString(),
                "Patched"
        );
    }

    @Test
    public void verifyDeleteUser() {

        APIResponse response = userApiClient.deleteUser(1);

        Assert.assertEquals(response.status(), 200);

        JsonObject responseJson =
                JsonParser.parseString(response.text()).getAsJsonObject();

        Assert.assertTrue(
                responseJson.get("isDeleted").getAsBoolean()
        );
    }
}