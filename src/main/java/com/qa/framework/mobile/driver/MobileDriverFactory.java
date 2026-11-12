
package com.qa.framework.mobile.driver;

import com.qa.framework.config.ConfigManager;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;

import java.net.URI;
import java.time.Duration;

public class MobileDriverFactory {

    public static AndroidDriver createDriver() throws Exception {

        UiAutomator2Options options = new UiAutomator2Options();

        options.setPlatformName(
                ConfigManager.getProperty("mobile.platform")
        );

        options.setAutomationName(
                ConfigManager.getProperty("mobile.automationName")
        );

        options.setDeviceName(
                ConfigManager.getProperty("mobile.deviceName")
        );

        options.setUdid(
                ConfigManager.getProperty("mobile.udid")
        );

        options.setCapability(
                "browserName",
                ConfigManager.getProperty("mobile.browserName")
        );

        options.setNewCommandTimeout(Duration.ofSeconds(60));

        String serverUrl =
                ConfigManager.getProperty("mobile.appiumServerUrl");

        return new AndroidDriver(
                URI.create(serverUrl).toURL(),
                options
        );
    }
}
