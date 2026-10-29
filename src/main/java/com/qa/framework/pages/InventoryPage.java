
package com.qa.framework.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class InventoryPage {

    private final Page page;
    private final Locator pageTitleSwagLab;
    private final Locator productsTag;
    private final Locator productNames;
    private final Locator productPrices;
    private final Locator sortingOptions;

    public InventoryPage(Page page) {
        this.page = page;

        pageTitleSwagLab = page.getByText("Swag Labs");
        productsTag = page.getByText("Products");
        productNames = page.locator(".inventory_item_name");
        productPrices = page.locator(".inventory_item_price");
        sortingOptions = page.locator(".product_sort_container");
    }

    public void checkInventoryPage() {
        assertThat(pageTitleSwagLab).isVisible();
        assertThat(productsTag).isVisible();
        assertThat(productNames.first()).isVisible();
    }

    public List<String> getProductNames() {
        return productNames.allTextContents();
    }

    public List<String> getProductPrices() {
        return productPrices.allTextContents();
    }

    public String getProductPrice(String productName) {
        Locator product = page.locator(".inventory_item")
                .filter(new Locator.FilterOptions()
                        .setHasText(productName));

        assertThat(product).hasCount(1);

        return product.locator(".inventory_item_price").innerText();
    }

    public void addProductToCart(String productName) {
        Locator product = page.locator(".inventory_item")
                .filter(new Locator.FilterOptions()
                        .setHasText(productName));

        assertThat(product).hasCount(1);

        product.getByRole(
                AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Add to cart")
        ).click();
    }

    public void addMultipleProducts(String[] products) {
        for (String product : products) {
            addProductToCart(product);
        }
    }

    public void sortProductsByNameAndPrice(String option) {
        sortingOptions.selectOption(option);
    }

    public void verifyNoProductAddedInCart() {

    }
}
