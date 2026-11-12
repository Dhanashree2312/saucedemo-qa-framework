
package com.qa.framework.mobile.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class MobileCheckoutPage {

    private final AndroidDriver driver;
    private final WebDriverWait wait;

    // Checkout information locators
    private final By firstNameField = By.id("first-name");
    private final By lastNameField = By.id("last-name");
    private final By postalCodeField = By.id("postal-code");
    private final By continueButton = By.id("continue");

    // Checkout overview locators
    private final By subtotalLabel = By.className("summary_subtotal_label");
    private final By taxLabel = By.className("summary_tax_label");
    private final By totalLabel = By.className("summary_total_label");
    private final By finishButton = By.id("finish");

    // Checkout completion locators
    private final By confirmationHeader =
            By.className("complete-header");

    private final By confirmationMessage =
            By.className("complete-text");

    // Constructor
    public MobileCheckoutPage(AndroidDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    // Enter customer details
    public void enterCustomerDetails(
            String firstName,
            String lastName,
            String postalCode) {

        wait.until(
                ExpectedConditions.visibilityOfElementLocated(firstNameField)
        ).sendKeys(firstName);

        wait.until(
                ExpectedConditions.visibilityOfElementLocated(lastNameField)
        ).sendKeys(lastName);

        wait.until(
                ExpectedConditions.visibilityOfElementLocated(postalCodeField)
        ).sendKeys(postalCode);
    }

    // Continue to checkout overview
    public void continueToOverview() {
        wait.until(
                ExpectedConditions.elementToBeClickable(continueButton)
        ).click();
    }

    // Read subtotal
    public String getSubtotal() {
        return wait.until(
                ExpectedConditions.visibilityOfElementLocated(subtotalLabel)
        ).getText();
    }

    // Read tax
    public String getTax() {
        return wait.until(
                ExpectedConditions.visibilityOfElementLocated(taxLabel)
        ).getText();
    }

    // Read total
    public String getTotal() {
        return wait.until(
                ExpectedConditions.visibilityOfElementLocated(totalLabel)
        ).getText();
    }

    // Finish order
    public void finishCheckout() {
        wait.until(
                ExpectedConditions.elementToBeClickable(finishButton)
        ).click();
    }

    // Verify order confirmation
    public boolean isOrderConfirmed() {
        return wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        confirmationHeader
                )
        ).getText().equals("Thank you for your order!");
    }

    // Get confirmation message
    public String getConfirmationMessage() {
        return wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        confirmationMessage
                )
        ).getText();
    }
}
