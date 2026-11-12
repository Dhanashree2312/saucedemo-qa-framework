
package com.qa.tests.ui;

import com.qa.framework.data.TestDataManager;
import org.testng.annotations.DataProvider;

import java.util.List;

public class LoginDataProvider {

    @DataProvider(name = "loginData")
    public static Object[][] loginData() {

        List<String[]> testData = TestDataManager.getLoginTestData();

        Object[][] data = new Object[testData.size()][4];

        for (int i = 0; i < testData.size(); i++) {
            data[i] = testData.get(i);
        }

        return data;
    }
}
