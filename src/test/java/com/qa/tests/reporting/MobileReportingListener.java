package com.qa.tests.reporting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.qa.framework.config.ConfigManager;
import com.qa.tests.mobile.BaseNativeTest;

import io.appium.java_client.android.AndroidDriver;
import io.qameta.allure.Allure;

import org.openqa.selenium.OutputType;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestResult;
import org.testng.IConfigurationListener;

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

public class MobileReportingListener implements
        IInvokedMethodListener,
        ISuiteListener,
        IConfigurationListener {

    private static final Logger LOGGER =
            Logger.getLogger(MobileReportingListener.class.getName());

    private static final List<Map<String, Object>> TEST_RECORDS =
            new CopyOnWriteArrayList<>();

    private static final List<Map<String, Object>> CONFIGURATION_FAILURES =
            new CopyOnWriteArrayList<>();

    private static final Gson GSON =
            new GsonBuilder().setPrettyPrinting().create();

    private static final Path REPORT_DIR =
            Path.of("target", "reports", "mobile");

    private static final Path EVIDENCE_DIR =
            REPORT_DIR.resolve("evidence");

    @Override
    public void onStart(ISuite suite) {
        TEST_RECORDS.clear();
        CONFIGURATION_FAILURES.clear();

        try {
            Files.createDirectories(REPORT_DIR);
            Files.createDirectories(EVIDENCE_DIR);

            Files.deleteIfExists(
                    REPORT_DIR.resolve("execution-summary.json")
            );

            Files.deleteIfExists(
                    REPORT_DIR.resolve("failed-tests.json")
            );

            LOGGER.info("Mobile reporting initialized.");

        } catch (Exception e) {
            throw new RuntimeException(
                    "Could not initialize mobile reporting.", e
            );
        }
    }

    @Override
    public void afterInvocation(
            IInvokedMethod method,
            ITestResult result) {

        if (!method.isTestMethod()) {
            return;
        }

        String status = getStatus(result);

        String className = result.getTestClass().getName();
        String methodName = result.getMethod().getMethodName();

        Map<String, Object> record =
                new LinkedHashMap<>();

        record.put("testName", result.getName());
        record.put("className", className);
        record.put("methodName", methodName);
        record.put("platform", "mobile");
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

        List<String> evidence = new ArrayList<>();

        Throwable exception = result.getThrowable();

        if ("FAILED".equals(status)) {

            record.put(
                    "failureType",
                    exception != null
                            ? exception.getClass().getName()
                            : "Unknown"
            );

            record.put(
                    "failureMessage",
                    exception != null
                            ? exception.getMessage()
                            : "No exception message available"
            );

            if (exception != null) {
                StringWriter writer = new StringWriter();

                exception.printStackTrace(
                        new PrintWriter(writer)
                );

                String stackTrace = writer.toString();

                record.put("stackTrace", stackTrace);

                try {
                    Files.createDirectories(EVIDENCE_DIR);

                    String baseName = safeName(className)
                            + "-"
                            + safeName(methodName)
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
                            "Mobile Failure Exception",
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
                            "Could not save mobile exception evidence.",
                            e
                    );
                }
            }

            /*
             * Capture the screenshot before BaseNativeTest teardown.
             */
            if (result.getInstance() instanceof BaseNativeTest baseTest) {

                AndroidDriver driver = baseTest.getDriver();

                if (driver != null) {
                    try {
                        Files.createDirectories(EVIDENCE_DIR);

                        byte[] screenshot =
                                driver.getScreenshotAs(OutputType.BYTES);

                        String baseName = safeName(className)
                                + "-"
                                + safeName(methodName)
                                + "-"
                                + result.getStartMillis();

                        Path screenshotFile =
                                EVIDENCE_DIR.resolve(baseName + ".png");

                        Files.write(screenshotFile, screenshot);

                        evidence.add(
                                REPORT_DIR.relativize(screenshotFile)
                                        .toString()
                                        .replace("\\", "/")
                        );

                        Allure.addAttachment(
                                "Mobile Failure Screenshot",
                                "image/png",
                                new ByteArrayInputStream(screenshot),
                                ".png"
                        );

                        LOGGER.info(
                                "Mobile failure screenshot captured."
                        );

                    } catch (Exception e) {
                        LOGGER.log(
                                Level.WARNING,
                                "Could not capture mobile screenshot.",
                                e
                        );
                    }
                }
            }

            /*
             * Attach the existing framework execution log to Allure.
             * This does not invoke or modify the GenAI log analyser.
             */
            ReportingAttachments.attachExecutionLogs();

            evidence.add("logs/automation.log");

            record.put("evidence", evidence);
        }

        TEST_RECORDS.add(record);

        LOGGER.info(
                "Recorded mobile test result: "
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
    public void onConfigurationFailure(ITestResult result) {

        Throwable exception = result.getThrowable();

        Map<String, Object> record =
                new LinkedHashMap<>();

        String className = result.getTestClass() != null
                ? result.getTestClass().getName()
                : "unknown";

        String methodName = result.getMethod() != null
                ? result.getMethod().getMethodName()
                : "unknown";

        record.put("configurationMethod", methodName);
        record.put("className", className);
        record.put("status", "FAILED");

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

        if (exception != null) {

            record.put(
                    "failureType",
                    exception.getClass().getName()
            );

            record.put(
                    "failureMessage",
                    exception.getMessage()
            );

            StringWriter writer = new StringWriter();

            exception.printStackTrace(
                    new PrintWriter(writer)
            );

            String stackTrace = writer.toString();

            record.put("stackTrace", stackTrace);

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
                        stackTrace,
                        StandardCharsets.UTF_8
                );

                record.put(
                        "evidence",
                        REPORT_DIR.relativize(exceptionFile)
                                .toString()
                                .replace("\\", "/")
                );

            } catch (Exception e) {
                LOGGER.log(
                        Level.WARNING,
                        "Could not save configuration failure evidence.",
                        e
                );
            }
        }

        CONFIGURATION_FAILURES.add(record);

        LOGGER.severe(
                "Mobile configuration failure: "
                        + className + "." + methodName
        );
    }

    @Override
    public void onFinish(ISuite suite) {

        try {
            Files.createDirectories(REPORT_DIR);

            int total = TEST_RECORDS.size();

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
            summary.put("platform", "mobile");
            summary.put(
                    "environment",
                    ConfigManager.getEnvironment()
            );

            summary.put("totalTests", total);
            summary.put("passed", passed);
            summary.put("failed", failed);
            summary.put("skipped", skipped);
            summary.put("passRatePercent", passRate);

            summary.put("tests", TEST_RECORDS);
            summary.put(
                    "configurationFailures",
                    CONFIGURATION_FAILURES
            );

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
            failures.put("platform", "mobile");

            failures.put(
                    "failedTests",
                    TEST_RECORDS.stream()
                            .filter(test ->
                                    "FAILED".equals(test.get("status")))
                            .toList()
            );

            failures.put(
                    "failedConfigurations",
                    CONFIGURATION_FAILURES
            );

            Files.writeString(
                    REPORT_DIR.resolve("failed-tests.json"),
                    GSON.toJson(failures),
                    StandardCharsets.UTF_8
            );

            LOGGER.info(
                    "Mobile reports generated at: "
                            + REPORT_DIR.toAbsolutePath()
            );

        } catch (Exception e) {
            throw new RuntimeException(
                    "Could not generate mobile reports.", e
            );
        }
    }

    private long count(String status) {
        return TEST_RECORDS.stream()
                .filter(test ->
                        status.equals(test.get("status")))
                .count();
    }
}