package com.qa.tests.api;

import com.qa.framework.api.APIClient;
import com.qa.framework.api.AuthApiClient;
import com.qa.framework.api.ProductApiClient;
import com.qa.framework.api.UserApiClient;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class BaseApiTest {

    protected APIClient apiClient;
    protected ProductApiClient productApiClient;
    protected UserApiClient userApiClient;
    protected AuthApiClient authApiClient;

    @BeforeMethod
    public void setUpApi() {

        apiClient = new APIClient();

        productApiClient = new ProductApiClient(apiClient);
        userApiClient = new UserApiClient(apiClient);
        authApiClient = new AuthApiClient(apiClient);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDownApi() {

        if (apiClient != null) {
            apiClient.close();
            apiClient = null;
        }
    }
}