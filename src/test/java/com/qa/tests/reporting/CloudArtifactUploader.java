
package com.qa.tests.reporting;

import com.qa.framework.cloud.S3StorageService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CloudArtifactUploader {

    private static final Path REPORTS =
            Path.of("target/reports");

    private static final Path ALLURE_RESULTS =
            Path.of("target/allure-results");

    private static final Path ALLURE_HTML =
            Path.of("target/site/allure-maven-plugin");

    private static final Path SUREFIRE =
            Path.of("target/surefire-reports");

    public static void main(String[] args) {

        String executionId = args.length > 0
                ? args[0] : "local-execution";

        String testStatus = args.length > 1
                ? args[1] : "unknown";

        String latestPrefix = "saucedemo-reports/latest";
        String archivePrefix =
                "saucedemo-reports/executions/" + executionId;

        try {
            uploadBundle(latestPrefix, testStatus);
            uploadBundle(archivePrefix, testStatus);

            System.out.println(
                    "Complete Web + API + Mobile report uploaded."
            );

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to upload execution report to S3", e
            );
        }
    }

    private static void uploadBundle(
            String prefix, String testStatus) throws IOException {

        // Combined Allure HTML report at the root.
        uploadDirectoryIfPresent(
                ALLURE_HTML, prefix
        );

        // Individual platform evidence.
        uploadDirectoryIfPresent(
                REPORTS.resolve("web"), prefix + "/web"
        );

        uploadDirectoryIfPresent(
                REPORTS.resolve("api"), prefix + "/api"
        );

        uploadDirectoryIfPresent(
                REPORTS.resolve("mobile"), prefix + "/mobile"
        );

        // Raw Allure results and attachments.
        uploadDirectoryIfPresent(
                ALLURE_RESULTS, prefix + "/allure-results"
        );

        // Detailed TestNG reports.
        uploadDirectoryIfPresent(
                SUREFIRE, prefix + "/surefire-reports"
        );

        // Overall execution metadata.
        Path metadata = REPORTS.resolve("execution-metadata.json");

        if (Files.exists(metadata)) {
            S3StorageService.uploadFile(
                    metadata, prefix + "/execution-metadata.json"
            );
        }

        // Framework execution log, when available.
        Path log = Path.of("logs/automation.log");

        if (Files.exists(log)) {
            S3StorageService.uploadFile(
                    log, prefix + "/logs/automation.log"
            );
        }

        System.out.println(
                "Uploaded report bundle: " + prefix
                        + " | Test status: " + testStatus
        );
    }

    private static void uploadDirectoryIfPresent(
            Path directory, String prefix) throws IOException {

        if (Files.isDirectory(directory)) {
            S3StorageService.uploadDirectory(directory, prefix);
        } else {
            System.out.println(
                    "Optional report directory not found: " + directory
            );
        }
    }
}
