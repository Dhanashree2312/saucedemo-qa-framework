
package com.qa.tests.reporting;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.file.Files;
import java.nio.file.Path;

public class ReportingPipelineFinalizer {

    private static final Path SUMMARY =
            Path.of("target", "reports", "suite-summary.json");

    public static void main(String[] args) throws Exception {

        String executionId = args.length > 0
                ? args[0]
                : "local-execution";

        // Generate the authoritative suite summary and dashboard.
        ReportingDashboardGenerator.generate();

        if (!Files.isRegularFile(SUMMARY)) {
            throw new IllegalStateException(
                    "Suite summary was not generated: " + SUMMARY
            );
        }

        JsonObject summary = JsonParser.parseString(
                Files.readString(SUMMARY)
        ).getAsJsonObject();

        long total = getLong(summary, "totalTests");
        long passed = getLong(summary, "passed");
        long failed = getLong(summary, "failed");
        long skipped = getLong(summary, "skipped");
        long ignored = getLong(summary, "ignored");

        if (total == 0) {
            throw new IllegalStateException(
                    "No test execution results found. Refusing to upload "
                            + "an empty reporting bundle."
            );
        }

        // Do not add listener summary failures to Surefire/TestNG failures.
        // They describe the same test executions and would double-count.
        String status = failed > 0 ? "FAILED" : "PASSED";

        System.out.println("======================================");
        System.out.println("REPORTING PIPELINE FINALIZATION");
        System.out.println("Execution ID : " + executionId);
        System.out.println("Test status  : " + status);
        System.out.println("Total        : " + total);
        System.out.println("Passed       : " + passed);
        System.out.println("Failed       : " + failed);
        System.out.println("Skipped      : " + skipped);
        System.out.println("Ignored      : " + ignored);
        System.out.println("======================================");

        // Upload the reports before failing the Maven build.

        CloudArtifactUploader.main(
                new String[]{executionId, status}
        );

        System.out.println("Reporting upload completed.");

        if (failed > 0) {
            throw new IllegalStateException(
                    failed + " automated test(s) failed. "
                            + "Reports and evidence were uploaded to S3."
            );
        }

        System.out.println(
                "Reporting pipeline completed successfully."
        );
    }

    private static long getLong(JsonObject json, String key) {
        if (!json.has(key) || json.get(key).isJsonNull()) {
            return 0;
        }

        return json.get(key).getAsLong();
    }
}
