package com.qa.framework.mobile.utils;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class WaitUtils {

    private static final Duration DEFAULT_TIMEOUT =
            Duration.ofSeconds(15);

    private WaitUtils() {
        // Utility class: no object creation required
    }

    public static WebElement waitForVisible(
            AndroidDriver driver,
            By locator
    ) {
        WebDriverWait wait =
                new WebDriverWait(driver, DEFAULT_TIMEOUT);

        return wait.until(
                ExpectedConditions.visibilityOfElementLocated(locator)
        );
    }

    public static WebElement waitForClickable(
            AndroidDriver driver,
            By locator
    ) {
        WebDriverWait wait =
                new WebDriverWait(driver, DEFAULT_TIMEOUT);

        return wait.until(
                ExpectedConditions.elementToBeClickable(locator)
        );
    }
}