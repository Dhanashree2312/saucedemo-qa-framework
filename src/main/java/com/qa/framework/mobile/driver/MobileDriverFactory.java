
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

        // Maximum time Appium waits for commands.
        options.setNewCommandTimeout(Duration.ofSeconds(60));

        // Maximum time allowed for ADB commands.
        options.setCapability("appium:adbExecTimeout", 60000);

        // Maximum time allowed to install the UiAutomator2 server APK.
        options.setCapability(
                "appium:uiautomator2ServerInstallTimeout",
                90000
        );

        String serverUrl =
                ConfigManager.getProperty("mobile.appiumServerUrl");

        System.out.println("Starting Android browser session...");
        System.out.println("UiAutomator2 server installation timeout: 90 seconds");

        return new AndroidDriver(
                URI.create(serverUrl).toURL(),
                options
        );
    }
}
