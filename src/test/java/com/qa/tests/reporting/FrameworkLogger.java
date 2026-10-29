
package com.qa.tests.reporting;

import java.util.logging.Logger;

public final class FrameworkLogger {

    private FrameworkLogger() {
        // Prevent object creation.
    }

    public static Logger getLogger(Class<?> clazz) {
        return Logger.getLogger(clazz.getName());
    }
}
