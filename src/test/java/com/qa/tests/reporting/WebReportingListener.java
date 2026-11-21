
package com.qa.tests.reporting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.qa.framework.config.ConfigManager;

import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

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

public class WebReportingListener implements ITestListener, ISuiteListener {

    private static final Gson GSON =
            new GsonBuilder().setPrettyPrinting().create();

    private static final Path REPORT_DIRECTORY =
            Path.of("target", "reports", "web");

    private final List<Map<String, Object>> testRecords =
            new CopyOnWriteArrayList<>();

    @Override
    public void onStart(ISuite suite) {
        testRecords.clear();

        try {
            Files.createDirectories(REPORT_DIRECTORY);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Could not initialize Web reporting directory.", e
            );
        }
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

    private void recordTest(ITestResult result, String status) {

        String className = result.getTestClass().getName();

        // This listener is dedicated to Web tests.
        if (!className.startsWith("com.qa.tests.web.")) {
            return;
        }

        Map<String, Object> record = new LinkedHashMap<>();

        record.put("testName", result.getName());
        record.put("className", className);
        record.put("methodName", result.getMethod().getMethodName());
        record.put("platform", "web");
        record.put("status", status);

        record.put(
                "startTimeUtc",
                Instant.ofEpochMilli(result.getStartMillis()).toString()
        );

        record.put(
                "endTimeUtc",
                Instant.ofEpochMilli(result.getEndMillis()).toString()
        );

        record.put(
                "durationMs",
                Math.max(0, result.getEndMillis() - result.getStartMillis())
        );

        Throwable throwable = result.getThrowable();

        if (throwable != null) {
            record.put("failureType", throwable.getClass().getName());
            record.put("failureMessage", throwable.getMessage());

            StringWriter stackTrace = new StringWriter();

            throwable.printStackTrace(new PrintWriter(stackTrace));

            record.put("stackTrace", stackTrace.toString());
        }

        testRecords.add(record);
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
                : Math.round(passed * 10000.0 / executed) / 100.0;

        Map<String, Object> summary = new LinkedHashMap<>();

        summary.put("schemaVersion", "1.0");
        summary.put("generatedAtUtc", Instant.now().toString());
        summary.put("suiteName", suite.getName());
        summary.put("platform", "web");
        summary.put("environment", ConfigManager.getEnvironment());

        summary.put("totalTests", total);
        summary.put("passed", passed);
        summary.put("failed", failed);
        summary.put("skipped", skipped);
        summary.put("passRatePercent", passRate);

        summary.put("tests", testRecords);

        Map<String, Object> failures = new LinkedHashMap<>();

        failures.put("schemaVersion", "1.0");
        failures.put("generatedAtUtc", Instant.now().toString());
        failures.put("suiteName", suite.getName());
        failures.put("platform", "web");
        failures.put("failedTests", failedTests);

        writeReport("execution-summary.json", summary);
        writeReport("failed-tests.json", failures);

        System.out.println("======================================");
        System.out.println("WEB REPORTING COMPLETED");
        System.out.println("Total   : " + total);
        System.out.println("Passed  : " + passed);
        System.out.println("Failed  : " + failed);
        System.out.println("Skipped : " + skipped);
        System.out.println("======================================");
    }

    private long countStatus(String status) {
        return testRecords.stream()
                .filter(test -> status.equals(test.get("status")))
                .count();
    }

    private void writeReport(String fileName, Object data) {

        try {
            Files.createDirectories(REPORT_DIRECTORY);

            Files.writeString(
                    REPORT_DIRECTORY.resolve(fileName),
                    GSON.toJson(data),
                    StandardCharsets.UTF_8
            );

        } catch (Exception e) {
            throw new RuntimeException(
                    "Could not generate Web report: " + fileName, e
            );
        }
    }
}
