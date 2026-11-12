package com.qa.tests.mobile;

import com.qa.framework.mobile.pages.ExpandableListsPage;
import com.qa.framework.mobile.pages.NativeHomePage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class NativeNavigationTest extends BaseNativeTest {

    @Test
    public void verifyNavigationToExpandableLists() {

        NativeHomePage homePage =
                new NativeHomePage(driver);

        ExpandableListsPage expandableListsPage =
                new ExpandableListsPage(driver);

        // Step 1: Open Views from the home screen
        homePage.openViews();

        // Step 2: Verify that Expandable Lists is available
        Assert.assertTrue(
                homePage.isExpandableListsDisplayed(),
                "Expandable Lists option was not displayed."
        );

        // Step 3: Open Expandable Lists
        homePage.openExpandableLists();

        // Step 4: Verify the options on the Expandable Lists screen
        Assert.assertTrue(
                expandableListsPage.isCustomAdapterOptionDisplayed(),
                "Custom Adapter option was not displayed."
        );

        Assert.assertTrue(
                expandableListsPage.isCursorPeopleOptionDisplayed(),
                "Cursor People option was not displayed."
        );

        Assert.assertTrue(
                expandableListsPage.isSimpleAdapterOptionDisplayed(),
                "Simple Adapter option was not displayed."
        );

        System.out.println(
                "Native navigation test completed successfully."
        );
    }
}