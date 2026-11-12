package com.qa.tests.ui;

import com.qa.framework.config.ConfigManager;
import com.qa.framework.pages.InventoryPage;
import com.qa.framework.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

public class InventoryTest extends BaseTest {

    private InventoryPage inventoryPage;

    @BeforeMethod(alwaysRun = true)
    public void setUpInventoryPage() {

        System.out.println("========== InventoryTest setup started ==========");
        System.out.println("Page is null: " + (page == null));

        Assert.assertNotNull(
                page,
                "Page is null. BaseTest setup may not have initialized the page."
        );

        System.out.println(
                "InventoryTest page identity: " +
                        System.identityHashCode(page)
        );

        String username = ConfigManager.getProperty("inventory_username");
        String password = ConfigManager.getProperty("inventory_password");

        // Navigate to SauceDemo.
        page.navigate(ConfigManager.getProperty("base_url"));

        // Create login page object.
        LoginPage loginPage = new LoginPage(page);

        // Login with the standard inventory user.
        loginPage.login(username, password);

        // Verify successful login.
        Assert.assertTrue(
                page.url().contains("/inventory.html"),
                "User should successfully log in and reach the inventory page."
        );

        // Initialize inventory page object.
        inventoryPage = new InventoryPage(page);

        // Verify inventory page.
        inventoryPage.checkInventoryPage();

        System.out.println("InventoryPage initialized successfully.");
        System.out.println("========== InventoryTest setup completed ==========");
    }

    @DataProvider(name = "sortingOptions")
    public Object[][] sortingOptions() {

        return new Object[][]{
                {"az", "name", false},
                {"za", "name", true},
                {"lohi", "price", false},
                {"hilo", "price", true}
        };
    }

    @Test(groups = {"smoke", "sanity"})
    public void inventoryTest() {

        Assert.assertNotNull(inventoryPage, "InventoryPage is not initialized.");

        List<String> productNames = inventoryPage.getProductNames();
        List<String> productPrices = inventoryPage.getProductPrices();

        // Verify product names and prices are available.
        Assert.assertFalse(
                productNames.isEmpty(),
                "Product names should not be empty."
        );

        Assert.assertFalse(
                productPrices.isEmpty(),
                "Product prices should not be empty."
        );

        Assert.assertEquals(
                productNames.size(),
                productPrices.size(),
                "Every product must have a corresponding price."
        );

        // Verify product names are unique.
        Set<String> uniqueNames = new HashSet<>(productNames);

        Assert.assertEquals(
                uniqueNames.size(),
                productNames.size(),
                "Duplicate product names are found."
        );

        // Verify all product names are populated.
        Assert.assertTrue(
                productNames.stream().allMatch(
                        name -> name != null && !name.isBlank()
                ),
                "Every product should have a name."
        );

        // Verify price format.
        Pattern priceMatcher = Pattern.compile("\\$\\d+\\.\\d{2}");

        Assert.assertTrue(
                productPrices.stream().allMatch(
                        price -> price != null &&
                                priceMatcher.matcher(price.trim()).matches()
                ),
                "Every price should follow the standard format of $0.00."
        );
    }

    @Test(dataProvider = "sortingOptions", groups = {"sanity"})
    public void verifyProductSorting(
            String option,
            String sortType,
            boolean descending
    ) {

        Assert.assertNotNull(inventoryPage, "InventoryPage is not initialized.");

        inventoryPage.sortProductsByNameAndPrice(option);

        if (sortType.equals("name")) {

            List<String> actualProductNames =
                    inventoryPage.getProductNames();

            List<String> expectedProductNames =
                    new ArrayList<>(actualProductNames);

            expectedProductNames.sort(String.CASE_INSENSITIVE_ORDER);

            if (descending) {
                Collections.reverse(expectedProductNames);
            }

            Assert.assertEquals(
                    actualProductNames,
                    expectedProductNames,
                    "Product name sorting failed for option: " + option
            );

        } else if (sortType.equals("price")) {

            List<String> actualPrices =
                    inventoryPage.getProductPrices();

            List<BigDecimal> actual = actualPrices.stream()
                    .map(price -> new BigDecimal(
                            price.replace("$", "").trim()
                    ))
                    .toList();

            List<BigDecimal> expected = new ArrayList<>(actual);

            Collections.sort(expected);

            if (descending) {
                Collections.reverse(expected);
            }

            Assert.assertEquals(
                    actual,
                    expected,
                    "Product price sorting failed for option: " + option
            );

        } else {

            Assert.fail("Unsupported sorting type: " + sortType);
        }
    }
}