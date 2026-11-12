package com.qa.tests.mobile;

import org.testng.Assert;
import org.testng.annotations.Test;

public class NativeAppLaunchTest extends BaseNativeTest {

    @Test
    public void verifyNativeAppLaunch() {

        String currentPackage = driver.getCurrentPackage();

        String pageSource = driver.getPageSource();
        System.out.println(pageSource);

        System.out.println("Launched package: " + currentPackage);

        Assert.assertNotNull(
                currentPackage,
                "Native application was not launched"
        );

        Assert.assertTrue(
                currentPackage.equals("io.appium.android.apis"),
                "Unexpected application package: " + currentPackage
        );
    }
}
