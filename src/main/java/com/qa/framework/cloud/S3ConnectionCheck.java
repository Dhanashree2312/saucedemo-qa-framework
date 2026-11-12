package com.qa.framework.cloud;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;

/*Connect our Java application to AWS S3, authenticate using our AWS credentials, and list the files stored in our bucket.*/

public class S3ConnectionCheck {

    public static void main(String[] args) {

        String bucketName = "saucedemo-qa-framework-uniqueid";

        try (S3Client s3Client = S3Client.builder()
                .region(Region.EU_NORTH_1)
                .credentialsProvider(
                        ProfileCredentialsProvider.create("saucedemo"))
                .build()) {

            ListObjectsV2Response response = s3Client.listObjectsV2(
                    ListObjectsV2Request.builder()
                            .bucket(bucketName)
                            .build()
            );

            System.out.println("Successfully connected to S3!");

            response.contents().forEach(object ->
                    System.out.println("File: " + object.key()));

        }
    }
}