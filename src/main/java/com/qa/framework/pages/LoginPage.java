package com.qa.framework.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class LoginPage {
    private final Page page;
    private final Locator username;
    private final Locator password;
    private final Locator loginBtn;


    public LoginPage(Page page){
        this.page=page;
        username = this.page.getByLabel("Username");
        password = this.page.getByLabel("Password");
        loginBtn = this.page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Login"));
    }

    public void login(String usernamedata, String passworddata){
        username.fill(usernamedata);
        password.fill(passworddata);
        loginBtn.click();
    }

}
