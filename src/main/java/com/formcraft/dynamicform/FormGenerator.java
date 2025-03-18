package com.formcraft.dynamicform;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;

public class FormGenerator {

    /**
     * Builds an HTML snippet (string) for the form fields
     * based on the given schema & UI config.
     */
    public static String buildFields(JSONObject schemaJson, JSONObject uiConfigJson) {
        // Get the schema properties
        JSONObject properties = schemaJson.getJSONObject("properties");
        // Get the UI config fields
        JSONObject fieldsConfig = uiConfigJson.getJSONObject("fields");

        StringBuilder fieldsBuilder = new StringBuilder();

        // Iterate over each property from the schema
        for (Iterator<String> it = properties.keys(); it.hasNext();) {
            String fieldName = it.next();

            // The schema for this field
            JSONObject fieldSchema = properties.getJSONObject(fieldName);

            // The UI config for this field (if any)
            JSONObject fieldUI = fieldsConfig.optJSONObject(fieldName);
            if (fieldUI == null) {
                // If there's no UI config for this field, skip or create a default approach
                continue;
            }

            // Extract UI info
            String label = fieldUI.optString("label", fieldName);
            String controlType = fieldUI.optString("controlType", "text");
            String placeholder = fieldUI.optString("placeholder", "");
            JSONArray optionsArray = fieldUI.optJSONArray("options"); // for select, radio, etc.

            fieldsBuilder.append("<div>\n");
            fieldsBuilder.append("  <label for=\"").append(fieldName).append("\">")
                    .append(label).append(":</label>\n");

            // If it's a select dropdown
            if ("select".equalsIgnoreCase(controlType) && optionsArray != null) {
                fieldsBuilder.append("  <select name=\"").append(fieldName).append("\" ")
                        .append("id=\"").append(fieldName).append("\">\n");
                for (int i = 0; i < optionsArray.length(); i++) {
                    String optValue = optionsArray.getString(i);
                    fieldsBuilder.append("    <option value=\"").append(optValue).append("\">")
                            .append(optValue).append("</option>\n");
                }
                fieldsBuilder.append("  </select>\n");
            }
            // Otherwise, assume a basic <input>
            else {
                fieldsBuilder.append("  <input type=\"").append(controlType).append("\" ")
                        .append("name=\"").append(fieldName).append("\" ")
                        .append("id=\"").append(fieldName).append("\" ");
                if (!placeholder.isEmpty()) {
                    fieldsBuilder.append("placeholder=\"").append(placeholder).append("\" ");
                }
                fieldsBuilder.append("/>\n");
            }

            fieldsBuilder.append("</div>\n\n");
        }

        return fieldsBuilder.toString();
    }
}
