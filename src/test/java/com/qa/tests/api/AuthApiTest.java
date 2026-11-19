
package com.qa.tests.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.APIResponse;
import org.testng.Assert;
import org.testng.annotations.Test;
import com.qa.tests.reporting.ApiReportingListener;
import org.testng.annotations.Listeners;

@Listeners(ApiReportingListener.class)
public class AuthApiTest extends BaseApiTest {

    private JsonObject getValidCredentials() {

        JsonObject credentials = new JsonObject();
        credentials.addProperty("username", "emilys");
        credentials.addProperty("password", "emilyspass");
        credentials.addProperty("expiresInMins", 30);

        return credentials;
    }

    @Test
    public void verifySuccessfulLogin() {

        APIResponse response =
                authApiClient.login(getValidCredentials());

        Assert.assertEquals(response.status(), 200);

        JsonObject result =
                JsonParser.parseString(response.text()).getAsJsonObject();

        Assert.assertEquals(
                result.get("username").getAsString(),
                "emilys"
        );

        Assert.assertTrue(result.has("accessToken"));
        Assert.assertTrue(result.has("refreshToken"));

        Assert.assertFalse(result.get("accessToken").getAsString().isBlank());
    }

    @Test
    public void verifyLoginWithInvalidPassword() {

        JsonObject credentials = getValidCredentials();
        credentials.addProperty("password", "wrong-password");

        APIResponse response = authApiClient.login(credentials);

        Assert.assertEquals(
                response.status(),
                400,
                "Invalid credentials should not authenticate the user"
        );
    }

    @Test
    public void verifyAuthenticatedUserDetails() {

        // Step 1: Login
        APIResponse loginResponse =
                authApiClient.login(getValidCredentials());

        Assert.assertEquals(loginResponse.status(), 200);

        JsonObject loginResult =
                JsonParser.parseString(loginResponse.text()).getAsJsonObject();

        String accessToken =
                loginResult.get("accessToken").getAsString();

        // Step 2: Use the token to get the current user
        APIResponse userResponse =
                authApiClient.getCurrentUser(accessToken);

        Assert.assertEquals(userResponse.status(), 200);

        JsonObject user =
                JsonParser.parseString(userResponse.text()).getAsJsonObject();

        Assert.assertEquals(
                user.get("username").getAsString(),
                "emilys"
        );

        Assert.assertEquals(
                user.get("id").getAsInt(),
                loginResult.get("id").getAsInt()
        );
    }

    @Test
    public void verifyRefreshToken() {

        // Step 1: Login and obtain refresh token
        APIResponse loginResponse =
                authApiClient.login(getValidCredentials());

        Assert.assertEquals(loginResponse.status(), 200);

        JsonObject loginResult =
                JsonParser.parseString(loginResponse.text()).getAsJsonObject();

        String refreshToken =
                loginResult.get("refreshToken").getAsString();

        // Step 2: Prepare refresh request
        JsonObject refreshData = new JsonObject();
        refreshData.addProperty("refreshToken", refreshToken);
        refreshData.addProperty("expiresInMins", 30);

        // Step 3: Refresh authentication session
        APIResponse refreshResponse =
                authApiClient.refreshToken(refreshData);

        Assert.assertEquals(refreshResponse.status(), 200);

        JsonObject refreshResult =
                JsonParser.parseString(refreshResponse.text()).getAsJsonObject();

        Assert.assertTrue(refreshResult.has("accessToken"));
        Assert.assertTrue(refreshResult.has("refreshToken"));

        Assert.assertFalse(
                refreshResult.get("accessToken").getAsString().isBlank()
        );
    }

    @Test
    public void verifyAccessWithoutTokenIsRejected() {

        APIResponse response =
                authApiClient.getCurrentUserWithoutToken();

        Assert.assertNotEquals(
                response.status(),
                200,
                "Unauthenticated request must not return a successful user response"
        );
    }
}
