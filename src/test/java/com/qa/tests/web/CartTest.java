package com.qa.tests.web;

import com.qa.framework.config.ConfigManager;
import com.qa.framework.pages.CartPage;
import com.qa.framework.pages.InventoryPage;
import com.qa.framework.pages.LoginPage;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CartTest extends BaseTest {

    private InventoryPage inventoryPage;
    private CartPage cartPage;

    @BeforeMethod(alwaysRun = true)
    public void setUpCartPage() {

        System.out.println("========== CartTest setup started ==========");
        System.out.println("Page is null: " + (page == null));

        Assert.assertNotNull(
                page,
                "Page is null. BaseTest setup may not have initialized the page."
        );

        System.out.println(
                "CartTest page identity: " +
                        System.identityHashCode(page)
        );

        String username = ConfigManager.getProperty("inventory_username");
        String password = ConfigManager.getProperty("inventory_password");

        // Navigate to SauceDemo.
        page.navigate(ConfigManager.getProperty("base_url"));

        // Login.
        LoginPage loginPage = new LoginPage(page);
        loginPage.login(username, password);

        // Verify successful login.
        Assert.assertTrue(
                page.url().contains("/inventory.html"),
                "User should successfully log in and reach the inventory page."
        );

        // Initialize page objects.
        inventoryPage = new InventoryPage(page);
        inventoryPage.checkInventoryPage();

        cartPage = new CartPage(page);

        System.out.println("InventoryPage and CartPage initialized successfully.");
        System.out.println("========== CartTest setup completed ==========");
    }

    @DataProvider(name = "listOfProducts")
    public Object[][] listOfProducts() {

        return new Object[][]{
                {"Sauce Labs Backpack"},
                {"Sauce Labs Bike Light"},
                {"Test.allTheThings() T-Shirt (Red)"}
        };
    }

    @DataProvider(name = "multipleProducts")
    public Object[][] multipleProducts() {

        return new Object[][]{
                {
                        new String[]{
                                "Sauce Labs Backpack",
                                "Sauce Labs Bike Light",
                                "Test.allTheThings() T-Shirt (Red)"
                        }
                }
        };
    }

    @Test(dataProvider = "listOfProducts", groups = {"smoke", "sanity"})
    public void addProductToCart(String productName) {

        Assert.assertNotNull(inventoryPage, "InventoryPage is not initialized.");
        Assert.assertNotNull(cartPage, "CartPage is not initialized.");

        // Capture expected price before adding the product.
        String expectedPrice = inventoryPage.getProductPrice(productName);

        // Add selected product.
        inventoryPage.addProductToCart(productName);

        // Verify cart badge count.
        cartPage.verifyCartItemCount(1);

        // Open cart.
        cartPage.openCart();

        // Verify product name.
        List<String> actualProductNames = cartPage.getCartProductNames();

        Assert.assertEquals(
                actualProductNames,
                List.of(productName),
                "Cart should contain only the selected product."
        );

        // Verify product price.
        List<String> actualProductPrices = cartPage.getCartProductPrices();

        Assert.assertEquals(
                actualProductPrices,
                List.of(expectedPrice),
                "Cart price should match the inventory price."
        );
    }

    @Test(dataProvider = "multipleProducts", groups = {"sanity"})
    public void addMultipleProductsToCart(String[] products) {

        Assert.assertNotNull(inventoryPage, "InventoryPage is not initialized.");
        Assert.assertNotNull(cartPage, "CartPage is not initialized.");

        // Capture expected prices from inventory.
        List<String> expectedPrices = new ArrayList<>();

        for (String product : products) {
            expectedPrices.add(inventoryPage.getProductPrice(product));
        }

        // Add all selected products.
        inventoryPage.addMultipleProducts(products);

        // Verify cart badge count.
        cartPage.verifyCartItemCount(products.length);

        Assert.assertEquals(
                cartPage.getCartItemCount(),
                products.length,
                "Cart badge should match the number of products added."
        );

        // Open cart.
        cartPage.openCart();

        // Verify product names and order.
        List<String> actualProductNames = cartPage.getCartProductNames();

        Assert.assertEquals(
                actualProductNames,
                Arrays.asList(products),
                "Cart products should match the selected products."
        );

        // Verify product prices and order.
        List<String> actualProductPrices = cartPage.getCartProductPrices();

        Assert.assertEquals(
                actualProductPrices,
                expectedPrices,
                "Cart prices should match the inventory prices."
        );
    }
}