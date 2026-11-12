
package com.qa.tests.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.APIResponse;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ApiHealthTest extends BaseApiTest {

    @Test
    public void verifyProductEndpoint() {

        APIResponse response =
                apiClient.getApiRequestContext().get("/products/1");

        Assert.assertEquals(response.status(), 200);

        JsonObject responseJson =
                JsonParser.parseString(response.text())
                        .getAsJsonObject();

        Assert.assertEquals(
                responseJson.get("id").getAsInt(),
                1,
                "Product ID should be 1"
        );

        Assert.assertTrue(
                responseJson.has("title"),
                "Product response should contain title"
        );

        Assert.assertFalse(
                responseJson.get("title").getAsString().isBlank(),
                "Product title should not be blank"
        );
    }
}
