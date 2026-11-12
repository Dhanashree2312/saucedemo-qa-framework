package com.qa.tests.mobile;

import com.qa.framework.mobile.driver.MobileDriverFactory;
import io.appium.java_client.android.AndroidDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class BaseMobileTest {

    protected AndroidDriver driver;

    @BeforeMethod
    public void setUp() throws Exception {
        driver = MobileDriverFactory.createDriver();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            try {
                driver.quit();
                System.out.println("Appium driver session closed successfully.");
            } catch (Exception e) {
                System.err.println("Error while closing Appium session: " + e.getMessage());
            } finally {
                driver = null;
            }
        }
    }
}
