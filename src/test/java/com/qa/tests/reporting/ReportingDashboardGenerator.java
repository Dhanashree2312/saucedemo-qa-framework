package com.qa.tests.reporting;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class ReportingDashboardGenerator {

    private static final Path REPORTS =
            Path.of("target", "reports");

    private static final Map<String, String> PLATFORMS =
            new LinkedHashMap<>();

    static {
        PLATFORMS.put("Web", "web");
        PLATFORMS.put("API", "api");
        PLATFORMS.put("Mobile", "mobile");
    }

    public static void main(String[] args) throws Exception {
        generate();
    }

    public static void generate() throws Exception {

        Files.createDirectories(REPORTS);

        long total = 0;
        long passed = 0;
        long failed = 0;
        long skipped = 0;

        StringBuilder rows = new StringBuilder();

        for (Map.Entry<String, String> platform
                : PLATFORMS.entrySet()) {

            String displayName = platform.getKey();
            String folder = platform.getValue();

            Path summaryPath = REPORTS
                    .resolve(folder)
                    .resolve("execution-summary.json");

            if (!Files.isRegularFile(summaryPath)) {
                throw new IllegalStateException(
                        "Missing execution summary: " + summaryPath
                );
            }

            JsonObject summary = JsonParser.parseString(
                    Files.readString(
                            summaryPath,
                            StandardCharsets.UTF_8
                    )
            ).getAsJsonObject();

            long platformTotal = getLong(summary, "totalTests");
            long platformPassed = getLong(summary, "passed");
            long platformFailed = getLong(summary, "failed");
            long platformSkipped = getLong(summary, "skipped");

            double passRate = getDouble(
                    summary,
                    "passRatePercent"
            );

            total += platformTotal;
            passed += platformPassed;
            failed += platformFailed;
            skipped += platformSkipped;

            String reportLink =
                    folder + "/allure-report/index.html";

            rows.append("<tr>")
                    .append("<td>").append(displayName).append("</td>")
                    .append("<td>").append(platformTotal).append("</td>")
                    .append("<td>").append(platformPassed).append("</td>")
                    .append("<td>").append(platformFailed).append("</td>")
                    .append("<td>").append(platformSkipped).append("</td>")
                    .append("<td>").append(
                            String.format("%.2f%%", passRate)
                    ).append("</td>")
                    .append("<td><a href=\"")
                    .append(reportLink)
                    .append("\">Open Allure Report</a></td>")
                    .append("</tr>");
        }

        long executed = total - skipped;

        double overallPassRate = executed == 0
                ? 0.0
                : Math.round(passed * 10000.0 / executed) / 100.0;

        rows.append("<tr class=\"overall\">")
                .append("<td><strong>Overall</strong></td>")
                .append("<td>").append(total).append("</td>")
                .append("<td>").append(passed).append("</td>")
                .append("<td>").append(failed).append("</td>")
                .append("<td>").append(skipped).append("</td>")
                .append("<td>").append(
                        String.format("%.2f%%", overallPassRate)
                ).append("</td>")
                .append("<td>—</td>")
                .append("</tr>");

        String html = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport"
                          content="width=device-width, initial-scale=1.0">
                    <title>SauceDemo QA Execution Dashboard</title>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            margin: 32px;
                            color: #202124;
                            background: #f7f8fa;
                        }
                        h1 {
                            margin-bottom: 8px;
                        }
                        .subtitle {
                            color: #5f6368;
                            margin-bottom: 24px;
                        }
                        table {
                            width: 100%;
                            border-collapse: collapse;
                            background: white;
                        }
                        th, td {
                            border: 1px solid #dadce0;
                            padding: 14px;
                            text-align: left;
                        }
                        th {
                            background: #eef1f5;
                        }
                        .overall {
                            background: #f0f4f8;
                        }
                        a {
                            color: #1457a6;
                            font-weight: 600;
                        }
                        .note {
                            margin-top: 24px;
                            color: #5f6368;
                            font-size: 13px;
                        }
                    </style>
                </head>
                <body>
                    <h1>SauceDemo QA Execution Dashboard</h1>
                    <div class="subtitle">
                        Consolidated Web, API and Mobile automation results
                    </div>

                    <table>
                        <thead>
                            <tr>
                                <th>Platform</th>
                                <th>Total</th>
                                <th>Passed</th>
                                <th>Failed</th>
                                <th>Skipped</th>
                                <th>Pass %</th>
                                <th>Detailed Report</th>
                            </tr>
                        </thead>
                        <tbody>
                """ + rows + """
                        </tbody>
                    </table>

                    <div class="note">
                        Each detailed report contains its platform's
                        Allure test results and available failure evidence.
                    </div>
                </body>
                </html>
                """;

        Files.writeString(
                REPORTS.resolve("index.html"),
                html,
                StandardCharsets.UTF_8
        );

        System.out.println(
                "Consolidated dashboard generated: "
                        + REPORTS.resolve("index.html").toAbsolutePath()
        );
    }

    private static long getLong(JsonObject object, String key) {
        return object.has(key) && !object.get(key).isJsonNull()
                ? object.get(key).getAsLong()
                : 0L;
    }

    private static double getDouble(JsonObject object, String key) {
        return object.has(key) && !object.get(key).isJsonNull()
                ? object.get(key).getAsDouble()
                : 0.0;
    }
}