
package com.qa.tests.mobile;

import com.qa.framework.mobile.pages.MobileInventoryPage;
import com.qa.framework.mobile.pages.MobileLoginPage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

public class MobileInventoryTest extends BaseMobileTest {

    private MobileInventoryPage inventoryPage;

    @BeforeMethod
    public void loginToApplication() {
        driver.get("https://www.saucedemo.com/");

        MobileLoginPage loginPage = new MobileLoginPage(driver);
        loginPage.login("standard_user", "secret_sauce");

        inventoryPage = new MobileInventoryPage(driver);
    }

    @Test
    public void verifyInventoryPage() {
        Assert.assertTrue(
                inventoryPage.isInventoryPageDisplayed(),
                "Inventory page is not displayed"
        );
    }

    @Test
    public void verifyProductListing() {
        List<String> products = inventoryPage.getProductNames();

        Assert.assertFalse(products.isEmpty(),
                "No products displayed");

        Assert.assertTrue(products.contains("Sauce Labs Backpack"),
                "Expected product is missing");
    }

    @Test
    public void verifyProductSorting() {
        inventoryPage.sortProducts("Name (Z to A)");

        List<String> products = inventoryPage.getProductNames();

        Assert.assertEquals(
                products.get(0),
                "Test.allTheThings() T-Shirt (Red)"
        );
    }

    @Test
    public void verifyAddProductToCart() {
        inventoryPage.addProductToCart("Sauce Labs Backpack");
        inventoryPage.openCart();

        Assert.assertTrue(driver.getCurrentUrl().contains("cart.html"));
    }
}
