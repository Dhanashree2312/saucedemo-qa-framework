
package com.qa.framework.data;

import com.qa.framework.cloud.S3StorageService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class TestDataManager {

    private static final String LOCAL_FILE =
            "src/test/resources/test-data/web/web-test-data.xlsx";

    private static final String S3_KEY =
            "test-data/web/web-test-data.xlsx";

    private static List<String[]> loginTestData;

    public static synchronized List<String[]> getLoginTestData() {

        // Reuse data if it has already been loaded
        if (loginTestData != null) {
            return loginTestData;
        }

        String source = System.getProperty("testdata.source", "local");

        try {
            Path excelFile;

            if (source.equalsIgnoreCase("s3")) {

                System.out.println("Loading test data from AWS S3...");

                Path destination =
                        Path.of("target/cloud-test-data/web-test-data.xlsx");

                Files.createDirectories(destination.getParent());

                // Remove the previous downloaded file, if present
                Files.deleteIfExists(destination);

                excelFile = S3StorageService.downloadFile(
                        S3_KEY,
                        destination
                );

                System.out.println("S3 test data downloaded to: " + excelFile);

            } else {

                System.out.println("Loading test data from local file...");

                excelFile = Path.of(LOCAL_FILE);
            }

            loginTestData = ExcelDataReader.readExcelData(
                    excelFile.toString(),
                    "Login"
            );

            System.out.println(
                    "Total test data rows loaded: " + loginTestData.size()
            );

            return loginTestData;

        } catch (IOException e) {

            throw new RuntimeException(
                    "Unable to load login test data",
                    e
            );
        }
    }
}
