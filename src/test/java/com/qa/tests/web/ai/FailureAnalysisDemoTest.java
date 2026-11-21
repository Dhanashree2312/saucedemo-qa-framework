package com.qa.tests.web.ai;
import com.qa.framework.config.ConfigManager;
import com.qa.tests.web.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import com.qa.tests.reporting.FailureScreenshotListener;
import org.testng.annotations.Listeners;

@Listeners(FailureScreenshotListener.class)
public class FailureAnalysisDemoTest extends BaseTest {
    @Test
    public void demonstrateTestFailure() {

        // Open SauceDemo
        getPage().navigate(ConfigManager.getProperty("base_url"));

        // Deliberately incorrect expected title
        String actualTitle = getPage().title();

        Assert.assertEquals(
                actualTitle,
                "Incorrect Expected Title",
                "Demo failure: Page title does not match expected value"
        );
    }
}