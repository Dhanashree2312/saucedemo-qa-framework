
package com.qa.tests.api;

import com.microsoft.playwright.APIResponse;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ProductApiNegativeTest extends BaseApiTest {

    @Test
    public void verifyNonExistingProductReturns404() {

        // Send request with an invalid product ID
        APIResponse response =
                productApiClient.getProductById(999999);

        // Validate HTTP status
        Assert.assertEquals(
                response.status(),
                404,
                "Non-existing product should return 404"
        );

        // Validate error response
        String responseBody = response.text();

        Assert.assertFalse(
                responseBody.isBlank(),
                "Error response should not be empty"
        );

        Assert.assertTrue(
                responseBody.toLowerCase().contains("not found"),
                "Response should explain that the product was not found"
        );
    }
}
