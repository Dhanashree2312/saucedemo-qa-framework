package com.qa.tests.reporting;

import com.qa.framework.cloud.S3StorageService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

public class CloudArtifactUploader {

    public static void main(String[] args) {

        Path resultsDirectory = Path.of("target/allure-results");

        // Default Allure Maven report output directory
        Path reportDirectory = Path.of(
                "target/site/allure-maven-plugin"
        );

        String executionId = Instant.now().toString()
                .replace(":", "-");

        String s3Prefix =
                "execution-reports/saucedemo-qa-framework/execution-"
                        + executionId;

        try {

            // Upload raw Allure results
            if (Files.exists(resultsDirectory)) {

                S3StorageService.uploadDirectory(
                        resultsDirectory,
                        s3Prefix + "/allure-results"
                );

            } else {
                System.out.println("Allure results directory not found.");
            }

            // Upload generated HTML report
            if (Files.exists(reportDirectory)) {

                S3StorageService.uploadDirectory(
                        reportDirectory,
                        s3Prefix + "/allure-report"
                );

            } else {
                System.out.println(
                        "Allure HTML report not found. Generate it first."
                );
            }

            System.out.println("Cloud upload process completed.");

        } catch (IOException e) {

            throw new RuntimeException(
                    "Unable to upload execution artifacts", e
            );
        }
    }
}
