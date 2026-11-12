package com.qa.framework.mobile.gestures;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebElement;

import java.util.Map;

public class MobileGestureUtils {

    private MobileGestureUtils() {
        // Utility class: no object creation required
    }

    public static void tap(AndroidDriver driver, WebElement element) {

        Rectangle bounds = element.getRect();

        int centerX = bounds.getX() + bounds.getWidth() / 2;
        int centerY = bounds.getY() + bounds.getHeight() / 2;

        System.out.println(
                "Tapping element at coordinates: " + centerX + ", " + centerY
        );

        driver.executeScript(
                "mobile: clickGesture",
                Map.of(
                        "x", centerX,
                        "y", centerY
                )
        );
    }
}