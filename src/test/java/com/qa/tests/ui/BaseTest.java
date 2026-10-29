package com.qa.tests.ui;

import com.microsoft.playwright.*;
import com.qa.framework.config.ConfigManager;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class BaseTest {
    protected Playwright playwright;
    protected Browser browser;
    protected BrowserContext context;
    protected Page page;

    @BeforeMethod
    public void setup(){
        playwright= Playwright.create();
        String browserType = ConfigManager.getProperty("browser");
        switch(browserType.toLowerCase().trim()) {
            case "chromium":
                browser = playwright.chromium().launch(new BrowserType.LaunchOptions().
                        setHeadless(ConfigManager.getBooleanProperty("headless")));
                break;
            case "firefox":
                browser =playwright.firefox().
                        launch(new BrowserType.LaunchOptions().
                                setHeadless(ConfigManager.getBooleanProperty("headless")));
                break;
            case "webkit":
                browser=playwright.webkit().launch(new BrowserType.LaunchOptions()
                        .setHeadless(ConfigManager.getBooleanProperty("headless")));
                break;
            default:
                throw new IllegalArgumentException("Unsupported browser"+browserType);
        }
        context=browser.newContext();
        page=context.newPage();
        page.setDefaultTimeout(ConfigManager.getIntProperty("timeout"));
    }

    @AfterMethod
    public void tearDown(){
        if(context != null){
            context.close();
        }
        if(browser !=null){
            browser.close();
        }
        if(playwright !=null){
            playwright.close();
        }
    }
}
