package com.qa.tests.web;

import com.microsoft.playwright.*;
import com.qa.framework.config.ConfigManager;
import com.qa.tests.reporting.FrameworkLogger;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import java.util.Locale;
import java.util.logging.Logger;

public class BaseTest {

    private static final Logger logger =
            FrameworkLogger.getLogger(BaseTest.class);

    protected Playwright playwright;
    protected Browser browser;
    protected BrowserContext context;
    protected Page page;

    @Parameters("browser")
    @BeforeMethod(alwaysRun = true)
    public void setup(@Optional("") String xmlBrowser) {

        logger.info("========== BaseTest setup started ==========");

        try {
            playwright = Playwright.create();

            // XML browser takes priority. Otherwise, use ConfigManager.
            String browserType = xmlBrowser.isBlank()
                    ? ConfigManager.getBrowser()
                    : xmlBrowser;

            browserType = browserType.toLowerCase(Locale.ROOT);

            switch (browserType) {
                case "chromium":
                    browser = playwright.chromium().launch(
                            new BrowserType.LaunchOptions()
                                    .setHeadless(ConfigManager.isHeadless())
                    );
                    break;

                case "firefox":
                    browser = playwright.firefox().launch(
                            new BrowserType.LaunchOptions()
                                    .setHeadless(ConfigManager.isHeadless())
                    );
                    break;

                case "webkit":
                    browser = playwright.webkit().launch(
                            new BrowserType.LaunchOptions()
                                    .setHeadless(ConfigManager.isHeadless())
                    );
                    break;

                default:
                    throw new IllegalArgumentException(
                            "Unsupported browser: " + browserType
                    );
            }

            logger.info("Configured browser: " + browserType);

            context = browser.newContext();
            page = context.newPage();

            page.setDefaultTimeout(
                    ConfigManager.getIntProperty("timeout")
            );

            logger.info("BaseTest page initialized: " + (page != null));
            logger.info("Page identity: " + System.identityHashCode(page));
            logger.info("========== BaseTest setup completed ==========");

        } catch (Exception e) {
            logger.severe("BaseTest setup failed: " + e.getMessage());
            tearDown();
            throw e;
        }
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {

        logger.info("========== BaseTest teardown started ==========");

        if (page != null) {
            page.close();
            page = null;
        }

        if (context != null) {
            context.close();
            context = null;
        }

        if (browser != null) {
            browser.close();
            browser = null;
        }

        if (playwright != null) {
            playwright.close();
            playwright = null;
        }

        logger.info("========== BaseTest teardown completed ==========");
    }

    /*
     * Allows listeners and other framework components
     * to access the current browser page.
     */
    public Page getPage() {
        return page;
    }
}