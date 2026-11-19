package com.qa.framework.mobile.driver;

import com.qa.framework.config.ConfigManager;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;

import java.io.File;
import java.net.URI;
import java.time.Duration;

public class NativeAndroidDriverFactory {

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

        File app = new File(
                "src/test/resources/apps/ApiDemos-debug.apk"
        );

        if (!app.exists()) {
            throw new IllegalStateException(
                    "ApiDemos APK not found: " + app.getAbsolutePath()
            );
        }

        options.setApp(app.getAbsolutePath());
        options.setNewCommandTimeout(Duration.ofSeconds(60));
        options.setCapability("appium:adbExecTimeout", 60000);

        String serverUrl =
                ConfigManager.getProperty("mobile.appiumServerUrl");

        return new AndroidDriver(
                URI.create(serverUrl).toURL(),
                options
        );
    }
}