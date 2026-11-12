package com.qa.tests.api;

import com.microsoft.playwright.APIResponse;
import org.testng.Assert;
import org.testng.annotations.Test;

public class UserApiNegativeTest extends BaseApiTest {

    @Test
    public void verifyNonExistingUserReturns404() {

        // Send request with an invalid user ID
        APIResponse response =
                userApiClient.getUserById(999999);

        // Validate HTTP status
        Assert.assertEquals(
                response.status(),
                404,
                "Non-existing user should return 404"
        );

        // Validate error response
        String responseBody = response.text();

        Assert.assertFalse(
                responseBody.isBlank(),
                "Error response should not be empty"
        );

        Assert.assertTrue(
                responseBody.toLowerCase().contains("not found"),
                "Response should explain that the user was not found"
        );
    }
}
