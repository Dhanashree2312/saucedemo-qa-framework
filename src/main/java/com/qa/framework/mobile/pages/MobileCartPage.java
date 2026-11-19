package com.qa.framework.mobile.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class MobileCartPage {

    private final AndroidDriver driver;
    private final WebDriverWait wait;

    // Locators
    private final By pageTitle = By.className("title");
    private final By cartItems = By.className("cart_item");
    private final By productNames = By.className("inventory_item_name");
    private final By productPrices = By.className("inventory_item_price");
    private final By checkoutButton = By.id("checkout");
    private final By continueShoppingButton =
            By.id("continue-shopping");

    // Constructor
    public MobileCartPage(AndroidDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    // Verify cart page
    public boolean isCartPageDisplayed() {
        return wait.until(
                ExpectedConditions.visibilityOfElementLocated(pageTitle)
        ).getText().equals("Your Cart");
    }

    // Get cart item count
    public int getCartItemCount() {
        return driver.findElements(cartItems).size();
    }

    // Get product names in cart
    public List<String> getCartProductNames() {
        return driver.findElements(productNames)
                .stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

    // Get product prices in cart
    public List<String> getCartProductPrices() {
        return driver.findElements(productPrices)
                .stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

    // Remove a product from cart
    public void removeProduct(String productName) {

        String productId = productName.toLowerCase()
                .replace(" ", "-");

        By removeButton = By.id("remove-" + productId);

        wait.until(
                ExpectedConditions.elementToBeClickable(removeButton)
        ).click();
    }

    // Continue shopping
    public void continueShopping() {
        wait.until(
                ExpectedConditions.elementToBeClickable(
                        continueShoppingButton
                )
        ).click();
    }

    // Proceed to checkout
    public void proceedToCheckout() {
        wait.until(
                ExpectedConditions.elementToBeClickable(checkoutButton)
        ).click();
    }

    public void waitForCartItems() {
        wait.until(
                ExpectedConditions.visibilityOfElementLocated(pageTitle)
        );

        wait.until(
                ExpectedConditions.visibilityOfElementLocated(cartItems)
        );
    }
}
