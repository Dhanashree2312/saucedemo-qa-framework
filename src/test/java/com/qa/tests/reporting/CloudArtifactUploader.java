package com.qa.tests.reporting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.qa.framework.cloud.S3StorageService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public class CloudArtifactUploader {

    private static final Path REPORTS =
            Path.of("target", "reports");

    private static final Path ALLURE_RESULTS =
            Path.of("target", "allure-results");

    private static final Path SUREFIRE =
            Path.of("target", "surefire-reports");

    private static final Path LOG =
            Path.of("logs", "automation.log");

    private static final Gson GSON =
            new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) {

        String executionId = args.length > 0
                ? args[0]
                : "local-execution";

        String testStatus = args.length > 1
                ? args[1]
                : "unknown";

        String latestPrefix =
                "saucedemo-reports/latest";

        String archivePrefix =
                "saucedemo-reports/executions/" + executionId;

        try {
            validateReports();
            writeExecutionMetadata(executionId, testStatus);

            uploadBundle(latestPrefix);
            uploadBundle(archivePrefix);

            System.out.println(
                    "Complete Web + API + Mobile reporting bundle uploaded."
            );

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to upload reporting bundle to S3.", e
            );
        }
    }

    private static void validateReports() throws IOException {

        requireFile(
                REPORTS.resolve("index.html"),
                "Consolidated dashboard"
        );

        for (String platform : new String[]{
                "web", "api", "mobile"
        }) {
            Path platformDir = REPORTS.resolve(platform);

            requireFile(
                    platformDir.resolve("execution-summary.json"),
                    platform + " execution summary"
            );

            requireFile(
                    platformDir.resolve("failed-tests.json"),
                    platform + " failed-tests report"
            );

            requireFile(
                    platformDir.resolve("allure-report")
                            .resolve("index.html"),
                    platform + " Allure HTML report"
            );
        }

        if (!Files.isDirectory(ALLURE_RESULTS)) {
            throw new IOException(
                    "Allure results directory not found: "
                            + ALLURE_RESULTS
            );
        }

        if (!Files.isDirectory(SUREFIRE)) {
            throw new IOException(
                    "Surefire reports directory not found: "
                            + SUREFIRE
            );
        }
    }

    private static void requireFile(
            Path file,
            String description) throws IOException {

        if (!Files.isRegularFile(file)
                || Files.size(file) == 0) {

            throw new IOException(
                    description + " is missing or empty: " + file
            );
        }
    }

    private static void writeExecutionMetadata(
            String executionId,
            String testStatus) throws IOException {

        Map<String, Object> metadata =
                new LinkedHashMap<>();

        metadata.put("executionId", executionId);
        metadata.put("testStatus", testStatus);
        metadata.put("generatedAtUtc", Instant.now().toString());

        metadata.put(
                "platforms",
                new String[]{"web", "api", "mobile"}
        );

        Files.createDirectories(REPORTS);

        Files.writeString(
                REPORTS.resolve("execution-metadata.json"),
                GSON.toJson(metadata),
                StandardCharsets.UTF_8
        );
    }

    private static void uploadBundle(
            String prefix) throws IOException {

        /*
         * Upload dashboard, platform reports, JSON summaries,
         * failure evidence and individual Allure HTML reports.
         */
        S3StorageService.uploadDirectory(REPORTS, prefix);

        /*
         * Upload raw Allure result JSON and attachments.
         */
        S3StorageService.uploadDirectory(
                ALLURE_RESULTS,
                prefix + "/allure-results"
        );

        /*
         * Upload detailed Surefire/TestNG reports.
         */
        S3StorageService.uploadDirectory(
                SUREFIRE,
                prefix + "/surefire-reports"
        );

        /*
         * Upload framework execution log if available.
         */
        if (Files.isRegularFile(LOG)) {
            S3StorageService.uploadFile(
                    LOG,
                    prefix + "/logs/automation.log"
            );
        }

        System.out.println("Uploaded reporting bundle: " + prefix);
    }
}