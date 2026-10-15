package com.example.ganpativargani.whatsapp;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** Pre-flight checks before submitting a template to Meta for review. */
public final class MetaTemplateValidator {

    private static final Pattern CURRENCY_BEFORE_VAR =
            Pattern.compile("[₹$€£]\\s*\\{\\{\\s*\\w+\\s*\\}\\}");
    private static final Pattern CAMEL_CASE_VAR =
            Pattern.compile("\\{\\{\\s*[a-z]+[A-Z]\\w*\\s*\\}\\}");

    private MetaTemplateValidator() {
    }

    public static List<String> validateMessage(String message) {
        List<String> issues = new ArrayList<>();
        if (message == null || message.isBlank()) {
            issues.add("Message text is required.");
            return issues;
        }
        if (CURRENCY_BEFORE_VAR.matcher(message).find()) {
            issues.add("Do not put ₹, $, or € directly before a variable. Use \"Rs. {{amount}}\" instead of \"₹{{amount}}\".");
        }
        if (CAMEL_CASE_VAR.matcher(message).find()) {
            issues.add("Use lowercase snake_case variables for Meta (e.g. {{organization_name}}, not {{organizationName}}).");
        }
        for (String variable : MetaTemplateBodyBuilder.extractVariablesInOrder(message)) {
            if (!variable.matches("[a-z][a-z0-9_]*")) {
                issues.add("Variable {{" + variable + "}} is invalid. Meta only allows lowercase letters, numbers, and underscores.");
            }
        }
        return issues;
    }

    public static String nextResubmitName(String currentMetaName) {
        String base = currentMetaName.replaceAll("_v\\d+$", "");
        var versionMatcher = Pattern.compile("_v(\\d+)$").matcher(currentMetaName);
        int next = 2;
        if (versionMatcher.find()) {
            next = Integer.parseInt(versionMatcher.group(1)) + 1;
        }
        return base + "_v" + next;
    }
}
