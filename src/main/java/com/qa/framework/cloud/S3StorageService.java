
package com.qa.framework.cloud;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.core.sync.RequestBody;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class S3StorageService {

    private static final String BUCKET_NAME =
            "saucedemo-qa-framework-uniqueid";

    private static final Region REGION = Region.EU_NORTH_1;

    private static S3Client createClient() {
        return S3Client.builder()
                .region(REGION)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    public static Path downloadFile(String objectKey, Path destination)
            throws IOException {

        if (destination.getParent() != null) {
            Files.createDirectories(destination.getParent());
        }

        Files.deleteIfExists(destination);

        try (S3Client client = createClient()) {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(objectKey)
                    .build();

            client.getObject(request, ResponseTransformer.toFile(destination));
        }

        return destination;
    }

    public static void uploadFile(Path file, String objectKey) {
        try (S3Client client = createClient()) {
            uploadFile(client, file, objectKey);
        }
    }

    private static void uploadFile(
            S3Client client, Path file, String objectKey) {

        String contentType = getContentType(file);

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(objectKey)
                .contentType(contentType)
                .build();

        client.putObject(request, RequestBody.fromFile(file));

        System.out.println("Uploaded: " + objectKey);
    }

    public static void uploadDirectory(Path directory, String s3Prefix)
            throws IOException {

        if (!Files.isDirectory(directory)) {
            throw new IOException("Directory not found: " + directory);
        }

        try (S3Client client = createClient();
             Stream<Path> files = Files.walk(directory)) {

            files.filter(Files::isRegularFile)
                    .forEach(file -> {
                        String relativePath = directory.relativize(file)
                                .toString()
                                .replace("\\", "/");

                        String objectKey = s3Prefix + "/" + relativePath;

                        uploadFile(client, file, objectKey);
                    });
        }
    }

    private static String getContentType(Path file) {
        String name = file.getFileName().toString().toLowerCase();

        if (name.endsWith(".html")) return "text/html";
        if (name.endsWith(".js")) return "application/javascript";
        if (name.endsWith(".css")) return "text/css";
        if (name.endsWith(".json")) return "application/json";
        if (name.endsWith(".xml")) return "application/xml";
        if (name.endsWith(".png")) return "image/png";
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
        if (name.endsWith(".svg")) return "image/svg+xml";
        if (name.endsWith(".gif")) return "image/gif";
        if (name.endsWith(".webp")) return "image/webp";
        if (name.endsWith(".txt") || name.endsWith(".log")) return "text/plain";
        if (name.endsWith(".woff")) return "font/woff";
        if (name.endsWith(".woff2")) return "font/woff2";
        if (name.endsWith(".ttf")) return "font/ttf";

        try {
            String detected = Files.probeContentType(file);
            return detected != null ? detected : "application/octet-stream";
        } catch (IOException e) {
            return "application/octet-stream";
        }
    }
}
