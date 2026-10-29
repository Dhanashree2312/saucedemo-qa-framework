package com.qa.tests.ui;

import com.qa.framework.config.ConfigManager;
import com.qa.framework.pages.LoginPage;
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
    public void loginTest(
            String username,
            String password,
            boolean expectedSuccess) {

        // 1. Open SauceDemo
        page.navigate(ConfigManager.getProperty("base_url"));
        // 2. Create LoginPage
        LoginPage loginpage = new LoginPage(page);
        // 3. Call login(username, password)
        loginpage.login(username, password);
        // 4. Verify the expected outcome
        Boolean actualResult = page.url().contains("/inventory.html");
        Assert.assertEquals(actualResult,expectedSuccess,
                "Login outcome did not match the expected result for:"+username);
        if(!expectedSuccess){
            Assert.assertTrue(page.locator("[data-test='error']").isVisible(),
                    "Expected error login message for locked user:"+username);
        }
    }
}