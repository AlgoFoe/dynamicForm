package com.formcraft.dynamicform;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

@WebServlet(name = "SubmitFormServlet", urlPatterns = {"/submitForm"})
public class SubmitFormServlet extends HttpServlet {

    private JSONObject schemaJson;
    private JSONObject uiConfigJson;

    @Override
    public void init() throws ServletException {
        try {
            schemaJson = readJsonFile("userSchema.json");
            uiConfigJson = readJsonFile("userUIConfig.json");
        } catch (IOException | URISyntaxException e) {
            throw new ServletException("Error loading JSON files", e);
        }
    }

    private JSONObject readJsonFile(String resourceName) throws IOException, URISyntaxException {
        var resourceURL = getClass().getClassLoader().getResource(resourceName);
        if (resourceURL == null) {
            throw new IOException("Resource not found: " + resourceName);
        }
        byte[] bytes = Files.readAllBytes(Paths.get(resourceURL.toURI()));
        String content = new String(bytes, StandardCharsets.UTF_8);
        return new JSONObject(content);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            // Validate the form data against the schema
            ValidationResult validationResult = validateFormData(request);

            if (!validationResult.isValid()) {
                sendValidationErrors(response, validationResult.getErrors());
                return;
            }

            // Process the valid form data
            Map<String, Object> formData = extractFormData(request);

            // In a real application, you would save this data to a database
            // For now, we'll just display a success message
            sendSuccessResponse(response, formData);

        } catch (Exception e) {
            sendErrorResponse(response, "An unexpected error occurred: " + e.getMessage());
        }
    }

    private ValidationResult validateFormData(HttpServletRequest request) {
        ValidationResult result = new ValidationResult();
        JSONObject properties = schemaJson.getJSONObject("properties");
        JSONArray required = schemaJson.optJSONArray("required");

        Set<String> requiredFields = new HashSet<>();
        if (required != null) {
            for (int i = 0; i < required.length(); i++) {
                requiredFields.add(required.getString(i));
            }
        }

        // Validate each field
        for (Iterator<String> it = properties.keys(); it.hasNext();) {
            String fieldName = it.next();
            JSONObject fieldSchema = properties.getJSONObject(fieldName);

            String[] values = request.getParameterValues(fieldName);
            String value = (values != null && values.length > 0) ? values[0] : null;

            validateField(fieldName, value, values, fieldSchema, requiredFields.contains(fieldName), result);
        }
        validatePasswordMatch(request, result);

        return result;
    }

    private void validatePasswordMatch(HttpServletRequest request, ValidationResult result) {
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        if (password != null && confirmPassword != null && !password.equals(confirmPassword)) {
            result.addError("confirmPassword", "Passwords do not match");
        }
    }

    private void validateField(String fieldName, String value, String[] values, JSONObject fieldSchema, boolean isRequired, ValidationResult result) {
        // Check if required field is missing
        if (isRequired && (value == null || value.trim().isEmpty())) {
            result.addError(fieldName, fieldName + " is required");
            return;
        }

        // Skip validation if field is empty and not required
        if (value == null || value.trim().isEmpty()) {
            return;
        }

        String type = fieldSchema.optString("type", "string");

        switch (type) {
            case "string":
                validateStringField(fieldName, value, fieldSchema, result);
                break;
            case "integer":
                validateIntegerField(fieldName, value, fieldSchema, result);
                break;
            case "boolean":
                validateBooleanField(fieldName, value, fieldSchema, result);
                break;
            case "array":
                validateArrayField(fieldName, values, fieldSchema, result);
                break;
        }
    }

    private void validateStringField(String fieldName, String value, JSONObject fieldSchema, ValidationResult result) {
        // Length validation
        if (fieldSchema.has("minLength")) {
            int minLength = fieldSchema.getInt("minLength");
            if (value.length() < minLength) {
                result.addError(fieldName, fieldName + " must be at least " + minLength + " characters");
                return;
            }
        }

        if (fieldSchema.has("maxLength")) {
            int maxLength = fieldSchema.getInt("maxLength");
            if (value.length() > maxLength) {
                result.addError(fieldName, fieldName + " must not exceed " + maxLength + " characters");
                return;
            }
        }

        // Pattern validation
        if (fieldSchema.has("pattern")) {
            String patternStr = fieldSchema.getString("pattern");
            Pattern pattern = Pattern.compile(patternStr);
            if (!pattern.matcher(value).matches()) {
                result.addError(fieldName, fieldName + " format is invalid");
                return;
            }
        }

        // Format validation
        if (fieldSchema.has("format")) {
            String format = fieldSchema.getString("format");
            if ("email".equals(format)) {
                if (!isValidEmail(value)) {
                    result.addError(fieldName, "Invalid email address");
                    return;
                }
            } else if ("date".equals(format)) {
                if (!isValidDate(value)) {
                    result.addError(fieldName, "Invalid date format");
                    return;
                }
            }
        }

        // Enum validation
        if (fieldSchema.has("enum")) {
            JSONArray enumValues = fieldSchema.getJSONArray("enum");
            boolean isValidEnum = false;
            for (int i = 0; i < enumValues.length(); i++) {
                if (enumValues.getString(i).equals(value)) {
                    isValidEnum = true;
                    break;
                }
            }
            if (!isValidEnum) {
                result.addError(fieldName, fieldName + " has an invalid value");
            }
        }

        // Const validation (for terms acceptance, etc.)
        if (fieldSchema.has("const")) {
            boolean constValue = fieldSchema.getBoolean("const");
            if (constValue && !"true".equals(value)) {
                result.addError(fieldName, fieldName + " must be accepted");
            }
        }
    }

    private void validateIntegerField(String fieldName, String value, JSONObject fieldSchema, ValidationResult result) {
        try {
            int intValue = Integer.parseInt(value);

            if (fieldSchema.has("minimum")) {
                int minimum = fieldSchema.getInt("minimum");
                if (intValue < minimum) {
                    result.addError(fieldName, fieldName + " must be at least " + minimum);
                    return;
                }
            }

            if (fieldSchema.has("maximum")) {
                int maximum = fieldSchema.getInt("maximum");
                if (intValue > maximum) {
                    result.addError(fieldName, fieldName + " must not exceed " + maximum);
                }
            }
        } catch (NumberFormatException e) {
            result.addError(fieldName, fieldName + " must be a valid number");
        }
    }

    private void validateBooleanField(String fieldName, String value, JSONObject fieldSchema, ValidationResult result) {
        if (!"true".equals(value) && !"false".equals(value)) {
            result.addError(fieldName, fieldName + " must be true or false");
        }
    }

    private void validateArrayField(String fieldName, String[] values, JSONObject fieldSchema, ValidationResult result) {
        if (values == null) values = new String[0];

        if (fieldSchema.has("minItems")) {
            int minItems = fieldSchema.getInt("minItems");
            if (values.length < minItems) {
                result.addError(fieldName, fieldName + " must have at least " + minItems + " items");
                return;
            }
        }

        if (fieldSchema.has("maxItems")) {
            int maxItems = fieldSchema.getInt("maxItems");
            if (values.length > maxItems) {
                result.addError(fieldName, fieldName + " must not have more than " + maxItems + " items");
                return;
            }
        }

        // Validate each item against the item schema
        if (fieldSchema.has("items")) {
            JSONObject itemSchema = fieldSchema.getJSONObject("items");
            if (itemSchema.has("enum")) {
                JSONArray enumValues = itemSchema.getJSONArray("enum");
                Set<String> validValues = new HashSet<>();
                for (int i = 0; i < enumValues.length(); i++) {
                    validValues.add(enumValues.getString(i));
                }

                for (String value : values) {
                    if (!validValues.contains(value)) {
                        result.addError(fieldName, fieldName + " contains invalid value: " + value);
                        break;
                    }
                }
            }
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    }

    private boolean isValidDate(String date) {
        try {
            // Simple date format validation (YYYY-MM-DD)
            return date != null && date.matches("^\\d{4}-\\d{2}-\\d{2}$");
        } catch (Exception e) {
            return false;
        }
    }

    private Map<String, Object> extractFormData(HttpServletRequest request) {
        Map<String, Object> formData = new LinkedHashMap<>();
        JSONObject properties = schemaJson.getJSONObject("properties");

        for (Iterator<String> it = properties.keys(); it.hasNext();) {
            String fieldName = it.next();
            JSONObject fieldSchema = properties.getJSONObject(fieldName);
            String type = fieldSchema.optString("type", "string");

            if ("array".equals(type)) {
                String[] values = request.getParameterValues(fieldName);
                formData.put(fieldName, values != null ? Arrays.asList(values) : new ArrayList<>());
            } else if ("boolean".equals(type)) {
                String value = request.getParameter(fieldName);
                formData.put(fieldName, "true".equals(value));
            } else if ("integer".equals(type)) {
                String value = request.getParameter(fieldName);
                try {
                    formData.put(fieldName, value != null ? Integer.parseInt(value) : null);
                } catch (NumberFormatException e) {
                    formData.put(fieldName, null);
                }
            } else {
                formData.put(fieldName, request.getParameter(fieldName));
            }
        }

        return formData;
    }

    private void sendSuccessResponse(HttpServletResponse response, Map<String, Object> formData) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("    <title>Registration Successful</title>");
            out.println("    <meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            out.println("    <style>");
            out.println("        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); margin: 0; padding: 20px; min-height: 100vh; }");
            out.println("        .container { max-width: 600px; margin: 50px auto; background: white; border-radius: 12px; box-shadow: 0 20px 40px rgba(0,0,0,0.1); overflow: hidden; }");
            out.println("        .header { background: linear-gradient(135deg, #48bb78 0%, #38a169 100%); color: white; padding: 30px; text-align: center; }");
            out.println("        .content { padding: 30px; }");
            out.println("        .success-icon { font-size: 3em; margin-bottom: 20px; }");
            out.println("        .data-item { margin: 10px 0; padding: 10px; background: #f7fafc; border-radius: 6px; }");
            out.println("        .label { font-weight: 600; color: #2d3748; }");
            out.println("        .value { color: #4a5568; margin-left: 10px; }");
            out.println("        .back-link { display: inline-block; margin-top: 20px; padding: 12px 24px; background: #4299e1; color: white; text-decoration: none; border-radius: 6px; }");
            out.println("    </style>");
            out.println("</head>");
            out.println("<body>");
            out.println("    <div class='container'>");
            out.println("        <div class='header'>");
            out.println("            <div class='success-icon'>✅</div>");
            out.println("            <h1>Registration Successful!</h1>");
            out.println("            <p>Your account has been created successfully.</p>");
            out.println("        </div>");
            out.println("        <div class='content'>");
            out.println("            <h3>Submitted Information:</h3>");

            for (Map.Entry<String, Object> entry : formData.entrySet()) {
                String fieldName = entry.getKey();
                Object value = entry.getValue();

                // Skip sensitive fields like password
                if ("password".equals(fieldName) || "confirmPassword".equals(fieldName)) {
                    continue;
                }

                out.println("            <div class='data-item'>");
                out.println("                <span class='label'>" + capitalizeFirst(fieldName) + ":</span>");

                if (value instanceof List) {
                    List<?> listValue = (List<?>) value;
                    out.println("                <span class='value'>" + String.join(", ", listValue.stream().map(Object::toString).toArray(String[]::new)) + "</span>");
                } else if (value instanceof Boolean) {
                    out.println("                <span class='value'>" + ((Boolean) value ? "Yes" : "No") + "</span>");
                } else {
                    out.println("                <span class='value'>" + (value != null ? value.toString() : "Not provided") + "</span>");
                }

                out.println("            </div>");
            }

            out.println("            <a href='dynamicForm' class='back-link'>Create Another Account</a>");
            out.println("        </div>");
            out.println("    </div>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    private void sendValidationErrors(HttpServletResponse response, Map<String, String> errors) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("text/html;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("    <title>Validation Errors</title>");
            out.println("    <meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            out.println("    <style>");
            out.println("        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); margin: 0; padding: 20px; min-height: 100vh; }");
            out.println("        .container { max-width: 600px; margin: 50px auto; background: white; border-radius: 12px; box-shadow: 0 20px 40px rgba(0,0,0,0.1); overflow: hidden; }");
            out.println("        .header { background: linear-gradient(135deg, #e53e3e 0%, #c53030 100%); color: white; padding: 30px; text-align: center; }");
            out.println("        .content { padding: 30px; }");
            out.println("        .error-icon { font-size: 3em; margin-bottom: 20px; }");
            out.println("        .error-item { margin: 10px 0; padding: 12px; background: #fed7d7; border-left: 4px solid #e53e3e; border-radius: 6px; }");
            out.println("        .back-link { display: inline-block; margin-top: 20px; padding: 12px 24px; background: #4299e1; color: white; text-decoration: none; border-radius: 6px; }");
            out.println("    </style>");
            out.println("</head>");
            out.println("<body>");
            out.println("    <div class='container'>");
            out.println("        <div class='header'>");
            out.println("            <div class='error-icon'>❌</div>");
            out.println("            <h1>Validation Errors</h1>");
            out.println("            <p>Please correct the following errors and try again.</p>");
            out.println("        </div>");
            out.println("        <div class='content'>");

            for (Map.Entry<String, String> error : errors.entrySet()) {
                out.println("            <div class='error-item'>");
                out.println("                <strong>" + capitalizeFirst(error.getKey()) + ":</strong> " + error.getValue());
                out.println("            </div>");
            }

            out.println("            <a href='javascript:history.back()' class='back-link'>Go Back and Fix Errors</a>");
            out.println("        </div>");
            out.println("    </div>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.setContentType("text/html;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {
            out.println("<html><body>");
            out.println("<h2>Error: " + message + "</h2>");
            out.println("<a href='dynamicForm'>Go Back</a>");
            out.println("</body></html>");
        }
    }

    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    // Helper class for validation results
    private static class ValidationResult {
        private final Map<String, String> errors = new LinkedHashMap<>();

        public void addError(String field, String message) {
            errors.put(field, message);
        }

        public boolean isValid() {
            return errors.isEmpty();
        }

        public Map<String, String> getErrors() {
            return errors;
        }
    }
}