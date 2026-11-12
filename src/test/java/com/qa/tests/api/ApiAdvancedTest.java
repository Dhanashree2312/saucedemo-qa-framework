
package com.qa.tests.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.APIResponse;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ApiAdvancedTest extends BaseApiTest {

    @Test
    public void verifyProductResponseContentType() {

        APIResponse response = productApiClient.getProductById(1);

        Assert.assertEquals(response.status(), 200);

        String contentType =
                response.headers().get("content-type");

        Assert.assertNotNull(contentType);
        Assert.assertTrue(
                contentType.contains("application/json"),
                "Response should have JSON content type"
        );
    }

    @Test
    public void verifyProductResponseStructure() {

        APIResponse response = productApiClient.getProductById(1);

        Assert.assertEquals(response.status(), 200);

        JsonObject product =
                JsonParser.parseString(response.text()).getAsJsonObject();

        Assert.assertTrue(product.has("id"));
        Assert.assertTrue(product.has("title"));
        Assert.assertTrue(product.has("price"));

        Assert.assertTrue(product.get("id").isJsonPrimitive());
        Assert.assertTrue(product.get("title").isJsonPrimitive());
        Assert.assertTrue(product.get("price").isJsonPrimitive());

        Assert.assertTrue(product.get("id").getAsJsonPrimitive().isNumber());
        Assert.assertTrue(product.get("title").getAsJsonPrimitive().isString());
        Assert.assertTrue(product.get("price").getAsJsonPrimitive().isNumber());
    }

    @Test
    public void verifyUserResponseStructure() {

        APIResponse response = userApiClient.getUserById(1);

        Assert.assertEquals(response.status(), 200);

        JsonObject user =
                JsonParser.parseString(response.text()).getAsJsonObject();

        Assert.assertTrue(user.has("id"));
        Assert.assertTrue(user.has("firstName"));
        Assert.assertTrue(user.has("email"));

        Assert.assertTrue(user.get("id").getAsJsonPrimitive().isNumber());
        Assert.assertTrue(user.get("firstName").getAsJsonPrimitive().isString());
        Assert.assertTrue(user.get("email").getAsJsonPrimitive().isString());
    }

    @Test
    public void verifyProductPagination() {

        APIResponse response =
                apiClient.get("/products?limit=5&skip=5");

        Assert.assertEquals(response.status(), 200);

        JsonObject result =
                JsonParser.parseString(response.text()).getAsJsonObject();

        JsonArray products = result.getAsJsonArray("products");

        Assert.assertEquals(products.size(), 5);
        Assert.assertEquals(result.get("limit").getAsInt(), 5);
        Assert.assertEquals(result.get("skip").getAsInt(), 5);
        Assert.assertTrue(result.get("total").getAsInt() > 5);
    }

    @Test
    public void verifyProductsSortedByTitle() {

        APIResponse response =
                apiClient.get("/products?sortBy=title&order=asc&limit=10");

        Assert.assertEquals(response.status(), 200);

        JsonObject result =
                JsonParser.parseString(response.text()).getAsJsonObject();

        JsonArray products = result.getAsJsonArray("products");

        Assert.assertTrue(products.size() > 1);

        for (int i = 0; i < products.size() - 1; i++) {

            String currentTitle =
                    products.get(i).getAsJsonObject()
                            .get("title").getAsString();

            String nextTitle =
                    products.get(i + 1).getAsJsonObject()
                            .get("title").getAsString();

            Assert.assertTrue(
                    currentTitle.compareToIgnoreCase(nextTitle) <= 0,
                    "Products should be sorted alphabetically"
            );
        }
    }

    @Test
    public void verifyProductsFilteredByCategory() {

        APIResponse response =
                apiClient.get("/products/category/smartphones");

        Assert.assertEquals(response.status(), 200);

        JsonObject result =
                JsonParser.parseString(response.text()).getAsJsonObject();

        JsonArray products = result.getAsJsonArray("products");

        Assert.assertTrue(products.size() > 0);

        for (int i = 0; i < products.size(); i++) {

            String category =
                    products.get(i).getAsJsonObject()
                            .get("category").getAsString();

            Assert.assertEquals(category, "smartphones");
        }
    }

    @Test
    public void verifyProductSearchAndGetProductDetails() {

        APIResponse searchResponse =
                apiClient.get("/products/search?q=phone");

        Assert.assertEquals(searchResponse.status(), 200);

        JsonObject searchResult =
                JsonParser.parseString(searchResponse.text()).getAsJsonObject();

        JsonArray products = searchResult.getAsJsonArray("products");

        Assert.assertTrue(products.size() > 0);

        int productId =
                products.get(0).getAsJsonObject()
                        .get("id").getAsInt();

        // Use the ID from the first API response
        APIResponse detailResponse =
                productApiClient.getProductById(productId);

        Assert.assertEquals(detailResponse.status(), 200);

        JsonObject product =
                JsonParser.parseString(detailResponse.text()).getAsJsonObject();

        Assert.assertEquals(product.get("id").getAsInt(), productId);
    }

    @Test
    public void verifyProductApiResponseTime() {

        long startTime = System.nanoTime();

        APIResponse response = productApiClient.getProductById(1);

        long endTime = System.nanoTime();

        long responseTimeMs =
                (endTime - startTime) / 1_000_000;

        Assert.assertEquals(response.status(), 200);

        System.out.println(
                "Product API response time: " + responseTimeMs + " ms"
        );

        Assert.assertTrue(
                responseTimeMs < 10000,
                "API response should complete within 10 seconds"
        );
    }
}
