
package com.qa.tests.reporting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public class ReportingDashboardGenerator {

    private static final Path REPORTS =
            Path.of("target", "reports");

    private static final Path TESTNG_RESULTS =
            Path.of("target", "surefire-reports", "testng-results.xml");

    private static final Gson GSON =
            new GsonBuilder().setPrettyPrinting().create();

    private static final Map<String, Counts> PLATFORMS =
            new LinkedHashMap<>();

    static {
        PLATFORMS.put("Web", new Counts());
        PLATFORMS.put("API", new Counts());
        PLATFORMS.put("Mobile", new Counts());
        PLATFORMS.put("Unclassified", new Counts());
    }

    public static void main(String[] args) throws Exception {
        generate();
    }

    public static synchronized void generate() throws Exception {

        // Make repeated dashboard generation idempotent.
        resetPlatformCounts();

        if (!Files.isRegularFile(TESTNG_RESULTS)) {
            throw new IllegalStateException(
                    "TestNG results file not found: "
                            + TESTNG_RESULTS.toAbsolutePath()
            );
        }

        Files.createDirectories(REPORTS);

        readTestNGResults();

        Counts web = PLATFORMS.get("Web");
        Counts api = PLATFORMS.get("API");
        Counts mobile = PLATFORMS.get("Mobile");
        Counts unclassified = PLATFORMS.get("Unclassified");

        long total = 0;
        long passed = 0;
        long failed = 0;
        long skipped = 0;
        long ignored = 0;

        for (Counts counts : PLATFORMS.values()) {
            total += counts.total();
            passed += counts.passed;
            failed += counts.failed;
            skipped += counts.skipped;
            ignored += counts.ignored;
        }

        if (total == 0) {
            throw new IllegalStateException(
                    "No TestNG test methods were found. "
                            + "Dashboard was not generated."
            );
        }

        double passRate = passed + failed == 0
                ? 0.0
                : Math.round(
                passed * 10000.0 / (passed + failed)
        ) / 100.0;

        writeSuiteSummary(
                total,
                passed,
                failed,
                skipped,
                ignored,
                passRate
        );

        StringBuilder rows = new StringBuilder();

        appendPlatformRow(rows, "Web", web, "web");
        appendPlatformRow(rows, "API", api, "api");
        appendPlatformRow(rows, "Mobile", mobile, "mobile");

        if (unclassified.total() > 0) {
            appendPlatformRow(
                    rows,
                    "Unclassified",
                    unclassified,
                    null
            );
        }

        rows.append("""
                <tr class="overall">
                    <td><strong>Overall</strong></td>
                """)
                .append("<td>").append(total).append("</td>")
                .append("<td>").append(passed).append("</td>")
                .append("<td>").append(failed).append("</td>")
                .append("<td>").append(skipped).append("</td>")
                .append("<td>").append(ignored).append("</td>")
                .append("<td>")
                .append(String.format("%.2f%%", passRate))
                .append("</td>")
                .append("<td>—</td></tr>");

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

                        h1 { margin-bottom: 8px; }

                        .subtitle {
                            color: #5f6368;
                            margin-bottom: 20px;
                        }

                        .cards {
                            display: flex;
                            flex-wrap: wrap;
                            gap: 16px;
                            margin-bottom: 24px;
                        }

                        .card {
                            background: white;
                            padding: 18px;
                            border-radius: 8px;
                            min-width: 130px;
                            box-shadow: 0 1px 4px #00000018;
                        }

                        .card h2 {
                            margin: 0;
                            font-size: 26px;
                        }

                        .card p {
                            margin-bottom: 0;
                            color: #5f6368;
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

                        th { background: #eef1f5; }

                        .overall { background: #eaf0f7; }

                        a {
                            color: #1457a6;
                            font-weight: 600;
                        }

                        .note {
                            margin-top: 22px;
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

                    <div class="cards">
                        <div class="card">
                            <h2>TOTAL_VALUE</h2>
                            <p>Total tests</p>
                        </div>
                        <div class="card">
                            <h2>PASSED_VALUE</h2>
                            <p>Passed</p>
                        </div>
                        <div class="card">
                            <h2>FAILED_VALUE</h2>
                            <p>Failed</p>
                        </div>
                        <div class="card">
                            <h2>SKIPPED_VALUE</h2>
                            <p>Skipped</p>
                        </div>
                        <div class="card">
                            <h2>IGNORED_VALUE</h2>
                            <p>Ignored</p>
                        </div>
                        <div class="card">
                            <h2>RATE_VALUE</h2>
                            <p>Pass rate</p>
                        </div>
                    </div>

                    <h2>Platform Execution Summary</h2>

                    <table>
                        <thead>
                            <tr>
                                <th>Platform</th>
                                <th>Total</th>
                                <th>Passed</th>
                                <th>Failed</th>
                                <th>Skipped</th>
                                <th>Ignored</th>
                                <th>Pass %</th>
                                <th>Detailed Report</th>
                            </tr>
                        </thead>
                        <tbody>
                            ROWS_PLACEHOLDER
                        </tbody>
                    </table>

                    <div class="note">
                        Counts are calculated directly from the Maven
                        Surefire TestNG results XML. Platform JSON summaries
                        are not used as the source of dashboard counts.
                    </div>

                </body>
                </html>
                """;

        html = html
                .replace("TOTAL_VALUE", String.valueOf(total))
                .replace("PASSED_VALUE", String.valueOf(passed))
                .replace("FAILED_VALUE", String.valueOf(failed))
                .replace("SKIPPED_VALUE", String.valueOf(skipped))
                .replace("IGNORED_VALUE", String.valueOf(ignored))
                .replace(
                        "RATE_VALUE",
                        String.format("%.2f%%", passRate)
                )
                .replace("ROWS_PLACEHOLDER", rows.toString());

        Files.writeString(
                REPORTS.resolve("index.html"),
                html,
                StandardCharsets.UTF_8
        );

        System.out.println();
        System.out.println("======================================");
        System.out.println("CONSOLIDATED QA DASHBOARD GENERATED");
        System.out.println("======================================");

        printPlatformCounts("WEB", web);
        printPlatformCounts("API", api);
        printPlatformCounts("MOBILE", mobile);

        if (unclassified.total() > 0) {
            printPlatformCounts("UNCLASSIFIED", unclassified);
        }

        System.out.println("--------------------------------------");
        System.out.printf(
                "TOTAL: %d | PASSED: %d | FAILED: %d | "
                        + "SKIPPED: %d | IGNORED: %d | PASS RATE: %.2f%%%n",
                total,
                passed,
                failed,
                skipped,
                ignored,
                passRate
        );

        System.out.println("Dashboard: "
                + REPORTS.resolve("index.html").toAbsolutePath());

        System.out.println("======================================");
    }

    private static void resetPlatformCounts() {
        for (Counts counts : PLATFORMS.values()) {
            counts.reset();
        }
    }

    private static void readTestNGResults() throws Exception {

        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();

        factory.setFeature(
                "http://apache.org/xml/features/disallow-doctype-decl",
                true
        );

        factory.setFeature(
                "http://xml.org/sax/features/external-general-entities",
                false
        );

        factory.setFeature(
                "http://xml.org/sax/features/external-parameter-entities",
                false
        );

        factory.setAttribute(
                XMLConstants.ACCESS_EXTERNAL_DTD,
                ""
        );

        factory.setAttribute(
                XMLConstants.ACCESS_EXTERNAL_SCHEMA,
                ""
        );

        Document document = factory.newDocumentBuilder()
                .parse(TESTNG_RESULTS.toFile());

        NodeList classes = document.getElementsByTagName("class");

        for (int i = 0; i < classes.getLength(); i++) {

            Element classElement = (Element) classes.item(i);

            String className = classElement.getAttribute("name");

            Counts counts = getPlatformCounts(className);

            NodeList methods =
                    classElement.getElementsByTagName("test-method");

            for (int j = 0; j < methods.getLength(); j++) {

                Element method = (Element) methods.item(j);

                if (Boolean.parseBoolean(
                        method.getAttribute("is-config"))) {
                    continue;
                }

                boolean isIgnored = Boolean.parseBoolean(
                        method.getAttribute("is-ignored")
                );

                String status = method.getAttribute("status")
                        .toUpperCase();

                if (isIgnored) {
                    counts.ignored++;
                    continue;
                }

                switch (status) {

                    case "PASS" ->
                            counts.passed++;

                    case "FAIL",
                         "SUCCESS_PERCENTAGE_FAILURE",
                         "CONFUSED" ->
                            counts.failed++;

                    case "SKIP" ->
                            counts.skipped++;

                    default -> throw new IllegalStateException(
                            "Unexpected TestNG status: " + status
                                    + " in " + className
                    );
                }
            }
        }

        // Account for ignored methods reported only at the root level.
        Element root = document.getDocumentElement();

        long rootIgnored = parseLong(
                root.getAttribute("ignored")
        );

        long recordedIgnored = PLATFORMS.values().stream()
                .mapToLong(counts -> counts.ignored)
                .sum();

        if (rootIgnored > recordedIgnored) {
            PLATFORMS.get("Unclassified").ignored +=
                    rootIgnored - recordedIgnored;
        }
    }

    private static Counts getPlatformCounts(String className) {

        if (className.startsWith("com.qa.tests.web.")
                || className.startsWith("com.qa.tests.ui.")) {
            return PLATFORMS.get("Web");
        }

        if (className.startsWith("com.qa.tests.api.")) {
            return PLATFORMS.get("API");
        }

        if (className.startsWith("com.qa.tests.mobile.")) {
            return PLATFORMS.get("Mobile");
        }

        return PLATFORMS.get("Unclassified");
    }

    private static void appendPlatformRow(
            StringBuilder rows,
            String name,
            Counts counts,
            String folder) {

        rows.append("<tr>")
                .append("<td>").append(name).append("</td>")
                .append("<td>").append(counts.total()).append("</td>")
                .append("<td>").append(counts.passed).append("</td>")
                .append("<td>").append(counts.failed).append("</td>")
                .append("<td>").append(counts.skipped).append("</td>")
                .append("<td>").append(counts.ignored).append("</td>")
                .append("<td>")
                .append(String.format("%.2f%%", counts.passRate()))
                .append("</td>");

        if (folder != null) {

            Path allure = REPORTS.resolve(folder)
                    .resolve("allure-report")
                    .resolve("index.html");

            if (Files.isRegularFile(allure)) {

                rows.append("<td><a href=\"")
                        .append(folder)
                        .append("/allure-report/index.html\">")
                        .append("Open Allure Report</a></td>");

            } else {

                rows.append("<td>Report not generated</td>");
            }

        } else {

            rows.append("<td>—</td>");
        }

        rows.append("</tr>");
    }

    private static void writeSuiteSummary(
            long total,
            long passed,
            long failed,
            long skipped,
            long ignored,
            double passRate) throws Exception {

        Map<String, Object> summary = new LinkedHashMap<>();

        summary.put("generatedAtUtc", Instant.now().toString());
        summary.put("source", TESTNG_RESULTS.toString());
        summary.put("totalTests", total);
        summary.put("passed", passed);
        summary.put("failed", failed);
        summary.put("skipped", skipped);
        summary.put("ignored", ignored);
        summary.put("passRatePercent", passRate);

        Map<String, Object> platformResults = new LinkedHashMap<>();

        PLATFORMS.forEach((name, counts) -> {

            if (counts.total() > 0) {

                Map<String, Object> item = new LinkedHashMap<>();

                item.put("totalTests", counts.total());
                item.put("passed", counts.passed);
                item.put("failed", counts.failed);
                item.put("skipped", counts.skipped);
                item.put("ignored", counts.ignored);
                item.put("passRatePercent", counts.passRate());

                platformResults.put(name, item);
            }
        });

        summary.put("platforms", platformResults);

        Files.writeString(
                REPORTS.resolve("suite-summary.json"),
                GSON.toJson(summary),
                StandardCharsets.UTF_8
        );
    }

    private static void printPlatformCounts(
            String platform,
            Counts counts) {

        System.out.printf(
                "%-13s Total: %d | Passed: %d | Failed: %d "
                        + "| Skipped: %d | Ignored: %d%n",
                platform,
                counts.total(),
                counts.passed,
                counts.failed,
                counts.skipped,
                counts.ignored
        );
    }

    private static long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception ignored) {
            return 0;
        }
    }

    private static class Counts {

        long passed;
        long failed;
        long skipped;
        long ignored;

        long total() {
            return passed + failed + skipped + ignored;
        }

        double passRate() {

            long executed = passed + failed;

            return executed == 0
                    ? 0.0
                    : Math.round(
                    passed * 10000.0 / executed
            ) / 100.0;
        }

        void reset() {
            passed = 0;
            failed = 0;
            skipped = 0;
            ignored = 0;
        }
    }
}
