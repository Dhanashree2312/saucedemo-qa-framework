package com.qa.tests.reporting;

import io.qameta.allure.Allure;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ReportingAttachments {

    private static final Path EXECUTION_LOG =
            Path.of("logs", "automation.log");

    private ReportingAttachments() {
    }

    public static void attachExecutionLogs() {
        try {
            if (!Files.isRegularFile(EXECUTION_LOG)) {
                System.err.println(
                        "Execution log not found: " + EXECUTION_LOG
                );
                return;
            }

            byte[] logBytes = Files.readAllBytes(EXECUTION_LOG);

            if (logBytes.length == 0) {
                return;
            }

            Allure.addAttachment(
                    "Execution Logs",
                    "text/plain",
                    new ByteArrayInputStream(logBytes),
                    ".log"
            );

        } catch (Exception e) {
            System.err.println(
                    "Could not attach execution logs: " + e.getMessage()
            );
        }
    }

    public static void attachStackTrace(Throwable throwable) {
        if (throwable == null) {
            return;
        }

        try {
            java.io.StringWriter writer =
                    new java.io.StringWriter();

            throwable.printStackTrace(
                    new java.io.PrintWriter(writer)
            );

            Allure.addAttachment(
                    "Failure Stack Trace",
                    "text/plain",
                    writer.toString(),
                    ".txt"
            );

        } catch (Exception e) {
            System.err.println(
                    "Could not attach stack trace: " + e.getMessage()
            );
        }
    }
}