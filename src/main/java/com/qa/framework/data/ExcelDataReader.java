
package com.qa.framework.data;

import org.apache.poi.ss.usermodel.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExcelDataReader {

    public static List<String[]> readExcelData(
            String filePath,
            String sheetName
    ) throws IOException {

        List<String[]> testData = new ArrayList<>();
        /*
        try-with-resources.
         */
        try (FileInputStream file = new FileInputStream(filePath);
             Workbook workbook = WorkbookFactory.create(file)) {
        /*
        Apache POI provides WorkbookFactory to create a Workbook object from an Excel file or input stream.
        It also detects the Excel format, so it can work with supported Excel formats such as .xls and .xlsx.
        We could create a workbook directly using XSSFWorkbook, but WorkbookFactory is convenient when we want to support both formats.
         */
            Sheet sheet = workbook.getSheet(sheetName);

            if (sheet == null) {

                List<String> availableSheets = new ArrayList<>();

                for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                    availableSheets.add(workbook.getSheetName(i));
                }

                throw new IllegalArgumentException(
                        "Sheet not found: " + sheetName
                );
            }
            /*
            DataFormatter converts the displayed cell value into a String.
            It is particularly useful when you don't want to worry about whether Excel internally stores a value as a number or text.
             */
            DataFormatter formatter = new DataFormatter();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);

                if (row == null) {
                    continue;
                }

                String[] data = new String[4];

                for (int j = 0; j < 4; j++) {
                    data[j] = formatter.formatCellValue(
                            row.getCell(j)
                    );
                }

                testData.add(data);
            }
        }

        return testData;
    }
}
