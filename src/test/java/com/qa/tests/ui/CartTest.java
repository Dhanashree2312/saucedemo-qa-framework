
package com.qa.tests.ui;

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

    @BeforeMethod
    public void setUpInventoryPage() {

        String username = ConfigManager.getProperty("inventory_username");
        String password = ConfigManager.getProperty("inventory_password");

        page.navigate(ConfigManager.getProperty("base_url"));

        LoginPage loginPage = new LoginPage(page);
        loginPage.login(username, password);

        Assert.assertTrue(
                page.url().contains("/inventory.html"),
                "User should successfully log in and reach the inventory page."
        );

        inventoryPage = new InventoryPage(page);
        inventoryPage.checkInventoryPage();

        cartPage = new CartPage(page);
    }

    @DataProvider(name = "listOfProducts")
    public Object[][] listOfProducts() {
        return new Object[][] {
                {"Sauce Labs Backpack"},
                {"Sauce Labs Bike Light"},
                {"Test.allTheThings() T-Shirt (Red)"}
        };
    }

    @DataProvider(name = "multipleProducts")
    public Object[][] multipleProducts() {
        return new Object[][] {
                {
                        new String[] {
                                "Sauce Labs Backpack",
                                "Sauce Labs Bike Light",
                                "Test.allTheThings() T-Shirt (Red)"
                        }
                }
        };
    }


    @Test(dataProvider = "listOfProducts")
    public void addProductToCart(String productName) {

        // Capture the expected price before adding the product.
        String expectedPrice = inventoryPage.getProductPrice(productName);

        // Add the selected product.
        inventoryPage.addProductToCart(productName);
        cartPage.verifyCartItemCount(1);
        // Open the cart.
        cartPage.openCart();

        // Verify the product name.
        List<String> actualProductNames = cartPage.getCartProductNames();

        Assert.assertEquals(
                actualProductNames,
                List.of(productName),
                "Cart should contain only the selected product."
        );

        // Verify the product price.
        List<String> actualProductPrices = cartPage.getCartProductPrices();

        Assert.assertEquals(
                actualProductPrices,
                List.of(expectedPrice),
                "Cart price should match the inventory price."
        );
    }

    @Test(dataProvider = "multipleProducts")
    public void addMultipleProductsToCart(String[] products) {

        // Capture the expected prices from the inventory.
        List<String> expectedPrices = new ArrayList<>();

        for (String product : products) {
            expectedPrices.add(inventoryPage.getProductPrice(product));
        }

        // Add all selected products.
        inventoryPage.addMultipleProducts(products);

        cartPage.verifyCartItemCount(products.length);

        Assert.assertEquals(
                cartPage.getCartItemCount(),
                products.length,
                "Cart badge should match the number of products added."
        );
        // Open the cart.
        cartPage.openCart();

        // Verify all product names and their order.
        List<String> actualProductNames = cartPage.getCartProductNames();

        Assert.assertEquals(
                actualProductNames,
                Arrays.asList(products),
                "Cart products should match the selected products."
        );

        // Verify all product prices and their order.
        List<String> actualProductPrices = cartPage.getCartProductPrices();

        Assert.assertEquals(
                actualProductPrices,
                expectedPrices,
                "Cart prices should match the inventory prices."
        );
    }
}
