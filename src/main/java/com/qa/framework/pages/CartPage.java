
package com.qa.framework.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class CartPage {

    private final Locator shoppingCartIcon;
    private final Locator yourCartTitle;
    private final Locator cartList;
    private final Locator cartProductNames;
    private final Locator cartProductPrices;
    private final Locator cartBadge;


    public CartPage(Page page) {
        // SauceDemo uses data-test, not Playwright's default data-testid.
        // The CSS class below avoids that attribute mismatch.
        shoppingCartIcon = page.locator(".shopping_cart_link");

        yourCartTitle = page.getByText("Your Cart",
                new Page.GetByTextOptions().setExact(true));

        cartList = page.locator(".cart_list");
        cartProductNames = page.locator(".cart_item .inventory_item_name");
        cartProductPrices = page.locator(".cart_item .inventory_item_price");
        cartBadge = page.locator(".shopping_cart_badge");

    }

    public void openCart() {
        assertThat(shoppingCartIcon).isVisible();
        shoppingCartIcon.click();

        assertThat(yourCartTitle).isVisible();
        assertThat(cartList).isVisible();
    }

    public int getCartItemCount(){
        if (!cartBadge.isVisible()) {
            return 0;
        }
        assertThat(cartBadge).isVisible();
        return Integer.parseInt(cartBadge.innerText());
    }

    public void verifyCartItemCount(int expectedCount){
        assertThat(cartBadge).hasText(String.valueOf(expectedCount));
    }

    public List<String> getCartProductNames() {
        return cartProductNames.allTextContents();
    }

    public List<String> getCartProductPrices() {
        return cartProductPrices.allTextContents();
    }
}
