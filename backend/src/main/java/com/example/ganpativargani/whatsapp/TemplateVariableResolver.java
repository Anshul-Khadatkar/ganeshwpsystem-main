package com.example.ganpativargani.whatsapp;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves {@code {{variableName}}} placeholders in a template string.
 * Missing/null variables (e.g. a contributor with no flat number) resolve
 * to "—" rather than the literal string "null", per the spec's explicit
 * requirement never to render "Flat: null".
 */
public final class TemplateVariableResolver {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{\\s*(\\w+)\\s*\\}\\}");
    private static final String MISSING_VALUE_PLACEHOLDER = "—";

    private TemplateVariableResolver() {
    }

    public static String resolve(String template, Map<String, String> variables) {
        if (template == null) {
            return "";
        }
        Matcher matcher = PLACEHOLDER.matcher(template);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = variables.get(key);
            matcher.appendReplacement(result, Matcher.quoteReplacement(
                    value != null && !value.isBlank() ? value : MISSING_VALUE_PLACEHOLDER));
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
