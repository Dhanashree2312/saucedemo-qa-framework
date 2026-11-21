
package com.qa.tests.ai;

import com.qa.framework.ai.FailureContextCollector;
import com.qa.framework.ai.model.FailureContext;
import org.testng.Assert;
import org.testng.annotations.Test;

public class FailureContextCollectorTest {

    private final FailureContextCollector collector =
            new FailureContextCollector();

    @Test
    public void shouldCollectAndSanitizeFailureContext() {

        FailureContext context = collector.collect(
                "LoginTest",
                "WEB",
                "Login failed. password=MySecret123",
                "AuthenticationException: api_key=ABC123XYZ",
                "Authorization: Bearer abc.def.ghi",
                "target/screenshots/login-failure.png",
                null
        );

        // Verify basic failure details.
        Assert.assertNotNull(context.getExecutionId());
        Assert.assertEquals(context.getTestName(), "LoginTest");
        Assert.assertEquals(context.getPlatform(), "WEB");

        // Verify sensitive information is redacted.
        Assert.assertFalse(
                context.getFailureMessage().contains("MySecret123")
        );

        Assert.assertFalse(
                context.getStackTrace().contains("ABC123XYZ")
        );

        Assert.assertFalse(
                context.getLogs().contains("abc.def.ghi")
        );

        Assert.assertTrue(
                context.getFailureMessage().contains("[REDACTED]")
        );

        // Verify screenshot path is retained.
        Assert.assertEquals(
                context.getScreenshotPath(),
                "target/screenshots/login-failure.png"
        );

        // Verify timestamp is generated when not supplied.
        Assert.assertNotNull(context.getTimestamp());
        Assert.assertFalse(context.getTimestamp().isBlank());
    }

    @Test
    public void shouldPreserveProvidedTimestamp() {

        String expectedTimestamp = "2026-11-19T10:00:00Z";

        FailureContext context = collector.collect(
                "InventoryTest",
                "WEB",
                "Element not found",
                "TimeoutException",
                "Page load timeout",
                "",
                expectedTimestamp
        );

        Assert.assertEquals(
                context.getTimestamp(),
                expectedTimestamp
        );
    }

    @Test
    public void shouldHandleNullFailureDetails() {

        FailureContext context = collector.collect(
                "SampleTest",
                "API",
                null,
                null,
                null,
                null,
                null
        );

        Assert.assertEquals(context.getFailureMessage(), "");
        Assert.assertEquals(context.getStackTrace(), "");
        Assert.assertEquals(context.getLogs(), "");
        Assert.assertEquals(context.getScreenshotPath(), "");
        Assert.assertNotNull(context.getTimestamp());
    }
}
