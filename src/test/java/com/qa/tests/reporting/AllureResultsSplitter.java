package com.qa.tests.reporting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Splits combined Allure results into independent Web, API and Mobile
 * result directories.
 *
 * Framework/support tests that do not belong to these platforms are
 * excluded from platform-specific reports.
 *
 * The original target/allure-results directory is never modified.
 */
public class AllureResultsSplitter {

    private static final Logger LOGGER =
            Logger.getLogger(AllureResultsSplitter.class.getName());

    private static final Path PROJECT_DIR = Path.of("").toAbsolutePath();

    private static final Path SOURCE_DIR =
            PROJECT_DIR.resolve("target/allure-results");

    private static final Path REPORTS_DIR =
            PROJECT_DIR.resolve("target/reports");

    private static final Gson GSON =
            new GsonBuilder().setPrettyPrinting().create();

    private enum Platform {
        WEB("web"),
        API("api"),
        MOBILE("mobile");

        private final String folder;

        Platform(String folder) {
            this.folder = folder;
        }

        public String folder() {
            return folder;
        }

        public Path resultsDirectory() {
            return REPORTS_DIR.resolve(folder)
                    .resolve("allure-results");
        }
    }

    private final Map<Platform, Set<String>> platformTestIds =
            new LinkedHashMap<>();

    private final Map<Platform, List<Path>> platformResultFiles =
            new LinkedHashMap<>();

    private final Map<String, JsonObject> containers =
            new LinkedHashMap<>();

    private final Set<String> copiedAttachments =
            new HashSet<>();

    private int skippedNonPlatformTests = 0;

    public static void main(String[] args) {
        try {
            new AllureResultsSplitter().split();
        } catch (Exception e) {
            LOGGER.severe(
                    "Allure results splitting failed: " + e.getMessage()
            );
            throw new RuntimeException(
                    "Unable to split Allure results by platform.", e
            );
        }
    }

    public void split() throws IOException {

        LOGGER.info("Starting Allure results splitting.");

        validateSourceDirectory();

        initializePlatformCollections();

        cleanPlatformDirectories();

        loadContainers();

        classifyTestResults();

        copyPlatformResults();

        copySharedMetadata();

        printSummary();

        LOGGER.info("Allure results splitting completed.");
    }

    private void validateSourceDirectory() {

        if (!Files.exists(SOURCE_DIR)
                || !Files.isDirectory(SOURCE_DIR)) {

            throw new IllegalStateException(
                    "Combined Allure results directory not found: "
                            + SOURCE_DIR
            );
        }
    }

    private void initializePlatformCollections() {

        for (Platform platform : Platform.values()) {

            platformTestIds.put(platform, new HashSet<>());

            platformResultFiles.put(platform, new ArrayList<>());
        }
    }

    /**
     * Clears only the generated platform-specific result directories.
     * The original combined results remain untouched.
     */
    private void cleanPlatformDirectories() throws IOException {

        for (Platform platform : Platform.values()) {

            Path directory = platform.resultsDirectory();

            deleteDirectory(directory);

            Files.createDirectories(directory);

            LOGGER.info(
                    "Prepared platform results directory: " + directory
            );
        }
    }

    private void deleteDirectory(Path directory) throws IOException {

        if (!Files.exists(directory)) {
            return;
        }

        try (Stream<Path> paths = Files.walk(directory)) {

            List<Path> allPaths = paths
                    .sorted((a, b) -> b.compareTo(a))
                    .toList();

            for (Path path : allPaths) {
                Files.deleteIfExists(path);
            }
        }
    }

    /**
     * Loads Allure fixture containers.
     *
     * Containers are associated with platform tests through their
     * children UUIDs.
     */
    private void loadContainers() throws IOException {

        try (Stream<Path> files = Files.list(SOURCE_DIR)) {

            List<Path> containerFiles = files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName()
                            .toString().endsWith("-container.json"))
                    .toList();

            for (Path file : containerFiles) {

                JsonObject container = readJson(file);

                String containerId = removeSuffix(
                        file.getFileName().toString(),
                        "-container.json"
                );

                containers.put(containerId, container);
            }
        }

        LOGGER.info(
                "Loaded " + containers.size()
                        + " Allure fixture containers."
        );
    }

    /**
     * Classifies platform test results using Allure package/testClass
     * labels.
     *
     * Unrecognised test packages, such as com.qa.tests.ai, are
     * intentionally excluded from platform-specific reports.
     * Their original files remain untouched in SOURCE_DIR.
     */
    private void classifyTestResults() throws IOException {

        try (Stream<Path> files = Files.list(SOURCE_DIR)) {

            List<Path> resultFiles = files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName()
                            .toString().endsWith("-result.json"))
                    .toList();

            for (Path file : resultFiles) {

                JsonObject result = readJson(file);

                Platform platform = identifyPlatform(result);

                if (platform == null) {

                    skippedNonPlatformTests++;

                    LOGGER.warning(
                            "Skipping non-platform Allure test result: "
                                    + file.getFileName()
                                    + " | Full name: "
                                    + getString(result, "fullName")
                    );

                    continue;
                }

                String testId = removeSuffix(
                        file.getFileName().toString(),
                        "-result.json"
                );

                platformTestIds.get(platform).add(testId);

                platformResultFiles.get(platform).add(file);
            }
        }

        int total = platformTestIds.values()
                .stream()
                .mapToInt(Set::size)
                .sum();

        if (total == 0) {

            throw new IllegalStateException(
                    "No Web, API or Mobile Allure test results were "
                            + "identified in " + SOURCE_DIR
            );
        }

        LOGGER.info(
                "Platform classification completed. "
                        + "Classified: " + total
                        + ", skipped non-platform: "
                        + skippedNonPlatformTests
        );
    }

    /**
     * Determines platform from Allure labels.
     */
    private Platform identifyPlatform(JsonObject result) {

        if (!result.has("labels")
                || !result.get("labels").isJsonArray()) {

            return null;
        }

        JsonArray labels = result.getAsJsonArray("labels");

        for (JsonElement element : labels) {

            if (!element.isJsonObject()) {
                continue;
            }

            JsonObject label = element.getAsJsonObject();

            String labelName = getString(label, "name");
            String labelValue = getString(label, "value");

            if (labelValue == null) {
                continue;
            }

            if ("package".equals(labelName)
                    || "testClass".equals(labelName)) {

                Platform platform = identifyFromClassName(labelValue);

                if (platform != null) {
                    return platform;
                }
            }
        }

        return null;
    }

    private Platform identifyFromClassName(String className) {

        if (className.startsWith("com.qa.tests.web.")
                || className.startsWith("com.qa.tests.ui.")) {
            return Platform.WEB;
        }

        if (className.startsWith("com.qa.tests.api.")) {
            return Platform.API;
        }

        if (className.startsWith("com.qa.tests.mobile.")) {
            return Platform.MOBILE;
        }

        return null;
    }

    /**
     * Copies each platform's test result JSON and referenced attachments.
     */
    private void copyPlatformResults() throws IOException {

        for (Platform platform : Platform.values()) {

            LOGGER.info(
                    "Copying " + platform.folder() + " test results."
            );

            Set<String> testIds = platformTestIds.get(platform);

            for (Path resultFile : platformResultFiles.get(platform)) {

                JsonObject result = readJson(resultFile);

                // Only Web needs suite-specific test identities.
                if (platform == Platform.WEB) {
                    applySuiteSpecificHistoryId(result);
                }

                Path destination = platform.resultsDirectory()
                        .resolve(resultFile.getFileName());

                // Write the modified copy; never modify the original.
                Files.writeString(
                        destination,
                        GSON.toJson(result),
                        StandardCharsets.UTF_8
                );

                copyReferencedAttachments(
                        result,
                        platform.resultsDirectory()
                );
            }

            copyMatchingContainers(platform, testIds);
        }
    }

    /**
     * Copies only containers linked to tests belonging to this platform.
     * Removes children belonging to other platforms.
     */
    private void copyMatchingContainers(
            Platform platform,
            Set<String> platformIds
    ) throws IOException {

        for (Map.Entry<String, JsonObject> entry
                : containers.entrySet()) {

            String containerId = entry.getKey();

            JsonObject container = entry.getValue();

            if (!container.has("children")
                    || !container.get("children").isJsonArray()) {

                continue;
            }

            JsonArray originalChildren =
                    container.getAsJsonArray("children");

            JsonArray filteredChildren = new JsonArray();

            boolean hasMatchingChild = false;

            for (JsonElement child : originalChildren) {

                if (!child.isJsonPrimitive()) {
                    continue;
                }

                String childId = child.getAsString();

                if (platformIds.contains(childId)) {

                    filteredChildren.add(childId);

                    hasMatchingChild = true;
                }
            }

            if (!hasMatchingChild) {
                continue;
            }

            JsonObject filteredContainer =
                    container.deepCopy();

            filteredContainer.add(
                    "children",
                    filteredChildren
            );

            Path destination = platform.resultsDirectory()
                    .resolve(containerId + "-container.json");

            Files.writeString(
                    destination,
                    GSON.toJson(filteredContainer)
            );

            copyReferencedAttachments(
                    filteredContainer,
                    platform.resultsDirectory()
            );
        }
    }

    /**
     * Recursively discovers attachment source references in test,
     * step and fixture JSON structures.
     */
    private void copyReferencedAttachments(
            JsonElement element,
            Path destinationDirectory
    ) throws IOException {

        if (element == null || element.isJsonNull()) {
            return;
        }

        if (element.isJsonArray()) {

            for (JsonElement child : element.getAsJsonArray()) {
                copyReferencedAttachments(
                        child,
                        destinationDirectory
                );
            }

            return;
        }

        if (!element.isJsonObject()) {
            return;
        }

        JsonObject object = element.getAsJsonObject();

        if (object.has("source")
                && object.get("source").isJsonPrimitive()
                && object.has("name")) {

            String source = object.get("source").getAsString();

            copyAttachment(source, destinationDirectory);
        }

        for (Map.Entry<String, JsonElement> entry
                : object.entrySet()) {

            copyReferencedAttachments(
                    entry.getValue(),
                    destinationDirectory
            );
        }
    }

    /**
     * Copies an attachment while preventing paths outside the source
     * Allure results directory.
     */
    private void copyAttachment(
            String attachmentSource,
            Path destinationDirectory
    ) throws IOException {

        if (attachmentSource == null
                || attachmentSource.isBlank()) {

            return;
        }

        Path source = SOURCE_DIR.resolve(attachmentSource)
                .normalize();

        if (!source.startsWith(SOURCE_DIR.normalize())) {

            throw new IOException(
                    "Invalid attachment path: " + attachmentSource
            );
        }

        if (!Files.exists(source)
                || !Files.isRegularFile(source)) {

            throw new IOException(
                    "Referenced Allure attachment is missing: "
                            + attachmentSource
            );
        }

        Path destination = destinationDirectory
                .resolve(attachmentSource)
                .normalize();

        if (!destination.startsWith(
                destinationDirectory.normalize())) {

            throw new IOException(
                    "Invalid attachment destination: "
                            + attachmentSource
            );
        }

        String uniqueKey =
                destinationDirectory.toAbsolutePath()
                        + "|" + attachmentSource;

        if (!copiedAttachments.add(uniqueKey)) {
            return;
        }

        if (destination.getParent() != null) {
            Files.createDirectories(destination.getParent());
        }

        Files.copy(
                source,
                destination,
                StandardCopyOption.REPLACE_EXISTING
        );
    }

    /**
     * Copies shared Allure metadata, but not unrelated test results
     * or unreferenced attachments.
     */
    private void copySharedMetadata() throws IOException {

        List<String> metadataFiles = List.of(
                "categories.json",
                "environment.properties",
                "environment.xml",
                "executor.json"
        );

        for (Platform platform : Platform.values()) {

            Path destination = platform.resultsDirectory();

            for (String metadataFile : metadataFiles) {

                Path source = SOURCE_DIR.resolve(metadataFile);

                if (Files.isRegularFile(source)) {

                    Files.copy(
                            source,
                            destination.resolve(metadataFile),
                            StandardCopyOption.REPLACE_EXISTING
                    );
                }
            }

            Path history = SOURCE_DIR.resolve("history");

            if (Files.isDirectory(history)) {

                copyDirectory(
                        history,
                        destination.resolve("history")
                );
            }
        }
    }

    private void copyDirectory(
            Path source,
            Path destination
    ) throws IOException {

        try (Stream<Path> paths = Files.walk(source)) {

            for (Path path : paths.toList()) {

                Path relative = source.relativize(path);

                Path target = destination.resolve(relative);

                if (Files.isDirectory(path)) {

                    Files.createDirectories(target);

                } else {

                    Files.createDirectories(target.getParent());

                    Files.copy(
                            path,
                            target,
                            StandardCopyOption.REPLACE_EXISTING
                    );
                }
            }
        }
    }

    private JsonObject readJson(Path file) throws IOException {

        try {

            JsonElement element = JsonParser.parseString(
                    Files.readString(file)
            );

            if (!element.isJsonObject()) {

                throw new IOException(
                        "Invalid Allure JSON object: " + file
                );
            }

            return element.getAsJsonObject();

        } catch (RuntimeException e) {

            throw new IOException(
                    "Unable to parse Allure JSON file: " + file,
                    e
            );
        }
    }

    private String getString(
            JsonObject object,
            String property
    ) {

        if (!object.has(property)
                || object.get(property).isJsonNull()) {

            return null;
        }

        return object.get(property).getAsString();
    }

    private String removeSuffix(
            String filename,
            String suffix
    ) {

        return filename.substring(
                0,
                filename.length() - suffix.length()
        );
    }

    private void printSummary() {

        LOGGER.info("========== Allure Split Summary ==========");

        for (Platform platform : Platform.values()) {

            int count = platformTestIds.get(platform).size();

            LOGGER.info(
                    platform.folder().toUpperCase()
                            + " tests: " + count
                            + " | Results: "
                            + platform.resultsDirectory()
            );
        }

        LOGGER.info(
                "Total classified platform tests: "
                        + platformTestIds.values()
                        .stream()
                        .mapToInt(Set::size)
                        .sum()
        );

        LOGGER.info(
                "Non-platform tests skipped: "
                        + skippedNonPlatformTests
        );

        LOGGER.info(
                "Original combined results preserved at: "
                        + SOURCE_DIR
        );

        LOGGER.info("==========================================");
    }

    /**
     * Makes intentional executions in different TestNG suites
     * independent in Allure reporting.
     *
     * Same test + same parameters + same suite:
     *     Same historyId (real retries remain grouped).
     *
     * Same test + same parameters + different suite:
     *     Different historyId (independent executions).
     *
     * This modifies only the platform-specific copied JSON.
     */
    private void applySuiteSpecificHistoryId(JsonObject result) {

        String originalHistoryId = getString(result, "historyId");
        String fullName = getString(result, "fullName");
        String suiteName = getLabelValue(result, "suite");

        if (suiteName == null || suiteName.isBlank()) {
            throw new IllegalStateException(
                    "Missing suite label for: " + fullName
            );
        }

        if (originalHistoryId == null || originalHistoryId.isBlank()) {
            throw new IllegalStateException(
                    "Missing historyId for: " + fullName
            );
        }

        // Keep genuine retries together within the same suite.
        result.addProperty(
                "historyId",
                generateStableHistoryId(
                        originalHistoryId,
                        suiteName
                )
        );

        // Distinguish independent suite executions and parameter sets.
        String parameters = result.has("parameters")
                ? result.get("parameters").toString()
                : "";

        String testIdentity =
                fullName + "|" + suiteName + "|" + parameters;

        result.addProperty(
                "testCaseId",
                generateStableHistoryId(testIdentity, "test-case")
        );
    }

    /**
     * Retrieves a specific Allure label value.
     */
    private String getLabelValue(
            JsonObject result,
            String requiredLabel
    ) {

        if (!result.has("labels")
                || !result.get("labels").isJsonArray()) {

            return null;
        }

        for (JsonElement element : result.getAsJsonArray("labels")) {

            if (!element.isJsonObject()) {
                continue;
            }

            JsonObject label = element.getAsJsonObject();

            String name = getString(label, "name");
            String value = getString(label, "value");

            if (requiredLabel.equals(name)) {
                return value;
            }
        }

        return null;
    }

    /**
     * Generates a deterministic SHA-256 identity from the original
     * test history ID and its TestNG suite name.
     */
    private String generateStableHistoryId(
            String originalHistoryId,
            String suiteName
    ) {

        try {

            String identity =
                    originalHistoryId + "|" + suiteName;

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    identity.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException e) {

            throw new IllegalStateException(
                    "SHA-256 algorithm is unavailable.", e
            );
        }
    }
}
