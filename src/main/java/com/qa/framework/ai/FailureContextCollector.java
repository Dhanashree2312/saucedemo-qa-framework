
package com.qa.framework.ai;

import com.qa.framework.config.ConfigManager;
import com.qa.framework.ai.model.FailureContext;

import java.time.Instant;
import java.util.UUID;

public class FailureContextCollector {

    private static final String EXECUTION_ID = resolveExecutionId();

    private final LogSanitizer logSanitizer = new LogSanitizer();

    public FailureContext collect(
            String testName,
            String platform,
            String failureMessage,
            String stackTrace,
            String logs,
            String screenshotPath,
            String timestamp) {

        return new FailureContext(
                EXECUTION_ID,
                normalize(testName),
                normalize(platform),
                ConfigManager.getEnvironment(),

                // Sanitize sensitive information before storing failure details.
                logSanitizer.sanitize(normalize(failureMessage)),
                logSanitizer.sanitize(normalize(stackTrace)),
                logSanitizer.sanitize(normalize(logs)),

                normalize(screenshotPath),

                timestamp == null || timestamp.isBlank()
                        ? Instant.now().toString()
                        : timestamp
        );
    }

    private static String resolveExecutionId() {
        String executionId = System.getProperty("execution.id");

        if (executionId != null && !executionId.isBlank()) {
            return executionId;
        }

        String githubRunId = System.getenv("GITHUB_RUN_ID");

        if (githubRunId != null && !githubRunId.isBlank()) {
            String attempt = System.getenv("GITHUB_RUN_ATTEMPT");

            return githubRunId + "-"
                    + (attempt != null ? attempt : "1");
        }

        return UUID.randomUUID().toString();
    }

    private String normalize(String value) {
        return value == null ? "" : value;
    }
}
