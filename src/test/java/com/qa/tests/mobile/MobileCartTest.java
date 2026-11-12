
package com.qa.tests.mobile;

import com.qa.framework.mobile.pages.MobileCartPage;
import com.qa.framework.mobile.pages.MobileInventoryPage;
import com.qa.framework.mobile.pages.MobileLoginPage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MobileCartTest extends BaseMobileTest {

    private MobileCartPage cartPage;

    @BeforeMethod
    public void setupCart() {
        driver.get("https://www.saucedemo.com/");

        MobileLoginPage loginPage = new MobileLoginPage(driver);
        loginPage.login("standard_user", "secret_sauce");

        MobileInventoryPage inventoryPage =
                new MobileInventoryPage(driver);

        inventoryPage.addProductToCart("Sauce Labs Backpack");
        inventoryPage.openCart();

        cartPage = new MobileCartPage(driver);
    }

    @Test
    public void verifyCartPage() {
        Assert.assertTrue(cartPage.isCartPageDisplayed());
    }

    @Test
    public void verifyCartProductDetails() {
        Assert.assertEquals(cartPage.getCartItemCount(), 1);

        Assert.assertTrue(
                cartPage.getCartProductNames()
                        .contains("Sauce Labs Backpack")
        );

        Assert.assertTrue(
                cartPage.getCartProductPrices().contains("$29.99")
        );
    }

    @Test
    public void verifyRemoveProductFromCart() {
        cartPage.removeProduct("Sauce Labs Backpack");

        Assert.assertEquals(
                cartPage.getCartItemCount(),
                0,
                "Cart should be empty after removing product"
        );
    }

    @Test
    public void verifyCheckoutNavigation() {
        cartPage.proceedToCheckout();

        Assert.assertTrue(
                driver.getCurrentUrl().contains("checkout-step-one.html")
        );
    }
}
