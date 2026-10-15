package com.example.ganpativargani.whatsapp;

import com.example.ganpativargani.entity.MetaParameterFormat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Converts app-level {{variable}} messages into Meta template API payloads. */
public final class MetaTemplateBodyBuilder {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{\\s*(\\w+)\\s*\\}\\}");

    private MetaTemplateBodyBuilder() {
    }

    public static String toMetaApiName(String displayName) {
        String slug = displayName.trim().toLowerCase()
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        if (slug.isBlank()) {
            throw new IllegalArgumentException("Template name must contain at least one letter or number.");
        }
        return slug.length() > 512 ? slug.substring(0, 512) : slug;
    }

    public static String toMetaBodyText(String message, MetaParameterFormat format) {
        if (format == MetaParameterFormat.POSITIONAL) {
            List<String> variables = extractVariablesInOrder(message);
            String result = message;
            for (int i = 0; i < variables.size(); i++) {
                String var = variables.get(i);
                result = result.replaceAll(
                        "\\{\\{\\s*" + Pattern.quote(var) + "\\s*\\}\\}",
                        "{{" + (i + 1) + "}}"
                );
            }
            return result;
        }
        return message;
    }

    public static List<String> extractVariablesInOrder(String message) {
        Set<String> seen = new LinkedHashSet<>();
        Matcher matcher = PLACEHOLDER.matcher(message);
        while (matcher.find()) {
            seen.add(matcher.group(1));
        }
        return new ArrayList<>(seen);
    }

    /** Only pass variables that appear in the template body, in first-seen order. */
    public static Map<String, String> parametersForSend(String templateMessage,
                                                       Map<String, String> allVariables) {
        Map<String, String> filtered = new LinkedHashMap<>();
        for (String key : extractVariablesInOrder(templateMessage)) {
            filtered.put(key, allVariables.get(key));
        }
        return filtered;
    }

    /**
     * Builds a Meta API IMAGE header component. Requires a media handle from
     * Meta's Resumable Upload API (sample image for template review).
     */
    public static Map<String, Object> buildImageHeaderComponent(String headerHandle) {
        Map<String, Object> example = new LinkedHashMap<>();
        example.put("header_handle", List.of(headerHandle));

        Map<String, Object> component = new LinkedHashMap<>();
        component.put("type", "HEADER");
        component.put("format", "IMAGE");
        component.put("example", example);
        return component;
    }

    /**
     * Builds a Meta API BODY component including required sample values for every variable.
     * Meta rejects templates without {@code example} — typically as INVALID_FORMAT.
     */
    public static Map<String, Object> buildBodyComponent(String message, MetaParameterFormat format) {
        List<String> variables = extractVariablesInOrder(message);
        for (String variable : variables) {
            validateParamName(variable);
        }

        String text = toMetaBodyText(message, format);
        Map<String, Object> component = new LinkedHashMap<>();
        component.put("type", "BODY");
        component.put("text", text);

        if (variables.isEmpty()) {
            return component;
        }

        Map<String, Object> example = new LinkedHashMap<>();
        if (format == MetaParameterFormat.NAMED) {
            List<Map<String, String>> namedParams = new ArrayList<>();
            for (String variable : variables) {
                Map<String, String> entry = new LinkedHashMap<>();
                entry.put("param_name", variable);
                entry.put("example", exampleFor(variable));
                namedParams.add(entry);
            }
            example.put("body_text_named_params", namedParams);
        } else {
            List<String> samples = variables.stream().map(MetaTemplateBodyBuilder::exampleFor).toList();
            example.put("body_text", List.of(samples));
        }
        component.put("example", example);
        return component;
    }

    public static void validateParamName(String variable) {
        if (!variable.matches("[a-z][a-z0-9_]*")) {
            throw new IllegalArgumentException(
                    "Variable '{{" + variable + "}}' is invalid for Meta named templates. "
                            + "Use lowercase letters, numbers, and underscores only (e.g. {{organization_name}}).");
        }
    }

    private static String exampleFor(String variable) {
        return switch (variable) {
            case "name" -> "Ramesh Patel";
            case "mobile" -> "9876543210";
            case "building" -> "Sunrise Apartments";
            case "floor" -> "2";
            case "flat" -> "12";
            case "amount" -> "500";
            case "date" -> "22 August 2026";
            case "organization_name", "organizationname" -> "Ganpati Mandal";
            default -> "Sample";
        };
    }
}
