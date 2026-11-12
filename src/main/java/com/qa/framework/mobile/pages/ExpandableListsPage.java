package com.qa.framework.mobile.pages;

import com.qa.framework.mobile.gestures.MobileGestureUtils;
import com.qa.framework.mobile.utils.WaitUtils;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class ExpandableListsPage {

    private final AndroidDriver driver;

    private final By customAdapterOption =
            AppiumBy.accessibilityId("1. Custom Adapter");

    private final By cursorPeopleOption =
            AppiumBy.accessibilityId("2. Cursor (People)");

    private final By simpleAdapterOption =
            AppiumBy.accessibilityId("3. Simple Adapter");

    public ExpandableListsPage(AndroidDriver driver) {
        this.driver = driver;
    }

    public void openCustomAdapter() {

        WebElement customAdapter = WaitUtils.waitForVisible(
                driver,
                customAdapterOption
        );

        System.out.println(
                "Found option: " + customAdapter.getText()
        );

        MobileGestureUtils.tap(driver, customAdapter);

        System.out.println("Screen after tapping Custom Adapter:");
        System.out.println(driver.getPageSource());
    }

    public boolean isCustomAdapterOptionDisplayed() {

        return WaitUtils.waitForVisible(
                driver,
                customAdapterOption
        ).isDisplayed();
    }

    public boolean isCursorPeopleOptionDisplayed() {

        return WaitUtils.waitForVisible(
                driver,
                cursorPeopleOption
        ).isDisplayed();
    }

    public boolean isSimpleAdapterOptionDisplayed() {

        return WaitUtils.waitForVisible(
                driver,
                simpleAdapterOption
        ).isDisplayed();
    }
}