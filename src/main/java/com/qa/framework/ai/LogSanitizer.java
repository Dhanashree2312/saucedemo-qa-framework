
package com.qa.framework.ai;

import java.util.regex.Pattern;

public class LogSanitizer {

    private static final String REDACTED = "[REDACTED]";

    // Passwords, API keys, tokens and secrets in key=value formats.
    private static final Pattern KEY_VALUE_SECRET =
            Pattern.compile(
                    "(?i)(password|passwd|pwd|api[_-]?key|"
                            + "access[_-]?token|refresh[_-]?token|"
                            + "client[_-]?secret|secret[_-]?key|"
                            + "aws_secret_access_key|"
                            + "private[_-]?key)"
                            + "(\\s*[=:]\\s*)"
                            + "(\"[^\"]*\"|'[^']*'|[^\\s,;&]+)"
            );

    // Authorization headers containing Bearer or Basic credentials.
    private static final Pattern AUTHORIZATION =
            Pattern.compile(
                    "(?i)(authorization\\s*:\\s*"
                            + "(?:bearer|basic)\\s+)[^\\s,;]+"
            );

    // JWT tokens.
    private static final Pattern JWT =
            Pattern.compile(
                    "\\beyJ[a-zA-Z0-9_-]+\\."
                            + "[a-zA-Z0-9_-]+\\."
                            + "[a-zA-Z0-9_-]+\\b"
            );

    // AWS access key IDs.
    private static final Pattern AWS_ACCESS_KEY =
            Pattern.compile("\\bAKIA[0-9A-Z]{16}\\b");

    public String sanitize(String input) {

        if (input == null || input.isBlank()) {
            return input;
        }

        String sanitized = input;

        sanitized = KEY_VALUE_SECRET.matcher(sanitized)
                .replaceAll("$1$2" + REDACTED);

        sanitized = AUTHORIZATION.matcher(sanitized)
                .replaceAll("$1" + REDACTED);

        sanitized = JWT.matcher(sanitized)
                .replaceAll(REDACTED);

        sanitized = AWS_ACCESS_KEY.matcher(sanitized)
                .replaceAll(REDACTED);

        return sanitized;
    }
}
