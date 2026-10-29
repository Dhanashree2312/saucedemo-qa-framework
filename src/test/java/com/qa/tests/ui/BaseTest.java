package com.qa.tests.ui;

import com.microsoft.playwright.*;
import com.qa.framework.config.ConfigManager;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import com.qa.tests.reporting.FrameworkLogger;

import java.util.logging.Logger;

public class BaseTest {
    private static final Logger logger =
            FrameworkLogger.getLogger(BaseTest.class);
    protected Playwright playwright;
    protected Browser browser;
    protected BrowserContext context;
    protected Page page;

    @BeforeMethod
    public void setup(){
        logger.info("Starting test setup");
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
        logger.info("Configured browser: " + browserType);
        context=browser.newContext();
        page=context.newPage();
        page.setDefaultTimeout(ConfigManager.getIntProperty("timeout"));
        logger.info("Browser setup completed successfully");
    }

    @AfterMethod
    public void tearDown(){
        logger.info("Starting test teardown");
        if(context != null){
            context.close();
        }
        if(browser !=null){
            browser.close();
        }
        if(playwright !=null){
            playwright.close();
        }
        logger.info("Test teardown completed");
    }
    /*
    page represents the active browser tab.
    getPage() allows another class, our failure listener, to access that tab.
    We keep page as protected, preserving the existing access design.
     */
    public Page getPage() {
        return page;
    }

}
