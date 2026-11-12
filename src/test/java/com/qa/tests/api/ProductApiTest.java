package com.qa.tests.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.APIResponse;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ProductApiTest extends BaseApiTest {

    @Test
    public void verifyGetAllProducts() {

        APIResponse response = productApiClient.getAllProducts();

        Assert.assertEquals(response.status(), 200);

        JsonObject responseJson =
                JsonParser.parseString(response.text()).getAsJsonObject();

        Assert.assertTrue(responseJson.has("products"));
        Assert.assertTrue(
                responseJson.getAsJsonArray("products").size() > 0
        );
    }

    @Test
    public void verifyGetProductById() {

        APIResponse response = productApiClient.getProductById(1);

        Assert.assertEquals(response.status(), 200);

        JsonObject responseJson =
                JsonParser.parseString(response.text()).getAsJsonObject();

        Assert.assertEquals(responseJson.get("id").getAsInt(), 1);
        Assert.assertTrue(responseJson.has("title"));
    }

    @Test
    public void verifyAddProduct() {

        JsonObject product = new JsonObject();
        product.addProperty("title", "QA Automation Book");
        product.addProperty("price", 100);

        APIResponse response = productApiClient.addProduct(product);

        Assert.assertEquals(response.status(), 201);

        JsonObject responseJson =
                JsonParser.parseString(response.text()).getAsJsonObject();

        Assert.assertEquals(
                responseJson.get("title").getAsString(),
                "QA Automation Book"
        );
    }

    @Test
    public void verifyUpdateProduct() {

        JsonObject product = new JsonObject();
        product.addProperty("title", "Updated QA Automation Book");
        product.addProperty("price", 150);

        APIResponse response =
                productApiClient.updateProduct(1, product);

        Assert.assertEquals(response.status(), 200);

        JsonObject responseJson =
                JsonParser.parseString(response.text()).getAsJsonObject();

        Assert.assertEquals(
                responseJson.get("title").getAsString(),
                "Updated QA Automation Book"
        );
    }

    @Test
    public void verifyPatchProduct() {

        JsonObject product = new JsonObject();
        product.addProperty("title", "Partially Updated Product");

        APIResponse response =
                productApiClient.patchProduct(1, product);

        Assert.assertEquals(response.status(), 200);

        JsonObject responseJson =
                JsonParser.parseString(response.text()).getAsJsonObject();

        Assert.assertEquals(
                responseJson.get("title").getAsString(),
                "Partially Updated Product"
        );
    }

    @Test
    public void verifyDeleteProduct() {

        APIResponse response = productApiClient.deleteProduct(1);

        Assert.assertEquals(response.status(), 200);

        JsonObject responseJson =
                JsonParser.parseString(response.text()).getAsJsonObject();

        Assert.assertTrue(
                responseJson.get("isDeleted").getAsBoolean()
        );
    }
}