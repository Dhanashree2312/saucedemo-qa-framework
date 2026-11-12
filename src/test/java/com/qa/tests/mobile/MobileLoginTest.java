
package com.qa.tests.mobile;

import com.qa.framework.mobile.pages.MobileLoginPage;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MobileLoginTest extends BaseMobileTest {

    @Test
    public void verifySuccessfulMobileLogin() {

        driver.get("https://www.saucedemo.com/");

        MobileLoginPage loginPage = new MobileLoginPage(driver);

        loginPage.login("standard_user", "secret_sauce");

        String currentUrl = driver.getCurrentUrl();

        Assert.assertTrue(
                currentUrl.contains("inventory.html"),
                "Login failed on Android Chrome"
        );

        System.out.println("Mobile login successful!");
    }

    @Test
    public void verifyChromeStability() {

        driver.get("https://www.saucedemo.com/");

        System.out.println("Page title: " + driver.getTitle());

        System.out.println("Username visible: " +
                driver.findElement(By.id("user-name")).isDisplayed());

        System.out.println("Password visible: " +
                driver.findElement(By.id("password")).isDisplayed());

        System.out.println("Login button visible: " +
                driver.findElement(By.id("login-button")).isDisplayed());
    }
}
