package com.qa.tests.reporting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.qa.tests.web.BaseTest;
import com.qa.framework.config.ConfigManager;
import com.qa.framework.ai.FailureContextCollector;
import com.qa.framework.ai.model.FailureContext;

import com.microsoft.playwright.Page;

import io.qameta.allure.Allure;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestResult;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FailureScreenshotListener implements
        IInvokedMethodListener,
        ISuiteListener {

    private static final Logger LOGGER =
            Logger.getLogger(FailureScreenshotListener.class.getName());

    private final List<Map<String, Object>> testRecords =
            new CopyOnWriteArrayList<>();

    private final FailureContextCollector contextCollector =
            new FailureContextCollector();

    private final List<FailureContext> failureContexts =
            new CopyOnWriteArrayList<>();

    private static final Gson GSON =
            new GsonBuilder().setPrettyPrinting().create();

    private static final Path REPORT_DIR =
            Path.of("target", "reports", "web");

    private static final Path EVIDENCE_DIR =
            REPORT_DIR.resolve("evidence");

    @Override
    public void onStart(ISuite suite) {
        testRecords.clear();
        failureContexts.clear();

        try {
            Files.createDirectories(REPORT_DIR);
            Files.createDirectories(EVIDENCE_DIR);

            Files.deleteIfExists(
                    REPORT_DIR.resolve("execution-summary.json")
            );

            Files.deleteIfExists(
                    REPORT_DIR.resolve("failed-tests.json")
            );

        } catch (Exception e) {
            throw new RuntimeException(
                    "Could not initialize Web reporting.", e
            );
        }
    }

    @Override
    public void afterInvocation(
            IInvokedMethod method,
            ITestResult result) {

        // Handle failed TestNG configuration methods separately.
// Do not count configuration failures as ordinary test executions.
        if (!method.isTestMethod()) {

            if (result.getStatus() == ITestResult.FAILURE
                    && result.getTestClass() != null
                    && result.getTestClass().getName()
                    .startsWith("com.qa.tests.ui.")) {

                attachConfigurationFailure(result);
            }

            return;
        }

        String className = result.getTestClass().getName();

        if (!className.startsWith("com.qa.tests.ui.")) {
            return;
        }

        String status = getStatus(result);
        String methodName = result.getMethod().getMethodName();

        String browser = null;

        if (result.getTestContext() != null
                && result.getTestContext().getCurrentXmlTest() != null) {

            browser = result.getTestContext()
                    .getCurrentXmlTest()
                    .getParameter("browser");
        }

        if (browser == null || browser.isBlank()) {
            browser = ConfigManager.getBrowser();
        }

        Map<String, Object> record =
                new LinkedHashMap<>();

        record.put("testName", result.getName());
        record.put("className", className);
        record.put("methodName", methodName);
        record.put("platform", "web");
        record.put(
                "browser",
                browser != null ? browser : "unspecified"
        );
        record.put("status", status);

        record.put(
                "startTimeUtc",
                Instant.ofEpochMilli(
                        result.getStartMillis()
                ).toString()
        );

        record.put(
                "endTimeUtc",
                Instant.ofEpochMilli(
                        result.getEndMillis()
                ).toString()
        );

        record.put(
                "durationMs",
                Math.max(
                        0,
                        result.getEndMillis() - result.getStartMillis()
                )
        );

        if ("FAILED".equals(status)) {

            Throwable exception = result.getThrowable();

            String failureMessage = exception != null
                    ? exception.getMessage()
                    : "No exception message available";

            String stackTrace = "";
            String screenshotPath = "";

            record.put(
                    "failureType",
                    exception != null
                            ? exception.getClass().getName()
                            : "Unknown"
            );

            record.put("failureMessage", failureMessage);

            List<String> evidence = new ArrayList<>();

            if (exception != null) {

                StringWriter writer = new StringWriter();

                exception.printStackTrace(
                        new PrintWriter(writer)
                );

                stackTrace = writer.toString();

                record.put("stackTrace", stackTrace);

                try {
                    Files.createDirectories(EVIDENCE_DIR);

                    String baseName = safeName(className)
                            + "-"
                            + safeName(methodName)
                            + "-"
                            + safeName(browser)
                            + "-"
                            + result.getStartMillis();

                    Path exceptionFile =
                            EVIDENCE_DIR.resolve(baseName + ".txt");

                    Files.writeString(
                            exceptionFile,
                            stackTrace,
                            StandardCharsets.UTF_8
                    );

                    evidence.add(
                            REPORT_DIR.relativize(exceptionFile)
                                    .toString()
                                    .replace("\\", "/")
                    );

                    Allure.addAttachment(
                            "Failure Exception",
                            "text/plain",
                            new ByteArrayInputStream(
                                    stackTrace.getBytes(
                                            StandardCharsets.UTF_8
                                    )
                            ),
                            ".txt"
                    );

                } catch (Exception e) {
                    LOGGER.log(
                            Level.WARNING,
                            "Could not save Web exception evidence.",
                            e
                    );
                }
            }

            /*
             * Capture screenshot before BaseTest teardown.
             */
            if (result.getInstance() instanceof BaseTest baseTest) {

                Page page = baseTest.getPage();

                if (page != null) {
                    try {
                        Files.createDirectories(EVIDENCE_DIR);

                        byte[] screenshot = page.screenshot(
                                new Page.ScreenshotOptions()
                                        .setFullPage(true)
                        );

                        String baseName = safeName(className)
                                + "-"
                                + safeName(methodName)
                                + "-"
                                + safeName(browser)
                                + "-"
                                + result.getStartMillis();

                        Path screenshotFile =
                                EVIDENCE_DIR.resolve(baseName + ".png");

                        Files.write(screenshotFile, screenshot);

                        screenshotPath =
                                screenshotFile.toAbsolutePath().toString();

                        evidence.add(
                                REPORT_DIR.relativize(screenshotFile)
                                        .toString()
                                        .replace("\\", "/")
                        );

                        Allure.addAttachment(
                                "Failure Screenshot",
                                "image/png",
                                new ByteArrayInputStream(screenshot),
                                ".png"
                        );

                        LOGGER.info("Web failure screenshot captured.");

                    } catch (Exception e) {
                        LOGGER.log(
                                Level.WARNING,
                                "Could not capture Web failure screenshot.",
                                e
                        );
                    }
                }
            }

            /*
             * Reporting attachment only. GenAI log analysis is unchanged.
             */
            ReportingAttachments.attachExecutionLogs();

            evidence.add("logs/automation.log");
            record.put("evidence", evidence);

            /*
             * Preserve the existing GenAI failure context flow.
             * Do not pass LogPreprocessor output into this flow here.
             */
            try {
                FailureContext context = contextCollector.collect(
                        result.getName(),
                        "web",
                        failureMessage,
                        stackTrace,
                        "",
                        screenshotPath,
                        Instant.ofEpochMilli(
                                result.getEndMillis()
                        ).toString()
                );

                failureContexts.add(context);

                LOGGER.info(
                        "GenAI failure context created for: "
                                + result.getName()
                );

            } catch (Exception e) {
                LOGGER.log(
                        Level.WARNING,
                        "Could not create GenAI failure context.",
                        e
                );
            }
        }

        testRecords.add(record);

        LOGGER.info(
                "Recorded Web test result: "
                        + methodName + " - " + status
        );
    }

    private String getStatus(ITestResult result) {
        return switch (result.getStatus()) {
            case ITestResult.SUCCESS -> "PASSED";
            case ITestResult.FAILURE -> "FAILED";
            case ITestResult.SKIP -> "SKIPPED";
            default -> "UNKNOWN";
        };
    }

    private String safeName(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }

        return value.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    @Override
    public void onFinish(ISuite suite) {

        try {
            Files.createDirectories(REPORT_DIR);

            int total = testRecords.size();

            long passed = count("PASSED");
            long failed = count("FAILED");
            long skipped = count("SKIPPED");

            long executed = total - skipped;

            double passRate = executed == 0
                    ? 0.0
                    : Math.round(
                    passed * 10000.0 / executed
            ) / 100.0;

            Map<String, Object> summary =
                    new LinkedHashMap<>();

            summary.put("schemaVersion", "1.0");
            summary.put("generatedAtUtc", Instant.now().toString());
            summary.put("suiteName", suite.getName());
            summary.put("platform", "web");
            summary.put(
                    "environment",
                    ConfigManager.getEnvironment()
            );

            summary.put("totalTests", total);
            summary.put("passed", passed);
            summary.put("failed", failed);
            summary.put("skipped", skipped);
            summary.put("passRatePercent", passRate);
            summary.put("tests", testRecords);

            Files.writeString(
                    REPORT_DIR.resolve("execution-summary.json"),
                    GSON.toJson(summary),
                    StandardCharsets.UTF_8
            );

            Map<String, Object> failures =
                    new LinkedHashMap<>();

            failures.put("schemaVersion", "1.0");
            failures.put("generatedAtUtc", Instant.now().toString());
            failures.put("suiteName", suite.getName());
            failures.put("platform", "web");

            failures.put(
                    "failedTests",
                    testRecords.stream()
                            .filter(test ->
                                    "FAILED".equals(test.get("status")))
                            .toList()
            );

            Files.writeString(
                    REPORT_DIR.resolve("failed-tests.json"),
                    GSON.toJson(failures),
                    StandardCharsets.UTF_8
            );

            LOGGER.info(
                    "Web structured reports generated at: "
                            + REPORT_DIR.toAbsolutePath()
            );

            LOGGER.info(
                    "Total GenAI failure contexts collected: "
                            + failureContexts.size()
            );

        } catch (Exception e) {
            throw new RuntimeException(
                    "Could not generate Web structured reports.", e
            );
        }
    }

    private long count(String status) {
        return testRecords.stream()
                .filter(test ->
                        status.equals(test.get("status")))
                .count();
    }

    private void attachConfigurationFailure(ITestResult result) {

        Throwable exception = result.getThrowable();

        String methodName = result.getMethod().getMethodName();

        String className = result.getTestClass().getName();

        StringWriter writer = new StringWriter();

        if (exception != null) {
            exception.printStackTrace(new PrintWriter(writer));
        }

        String stackTrace = writer.toString();

        try {
            Files.createDirectories(EVIDENCE_DIR);

            String baseName = safeName(className)
                    + "-"
                    + safeName(methodName)
                    + "-configuration-"
                    + result.getStartMillis();

            Path exceptionFile =
                    EVIDENCE_DIR.resolve(baseName + ".txt");

            Files.writeString(
                    exceptionFile,
                    stackTrace.isBlank()
                            ? "No stack trace available."
                            : stackTrace,
                    StandardCharsets.UTF_8
            );

            Allure.addAttachment(
                    "Configuration Failure Exception",
                    "text/plain",
                    new ByteArrayInputStream(
                            stackTrace.getBytes(StandardCharsets.UTF_8)
                    ),
                    ".txt"
            );

            Allure.addAttachment(
                    "Configuration Failure Details",
                    "text/plain",
                    "Method: " + methodName
                            + "\nClass: " + className
                            + "\nFailure: "
                            + (exception != null
                            ? exception.getMessage()
                            : "Unknown"),
                    ".txt"
            );

            ReportingAttachments.attachExecutionLogs();

            LOGGER.info(
                    "Configuration failure evidence attached for: "
                            + className + "." + methodName
            );

            // Preserve the existing failure-context collection flow.
            FailureContext context = contextCollector.collect(
                    className + "." + methodName,
                    "web",
                    exception != null ? exception.getMessage() : "",
                    stackTrace,
                    "",
                    "",
                    Instant.ofEpochMilli(result.getEndMillis()).toString()
            );

            failureContexts.add(context);

        } catch (Exception e) {

            LOGGER.log(
                    Level.WARNING,
                    "Could not attach configuration failure evidence.",
                    e
            );
        }
    }
}