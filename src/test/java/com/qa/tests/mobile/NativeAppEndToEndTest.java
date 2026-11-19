package com.qa.tests.mobile;

import com.qa.framework.mobile.pages.ExpandableListsPage;
import com.qa.framework.mobile.pages.NativeHomePage;
import org.testng.Assert;
import org.testng.annotations.Test;
import com.qa.tests.reporting.MobileReportingListener;
import org.testng.annotations.Listeners;

@Listeners(MobileReportingListener.class)
public class NativeAppEndToEndTest extends BaseNativeTest {

    @Test
    public void verifyNativeAppNavigationEndToEnd() {

        // Step 1: Verify that the native application launched
        Assert.assertEquals(
                driver.getCurrentPackage(),
                "io.appium.android.apis",
                "API Demos application did not launch."
        );

        // Step 2: Create page objects
        NativeHomePage homePage = new NativeHomePage(driver);

        ExpandableListsPage expandableListsPage =
                new ExpandableListsPage(driver);

        // Step 3: Navigate from Home to Views
        homePage.openViews();

        Assert.assertTrue(
                homePage.isExpandableListsDisplayed(),
                "Expandable Lists was not displayed on the Views screen."
        );

        // Step 4: Navigate to Expandable Lists
        homePage.openExpandableLists();

        // Step 5: Validate the available options
        Assert.assertTrue(
                expandableListsPage.isCustomAdapterOptionDisplayed(),
                "Custom Adapter option is missing."
        );

        Assert.assertTrue(
                expandableListsPage.isCursorPeopleOptionDisplayed(),
                "Cursor (People) option is missing."
        );

        Assert.assertTrue(
                expandableListsPage.isSimpleAdapterOptionDisplayed(),
                "Simple Adapter option is missing."
        );

        System.out.println(
                "Native Android end-to-end navigation completed successfully."
        );

    }
}