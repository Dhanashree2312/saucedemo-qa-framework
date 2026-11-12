package com.qa.framework.cloud;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.core.sync.ResponseTransformer;

import java.nio.file.Path;

public class S3FileDownloadCheck {

    public static void main(String[] args) {

        String bucketName = "saucedemo-qa-framework-uniqueid";
        String objectKey = "test-data/ui/login-users.csv";

        Path destinationPath = Path.of(
                "C:/qa-cloud-test-data/downloaded-login-users-java.csv"
        );

        try (S3Client s3Client = S3Client.builder()
                .region(Region.EU_NORTH_1)
                .credentialsProvider(
                        ProfileCredentialsProvider.create("saucedemo"))
                .build()) {

            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            s3Client.getObject(
                    request,
                    ResponseTransformer.toFile(destinationPath)
            );

            System.out.println("File downloaded successfully!");
            System.out.println("Saved at: " + destinationPath);

        }
    }
}