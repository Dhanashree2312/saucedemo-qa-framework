package com.qa.tests.ai;

import com.qa.framework.ai.LogSanitizer;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LogSanitizerTest {

    private final LogSanitizer sanitizer = new LogSanitizer();

    @Test
    public void shouldRedactPassword() {
        String input = "password=MySecret123";

        String result = sanitizer.sanitize(input);

        Assert.assertFalse(result.contains("MySecret123"));
        Assert.assertTrue(result.contains("[REDACTED]"));
    }

    @Test
    public void shouldRedactApiKey() {
        String input = "api_key: ABC123XYZ";

        String result = sanitizer.sanitize(input);

        Assert.assertFalse(result.contains("ABC123XYZ"));
        Assert.assertTrue(result.contains("[REDACTED]"));
    }

    @Test
    public void shouldRedactBearerToken() {
        String input = "Authorization: Bearer abc.def.ghi";

        String result = sanitizer.sanitize(input);

        Assert.assertFalse(result.contains("abc.def.ghi"));
        Assert.assertTrue(result.contains("[REDACTED]"));
    }

    @Test
    public void shouldPreserveNormalLogs() {
        String input = "Login test failed because button was not visible";

        String result = sanitizer.sanitize(input);

        Assert.assertEquals(result, input);
    }

    @Test
    public void shouldHandleNullInput() {
        Assert.assertNull(sanitizer.sanitize(null));
    }
}