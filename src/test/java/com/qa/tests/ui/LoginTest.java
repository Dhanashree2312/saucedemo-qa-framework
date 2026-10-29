package com.qa.tests.ui;

import com.qa.framework.config.ConfigManager;
import com.qa.framework.pages.LoginPage;
import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class LoginTest extends BaseTest {

    @DataProvider(name = "loginUsers")
    public Object[][] loginUsers() {
        return new Object[][] {
                {"standard_user", "secret_sauce", true},
                {"locked_out_user", "secret_sauce", false},
                {"problem_user", "secret_sauce", true},
                {"performance_glitch_user", "secret_sauce", true},
                {"error_user", "secret_sauce", true},
                {"visual_user", "secret_sauce", true}
        };
    }

    @Test(dataProvider = "loginUsers")
    @Description("Verify login behavior for different SauceDemo users")
    public void loginTest(
            String username,
            String password,
            boolean expectedSuccess) {

        Allure.step("Open SauceDemo Page",()->{
            page.navigate(ConfigManager.getProperty("base_url"));
        });

        LoginPage loginpage = new LoginPage(page);

        Allure.step("Enter credentials and click on login",()->{loginpage.login(username, password);});

        Allure.step("Verify login outcome", () -> {
            boolean actualResult =
                    page.url().contains("/inventory.html");

            Assert.assertEquals(
                    actualResult,
                    expectedSuccess,
                    "Login outcome did not match expected result for: "
                            + username
            );
        });

        if(!expectedSuccess){
            Allure.step("Verify locked error message when logged in with locked user:",
                    ()->
                    {
                        Assert.assertTrue(page.locator("[data-test='error']").isVisible(),
                                "Expected error login message for locked user:"+username);
                    });

        }
    }
}