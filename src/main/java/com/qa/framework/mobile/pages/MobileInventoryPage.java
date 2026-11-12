
package com.qa.framework.mobile.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class MobileInventoryPage {

    private final AndroidDriver driver;
    private final WebDriverWait wait;

    // Locators
    private final By pageTitle = By.className("title");
    private final By productNames = By.className("inventory_item_name");
    private final By productPrices = By.className("inventory_item_price");
    private final By sortDropdown = By.className("product_sort_container");
    private final By cartIcon = By.className("shopping_cart_link");

    // Constructor
    public MobileInventoryPage(AndroidDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    // Verify inventory page
    public boolean isInventoryPageDisplayed() {
        return wait.until(
                ExpectedConditions.visibilityOfElementLocated(pageTitle)
        ).getText().equals("Products");
    }

    // Get all product names
    public List<String> getProductNames() {
        wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        productNames
                )
        );

        return driver.findElements(productNames)
                .stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

    // Get all product prices
    public List<String> getProductPrices() {
        wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        productPrices
                )
        );

        return driver.findElements(productPrices)
                .stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

    // Sort products
    public void sortProducts(String option) {
        WebElement dropdown = wait.until(
                ExpectedConditions.elementToBeClickable(sortDropdown)
        );

        new Select(dropdown).selectByVisibleText(option);
    }

    // Add a product to cart using its name
    public void addProductToCart(String productName) {

        String productId = convertProductNameToId(productName);

        By addButton = By.id("add-to-cart-" + productId);

        wait.until(
                ExpectedConditions.elementToBeClickable(addButton)
        ).click();
    }

    // Remove a product from inventory page
    public void removeProductFromCart(String productName) {

        String productId = convertProductNameToId(productName);

        By removeButton = By.id("remove-" + productId);

        wait.until(
                ExpectedConditions.elementToBeClickable(removeButton)
        ).click();
    }

    // Open cart
    public void openCart() {
        wait.until(
                ExpectedConditions.elementToBeClickable(cartIcon)
        ).click();
    }

    // Convert product name into SauceDemo product ID
    private String convertProductNameToId(String productName) {
        return productName.toLowerCase()
                .replace(" ", "-");
    }
}
