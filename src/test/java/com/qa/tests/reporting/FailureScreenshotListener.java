
package com.qa.tests.reporting;

import com.microsoft.playwright.Page;
import com.qa.tests.reporting.FrameworkLogger;
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

            logger.info(
                    "Test instance class: " +
                            testInstance.getClass().getName()
            );

            if (testInstance instanceof BaseTest baseTest) {

                logger.info("Test instance extends BaseTest.");

                Page page = baseTest.getPage();

                if (page != null) {

                    logger.info("Page is available. Attempting screenshot capture.");

                    byte[] screenshot = page.screenshot(
                            new Page.ScreenshotOptions()
                                    .setFullPage(true)
                    );

                    logger.info(
                            "Screenshot captured successfully. Bytes: " +
                                    screenshot.length
                    );

                    Allure.addAttachment(
                            "Failure Screenshot",
                            "image/png",
                            new ByteArrayInputStream(screenshot),
                            ".png"
                    );

                    logger.info("Failure screenshot attached to Allure.");

                } else {
                    logger.warning(
                            "Page is null; screenshot cannot be captured."
                    );
                }

            } else {
                logger.warning(
                        "Test instance is not an instance of BaseTest."
                );
            }

        } catch (Exception e) {
            logger.warning(
                    "Could not capture failure screenshot: " +
                            e.getMessage()
            );
            logger.warning("Screenshot capture error details:");

            for (StackTraceElement element : e.getStackTrace()) {
                logger.warning(element.toString());
            }
        }
    }
}
