package com.qa.framework.mobile.pages;

import com.qa.framework.mobile.gestures.MobileGestureUtils;
import com.qa.framework.mobile.utils.WaitUtils;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class NativeHomePage {

    private final AndroidDriver driver;

    private final By viewsOption =
            AppiumBy.accessibilityId("Views");

    private final By expandableListsOption =
            AppiumBy.accessibilityId("Expandable Lists");

    public NativeHomePage(AndroidDriver driver) {
        this.driver = driver;
    }

    public void openViews() {

        System.out.println("Waiting for Views option...");

        WebElement views = WaitUtils.waitForVisible(
                driver,
                viewsOption
        );

        System.out.println("Views element found: " + views.getText());
        System.out.println("Element clickable: " + views.isEnabled());

        MobileGestureUtils.tap(driver, views);

        System.out.println("Screen after tapping Views:");
        System.out.println(driver.getPageSource());

        WaitUtils.waitForVisible(
                driver,
                expandableListsOption
        );

        System.out.println("Successfully navigated to Views.");
    }

    public void openExpandableLists() {

        WebElement expandableLists = WaitUtils.waitForVisible(
                driver,
                expandableListsOption
        );

        MobileGestureUtils.tap(driver, expandableLists);

        System.out.println("Screen after opening Expandable Lists:");
        System.out.println(driver.getPageSource());
    }

    public boolean isExpandableListsDisplayed() {

        return WaitUtils.waitForVisible(
                driver,
                expandableListsOption
        ).isDisplayed();
    }
}