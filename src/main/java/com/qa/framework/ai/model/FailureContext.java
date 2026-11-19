package com.qa.framework.ai.model;

public class FailureContext {

    private final String executionId;
    private final String testName;
    private final String platform;
    private final String environment;
    private final String failureMessage;
    private final String stackTrace;
    private final String logs;
    private final String screenshotPath;
    private final String timestamp;

    public FailureContext(
            String executionId,
            String testName,
            String platform,
            String environment,
            String failureMessage,
            String stackTrace,
            String logs,
            String screenshotPath,
            String timestamp) {

        this.executionId = executionId;
        this.testName = testName;
        this.platform = platform;
        this.environment = environment;
        this.failureMessage = failureMessage;
        this.stackTrace = stackTrace;
        this.logs = logs;
        this.screenshotPath = screenshotPath;
        this.timestamp = timestamp;
    }

    public String getExecutionId() {
        return executionId;
    }

    public String getTestName() {
        return testName;
    }

    public String getPlatform() {
        return platform;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getFailureMessage() {
        return failureMessage;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public String getLogs() {
        return logs;
    }

    public String getScreenshotPath() {
        return screenshotPath;
    }

    public String getTimestamp() {
        return timestamp;
    }
}