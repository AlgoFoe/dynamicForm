package com.formcraft.dynamicform;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class FormGenerator {

    public static String buildFields(JSONObject schemaJson, JSONObject uiConfigJson) {
        JSONObject properties = schemaJson.getJSONObject("properties");
        JSONArray required = schemaJson.optJSONArray("required");
        Set<String> requiredFields = new HashSet<>();

        if (required != null) {
            for (int i = 0; i < required.length(); i++) {
                requiredFields.add(required.getString(i));
            }
        }

        JSONObject fieldsConfig = uiConfigJson.getJSONObject("fields");

        // sort fields by order
        List<String> fieldNames = new ArrayList<>();
        for (Iterator<String> it = fieldsConfig.keys(); it.hasNext();) {
            fieldNames.add(it.next());
        }

        fieldNames.sort((a, b) -> {
            int orderA = fieldsConfig.optJSONObject(a).optInt("order", Integer.MAX_VALUE);
            int orderB = fieldsConfig.optJSONObject(b).optInt("order", Integer.MAX_VALUE);
            return Integer.compare(orderA, orderB);
        });

        StringBuilder fieldsBuilder = new StringBuilder();
        fieldsBuilder.append("<div class=\"form-container\">\n");

        for (String fieldName : fieldNames) {
            JSONObject fieldSchema = properties.optJSONObject(fieldName);
            if (fieldSchema == null) continue;

            JSONObject fieldUI = fieldsConfig.getJSONObject(fieldName);

            String fieldHtml = generateFieldHtml(fieldName, fieldSchema, fieldUI, requiredFields.contains(fieldName));
            fieldsBuilder.append(fieldHtml);
        }

        fieldsBuilder.append("</div>\n");
        return fieldsBuilder.toString();
    }

    private static String generateFieldHtml(String fieldName, JSONObject fieldSchema, JSONObject fieldUI, boolean isRequired) {
        StringBuilder html = new StringBuilder();

        String label = fieldUI.optString("label", fieldName);
        String controlType = fieldUI.optString("controlType", "text");
        String placeholder = fieldUI.optString("placeholder", "");
        String helpText = fieldUI.optString("helpText", "");
        String width = fieldUI.optString("width", "full");
        String icon = fieldUI.optString("icon", "");

        // starting field wrapper
        html.append("<div class=\"field-wrapper ").append(width).append("\">\n");

        html.append("  <label for=\"").append(fieldName).append("\" class=\"field-label");
        if (isRequired) html.append(" required");
        html.append("\">");
        if (!icon.isEmpty()) {
            html.append("<i class=\"icon-").append(icon).append("\"></i> ");
        }
        html.append(label);
        if (isRequired) html.append(" *");
        html.append("</label>\n");

        // generate diff control type
        switch (controlType.toLowerCase()) {
            case "select":
                html.append(generateSelectField(fieldName, fieldUI, placeholder));
                break;
            case "radio":
                html.append(generateRadioField(fieldName, fieldUI));
                break;
            case "checkbox":
                if (fieldUI.has("options")) {
                    html.append(generateCheckboxGroup(fieldName, fieldUI));
                } else {
                    html.append(generateSingleCheckbox(fieldName, fieldUI));
                }
                break;
            case "textarea":
                html.append(generateTextarea(fieldName, fieldUI, placeholder));
                break;
            default:
                html.append(generateInputField(fieldName, fieldSchema, fieldUI, controlType, placeholder));
        }

        // helping text
        if (!helpText.isEmpty()) {
            html.append("  <div class=\"help-text\">").append(helpText).append("</div>\n");
        }

        // Error message placeholder
        html.append("  <div class=\"error-message\" id=\"error-").append(fieldName).append("\"></div>\n");

        html.append("</div>\n\n");
        return html.toString();
    }

    private static String generateInputField(String fieldName, JSONObject fieldSchema, JSONObject fieldUI, String controlType, String placeholder) {
        StringBuilder html = new StringBuilder();

        html.append("  <input type=\"").append(controlType).append("\" ")
                .append("name=\"").append(fieldName).append("\" ")
                .append("id=\"").append(fieldName).append("\" ")
                .append("class=\"form-input\"");

        if (!placeholder.isEmpty()) {
            html.append(" placeholder=\"").append(placeholder).append("\"");
        }

        // Add validation attributes from schema
        if (fieldSchema.has("minLength")) {
            html.append(" minlength=\"").append(fieldSchema.getInt("minLength")).append("\"");
        }
        if (fieldSchema.has("maxLength")) {
            html.append(" maxlength=\"").append(fieldSchema.getInt("maxLength")).append("\"");
        }
        if (fieldSchema.has("minimum")) {
            html.append(" min=\"").append(fieldSchema.getInt("minimum")).append("\"");
        }
        if (fieldSchema.has("maximum")) {
            html.append(" max=\"").append(fieldSchema.getInt("maximum")).append("\"");
        }
        if (fieldSchema.has("pattern")) {
            html.append(" pattern=\"").append(fieldSchema.getString("pattern")).append("\"");
        }

        html.append(" />\n");

        // Password strength indicator
        if ("password".equals(controlType) && fieldUI.optBoolean("showStrengthIndicator", false)) {
            html.append("  <div class=\"password-strength\" id=\"strength-").append(fieldName).append("\"></div>\n");
        }

        return html.toString();
    }

    private static String generateSelectField(String fieldName, JSONObject fieldUI, String placeholder) {
        StringBuilder html = new StringBuilder();
        JSONArray options = fieldUI.optJSONArray("options");

        html.append("  <select name=\"").append(fieldName).append("\" ")
                .append("id=\"").append(fieldName).append("\" ")
                .append("class=\"form-select\">\n");

        if (!placeholder.isEmpty()) {
            html.append("    <option value=\"\">").append(placeholder).append("</option>\n");
        }

        if (options != null) {
            for (int i = 0; i < options.length(); i++) {
                String optValue = options.getString(i);
                html.append("    <option value=\"").append(optValue).append("\">")
                        .append(optValue).append("</option>\n");
            }
        }

        html.append("  </select>\n");
        return html.toString();
    }

    private static String generateRadioField(String fieldName, JSONObject fieldUI) {
        StringBuilder html = new StringBuilder();
        JSONArray options = fieldUI.optJSONArray("options");
        String layout = fieldUI.optString("layout", "vertical");

        html.append("  <div class=\"radio-group ").append(layout).append("\">\n");

        if (options != null) {
            for (int i = 0; i < options.length(); i++) {
                JSONObject option = options.getJSONObject(i);
                String value = option.getString("value");
                String optionLabel = option.getString("label");

                html.append("    <div class=\"radio-item\">\n")
                        .append("      <input type=\"radio\" name=\"").append(fieldName).append("\" ")
                        .append("id=\"").append(fieldName).append("_").append(value).append("\" ")
                        .append("value=\"").append(value).append("\" />\n")
                        .append("      <label for=\"").append(fieldName).append("_").append(value).append("\">")
                        .append(optionLabel).append("</label>\n")
                        .append("    </div>\n");
            }
        }

        html.append("  </div>\n");
        return html.toString();
    }

    private static String generateCheckboxGroup(String fieldName, JSONObject fieldUI) {
        StringBuilder html = new StringBuilder();
        JSONArray options = fieldUI.optJSONArray("options");
        String layout = fieldUI.optString("layout", "vertical");
        int columns = fieldUI.optInt("columns", 1);

        html.append("  <div class=\"checkbox-group ").append(layout);
        if ("grid".equals(layout)) {
            html.append(" columns-").append(columns);
        }
        html.append("\">\n");

        if (options != null) {
            for (int i = 0; i < options.length(); i++) {
                JSONObject option = options.getJSONObject(i);
                String value = option.getString("value");
                String optionLabel = option.getString("label");

                html.append("    <div class=\"checkbox-item\">\n")
                        .append("      <input type=\"checkbox\" name=\"").append(fieldName).append("\" ")
                        .append("id=\"").append(fieldName).append("_").append(value).append("\" ")
                        .append("value=\"").append(value).append("\" />\n")
                        .append("      <label for=\"").append(fieldName).append("_").append(value).append("\">")
                        .append(optionLabel).append("</label>\n")
                        .append("    </div>\n");
            }
        }

        html.append("  </div>\n");
        return html.toString();
    }

    private static String generateSingleCheckbox(String fieldName, JSONObject fieldUI) {
        StringBuilder html = new StringBuilder();
        String label = fieldUI.optString("label", fieldName);

        html.append("  <div class=\"checkbox-single\">\n")
                .append("    <input type=\"checkbox\" name=\"").append(fieldName).append("\" ")
                .append("id=\"").append(fieldName).append("\" ")
                .append("value=\"true\" />\n")
                .append("    <label for=\"").append(fieldName).append("\">").append(label).append("</label>\n")
                .append("  </div>\n");

        return html.toString();
    }

    private static String generateTextarea(String fieldName, JSONObject fieldUI, String placeholder) {
        StringBuilder html = new StringBuilder();
        int rows = fieldUI.optInt("rows", 4);

        html.append("  <textarea name=\"").append(fieldName).append("\" ")
                .append("id=\"").append(fieldName).append("\" ")
                .append("class=\"form-textarea\" ")
                .append("rows=\"").append(rows).append("\"");

        if (!placeholder.isEmpty()) {
            html.append(" placeholder=\"").append(placeholder).append("\"");
        }

        html.append("></textarea>\n");
        return html.toString();
    }
}