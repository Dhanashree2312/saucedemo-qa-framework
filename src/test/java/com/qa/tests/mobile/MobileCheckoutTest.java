
package com.qa.tests.mobile;

import com.qa.framework.mobile.pages.MobileCartPage;
import com.qa.framework.mobile.pages.MobileCheckoutPage;
import com.qa.framework.mobile.pages.MobileInventoryPage;
import com.qa.framework.mobile.pages.MobileLoginPage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MobileCheckoutTest extends BaseMobileTest {

    private MobileCheckoutPage checkoutPage;

    @BeforeMethod
    public void setupCheckout() {
        driver.get("https://www.saucedemo.com/");

        MobileLoginPage loginPage = new MobileLoginPage(driver);
        loginPage.login("standard_user", "secret_sauce");

        MobileInventoryPage inventoryPage =
                new MobileInventoryPage(driver);

        inventoryPage.addProductToCart("Sauce Labs Backpack");
        inventoryPage.openCart();

        MobileCartPage cartPage = new MobileCartPage(driver);
        cartPage.proceedToCheckout();

        checkoutPage = new MobileCheckoutPage(driver);
    }

    @Test
    public void verifyCheckoutInformation() {
        checkoutPage.enterCustomerDetails(
                "Dhanashree",
                "Saraf",
                "440001"
        );

        checkoutPage.continueToOverview();

        Assert.assertTrue(
                driver.getCurrentUrl().contains("checkout-step-two.html")
        );
    }

    @Test
    public void verifyCheckoutPriceDetails() {
        checkoutPage.enterCustomerDetails(
                "Dhanashree",
                "Saraf",
                "440001"
        );

        checkoutPage.continueToOverview();

        Assert.assertTrue(
                checkoutPage.getSubtotal().contains("29.99")
        );

        Assert.assertTrue(
                checkoutPage.getTax().contains("2.40")
        );

        Assert.assertTrue(
                checkoutPage.getTotal().contains("32.39")
        );
    }

    @Test
    public void verifySuccessfulOrder() {
        checkoutPage.enterCustomerDetails(
                "Dhanashree",
                "Saraf",
                "440001"
        );

        checkoutPage.continueToOverview();
        checkoutPage.finishCheckout();

        Assert.assertTrue(
                checkoutPage.isOrderConfirmed(),
                "Order confirmation was not displayed"
        );
    }
}
