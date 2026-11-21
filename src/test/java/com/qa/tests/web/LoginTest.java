package com.qa.tests.web;

import com.qa.framework.config.ConfigManager;
import com.qa.framework.pages.LoginPage;
import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LoginTest extends BaseTest {

    @Test(
            dataProvider = "loginData",
            dataProviderClass = LoginDataProvider.class,
            groups = {"smoke"}
    )
    @Description("Verify login behavior using test data from Excel or AWS S3")
    public void loginTest(
            String testCaseId,
            String username,
            String password,
            String expectedResult) {

        boolean expectedSuccess = expectedResult.equalsIgnoreCase("SUCCESS");

        Allure.step("Open SauceDemo Page", () -> {
            page.navigate(ConfigManager.getProperty("base_url"));
        });

        LoginPage loginPage = new LoginPage(page);

        Allure.step("Enter credentials and click on login", () -> {
            loginPage.login(username, password);
        });

        Allure.step("Verify login outcome", () -> {

            boolean actualResult = page.url().contains("/inventory.html");

            Assert.assertEquals(
                    actualResult,
                    expectedSuccess,
                    "Login outcome did not match for test case: " + testCaseId
                            + ", username: " + username
            );
        });

        if (!expectedSuccess) {

            Allure.step("Verify error message for unsuccessful login", () -> {

                Assert.assertTrue(
                        page.locator("[data-test='error']").isVisible(),
                        "Expected login error message for: " + username
                );
            });
        }
    }
}
