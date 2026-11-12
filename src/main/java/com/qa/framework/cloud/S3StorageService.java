
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

    // Download one file from S3
    public static Path downloadFile(String objectKey, Path destination)
            throws IOException {

        if (destination.getParent() != null) {
            Files.createDirectories(destination.getParent());
        }

        // Remove an existing file before downloading
        Files.deleteIfExists(destination);

        try (S3Client s3Client = createClient()) {

            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(objectKey)
                    .build();

            s3Client.getObject(
                    request,
                    ResponseTransformer.toFile(destination)
            );
        }

        System.out.println("Downloaded from S3: " + destination);
        return destination;
    }

    // Upload one file to S3
    public static void uploadFile(Path file, String objectKey) {

        try (S3Client s3Client = createClient()) {

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(objectKey)
                    .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromFile(file)
            );
        }

        System.out.println("Uploaded to S3: " + objectKey);
    }

    // Upload all files inside a directory
    public static void uploadDirectory(Path directory, String s3Prefix)
            throws IOException {

        if (!Files.exists(directory)) {
            throw new IOException("Directory not found: " + directory);
        }

        try (Stream<Path> files = Files.walk(directory)) {

            files.filter(Files::isRegularFile)
                    .forEach(file -> {

                        String relativePath = directory
                                .relativize(file)
                                .toString()
                                .replace("\\", "/");

                        String objectKey = s3Prefix + "/" + relativePath;

                        uploadFile(file, objectKey);
                    });
        }
    }
}
