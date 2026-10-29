
package com.qa.tests.reporting;

import com.microsoft.playwright.Page;
import com.qa.tests.ui.BaseTest;
import io.qameta.allure.Allure;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.ByteArrayInputStream;
import java.util.logging.Logger;

public class FailureScreenshotListener implements ITestListener {

    private static final Logger logger =
            FrameworkLogger.getLogger(FailureScreenshotListener.class);

    @Override
    public void onTestFailure(ITestResult result) {

        logger.severe("Test failed: " + result.getName());

        try {
            Object testInstance = result.getInstance();

            if (testInstance instanceof BaseTest baseTest) {
                Page page = baseTest.getPage();

                if (page != null) {
                    byte[] screenshot = page.screenshot(
                            new Page.ScreenshotOptions().setFullPage(true)
                    );

                    Allure.addAttachment(
                            "Failure Screenshot",
                            "image/png",
                            new ByteArrayInputStream(screenshot),
                            ".png"
                    );

                    logger.info("Failure screenshot attached to Allure.");
                }
            }

        } catch (Exception e) {
            logger.warning(
                    "Could not capture failure screenshot: " + e.getMessage()
            );
        }
    }
}
