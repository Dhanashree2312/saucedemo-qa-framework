package com.qa.tests.reporting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.qa.framework.api.APIClient;
import com.qa.framework.config.ConfigManager;

import io.qameta.allure.Allure;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.ITestListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class ApiReportingListener implements
        IInvokedMethodListener,
        ITestListener,
        ISuiteListener {

    private static final Gson GSON =
            new GsonBuilder().setPrettyPrinting().create();

    private static final Path REPORT_DIRECTORY =
            Path.of("target", "reports", "api");

    private final List<Map<String, Object>> testRecords =
            new CopyOnWriteArrayList<>();

    @Override
    public void onStart(ISuite suite) {
        testRecords.clear();
        APIClient.clearRequestRecords();

        try {
            Files.createDirectories(REPORT_DIRECTORY);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Could not initialize API reporting directory.", e
            );
        }
    }

    @Override
    public void beforeInvocation(
            IInvokedMethod method,
            ITestResult result) {

        if (method.isTestMethod()) {
            APIClient.setCurrentTestName(getTestId(result));
        }
    }

    @Override
    public void afterInvocation(
            IInvokedMethod method,
            ITestResult result) {

        if (!method.isTestMethod()) {
            return;
        }

        /*
         * Attach evidence while the test invocation is still active.
         * Do this before clearing the API test association.
         */
        if (result.getStatus() == ITestResult.FAILURE) {
            ReportingAttachments.attachStackTrace(
                    result.getThrowable()
            );

            ReportingAttachments.attachExecutionLogs();
        }

        APIClient.clearCurrentTestName();
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        recordTest(result, "PASSED");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        recordTest(result, "FAILED");
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        recordTest(result, "SKIPPED");
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(
            ITestResult result) {
        recordTest(result, "FAILED");
    }

    private void recordTest(
            ITestResult result,
            String status) {

        String testId = getTestId(result);

        Map<String, Object> record =
                new LinkedHashMap<>();

        record.put("testName", result.getName());
        record.put(
                "className",
                result.getTestClass().getName()
        );
        record.put(
                "methodName",
                result.getMethod().getMethodName()
        );
        record.put("platform", "api");
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

        List<Map<String, Object>> associatedRequests =
                APIClient.getRequestRecords()
                        .stream()
                        .filter(request ->
                                testId.equals(request.get("testName")))
                        .toList();

        record.put("apiRequests", associatedRequests);

        Throwable throwable = result.getThrowable();

        if (throwable != null) {
            record.put(
                    "failureType",
                    throwable.getClass().getName()
            );

            record.put(
                    "failureMessage",
                    throwable.getMessage()
            );

            StringWriter stackTrace =
                    new StringWriter();

            throwable.printStackTrace(
                    new PrintWriter(stackTrace)
            );

            record.put(
                    "stackTrace",
                    stackTrace.toString()
            );
        }

        testRecords.add(record);
    }

    private String getTestId(ITestResult result) {
        return result.getTestClass().getName()
                + "."
                + result.getMethod().getMethodName();
    }

    @Override
    public void onFinish(ISuite suite) {

        List<Map<String, Object>> failedTests =
                testRecords.stream()
                        .filter(test ->
                                "FAILED".equals(test.get("status")))
                        .toList();

        int total = testRecords.size();

        long passed = countStatus("PASSED");
        long failed = countStatus("FAILED");
        long skipped = countStatus("SKIPPED");

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
        summary.put("platform", "api");
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

        /*
         * APIClient already sanitizes and records request/response data.
         * Keep that existing implementation as the source of API evidence.
         */
        summary.put(
                "apiRequests",
                APIClient.getRequestRecords()
        );

        Map<String, Object> failures =
                new LinkedHashMap<>();

        failures.put("schemaVersion", "1.0");
        failures.put("generatedAtUtc", Instant.now().toString());
        failures.put("suiteName", suite.getName());
        failures.put("platform", "api");
        failures.put("failedTests", failedTests);

        writeReport("execution-summary.json", summary);
        writeReport("failed-tests.json", failures);
    }

    private long countStatus(String status) {
        return testRecords.stream()
                .filter(test ->
                        status.equals(test.get("status")))
                .count();
    }

    private void writeReport(
            String fileName,
            Object data) {

        try {
            Files.createDirectories(REPORT_DIRECTORY);

            Files.writeString(
                    REPORT_DIRECTORY.resolve(fileName),
                    GSON.toJson(data),
                    StandardCharsets.UTF_8
            );

        } catch (Exception e) {
            throw new RuntimeException(
                    "Could not generate API report: " + fileName,
                    e
            );
        }
    }
}