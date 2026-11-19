
package com.qa.framework.ai;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class LogPreprocessor {

    private static final DateTimeFormatter LOG_TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final ZoneId SYSTEM_ZONE = ZoneId.systemDefault();

    private static final int BUFFER_SECONDS = 5;
    private static final int MAX_LOG_LINES = 300;
    private static final int MAX_LOG_CHARACTERS = 40000;

    /**
     * Extracts log entries generated during a test execution.
     *
     * Includes a five-second buffer before and after execution.
     * Multiline stack traces and continuation lines are retained.
     */
    public String extractRelevantLogs(
            Path logFile,
            Instant startTime,
            Instant endTime,
            String testName) throws IOException {

        if (logFile == null || !Files.exists(logFile)) {
            return "Automation log file not found.";
        }

        if (startTime == null || endTime == null) {
            return "Test execution timestamps are unavailable.";
        }

        LocalDateTime start = startTime
                .atZone(SYSTEM_ZONE)
                .toLocalDateTime()
                .minusSeconds(BUFFER_SECONDS);

        LocalDateTime end = endTime
                .atZone(SYSTEM_ZONE)
                .toLocalDateTime()
                .plusSeconds(BUFFER_SECONDS);

        if (end.isBefore(start)) {
            LocalDateTime temp = start;
            start = end;
            end = temp;
        }

        List<String> relevantLines = new ArrayList<>();

        boolean includeCurrentEntry = false;
        int totalCharacters = 0;
        boolean truncated = false;

        try (BufferedReader reader = Files.newBufferedReader(
                logFile, StandardCharsets.UTF_8)) {

            String line;

            while ((line = reader.readLine()) != null) {

                LocalDateTime logTimestamp = extractTimestamp(line);

                // A timestamped line starts a new log entry.
                if (logTimestamp != null) {
                    includeCurrentEntry =
                            !logTimestamp.isBefore(start)
                                    && !logTimestamp.isAfter(end);
                }

                // Continuation lines belong to the previous log entry.
                if (!includeCurrentEntry) {
                    continue;
                }

                if (relevantLines.size() >= MAX_LOG_LINES
                        || totalCharacters + line.length()
                        > MAX_LOG_CHARACTERS) {

                    truncated = true;
                    break;
                }

                relevantLines.add(line);
                totalCharacters += line.length();
            }
        }

        if (relevantLines.isEmpty()) {
            return "No log entries found in the execution time window."
                    + "\nTest: " + normalize(testName)
                    + "\nLog file: " + logFile;
        }

        StringBuilder result = new StringBuilder();

        result.append("Relevant automation logs")
                .append("\nTest: ").append(normalize(testName))
                .append("\nLog file: ").append(logFile)
                .append("\nTime window: ").append(start)
                .append(" to ").append(end)
                .append("\n\n");

        for (String relevantLine : relevantLines) {
            result.append(relevantLine).append(System.lineSeparator());
        }

        if (truncated) {
            result.append("\n[Log output truncated: maximum size reached]");
        }

        return result.toString();
    }

    /**
     * Reads the timestamp at the beginning of a JUL log entry.
     *
     * Expected format:
     * 2026-11-19 11:24:35 [INFO] ...
     */
    private LocalDateTime extractTimestamp(String line) {

        if (line == null || line.length() < 19) {
            return null;
        }

        String timestampText = line.substring(0, 19);

        try {
            return LocalDateTime.parse(
                    timestampText,
                    LOG_TIMESTAMP_FORMAT
            );
        } catch (DateTimeParseException exception) {
            return null;
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank()
                ? "Unknown"
                : value;
    }
}
