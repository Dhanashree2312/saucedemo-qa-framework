
package com.qa.tests.mobile;

import com.qa.framework.mobile.driver.NativeAndroidDriverFactory;
import io.appium.java_client.android.AndroidDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class BaseNativeTest {

    protected AndroidDriver driver;

    @BeforeMethod
    public void setUp() throws Exception {

        driver = NativeAndroidDriverFactory.createDriver();

        System.out.println("Native Android app launched.");
    }

    public AndroidDriver getDriver() {
        return driver;
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {

        if (driver != null) {
            try {
                driver.quit();
                System.out.println("Native app session closed.");
            } catch (Exception e) {
                System.err.println(
                        "Error closing native app session: "
                                + e.getMessage()
                );
            } finally {
                driver = null;
            }
        }
    }
}
