package com.formcraft.dynamicform;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

@WebServlet(name = "DynamicFormServlet", urlPatterns = {"/dynamicForm"})
public class DynamicFormServlet extends HttpServlet {

    private JSONObject schemaJson;
    private JSONObject uiConfigJson;

    @Override
    public void init() throws ServletException {
        // Load JSON schema & UI config once at init
        try {
            schemaJson = readJsonFile("userSchema.json");
            uiConfigJson = readJsonFile("userUIConfig.json");
        } catch (IOException e) {
            throw new ServletException("Error loading JSON files", e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private JSONObject readJsonFile(String resourceName) throws IOException, URISyntaxException {
        // Load from resources folder (src/main/resources)
        var resourceURL = getClass().getClassLoader().getResource(resourceName);
        if (resourceURL == null) {
            throw new IOException("Resource not found: " + resourceName);
        }
        byte[] bytes = Files.readAllBytes(Paths.get(resourceURL.toURI()));
        String content = new String(bytes, StandardCharsets.UTF_8);
        return new JSONObject(content);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Generate the fields snippet
        String fieldsHtml = FormGenerator.buildFields(schemaJson, uiConfigJson);

        // 2. Extract top-level info from UI config (like title, submit button label)
        String formTitle = uiConfigJson.optString("formTitle", "Dynamic Form");
        String submitLabel = uiConfigJson.optString("submitButtonLabel", "Submit");

        // 3. Set attributes so JSP can access them via ${...}
        request.setAttribute("formTitle", formTitle);
        request.setAttribute("formFields", fieldsHtml);
        request.setAttribute("submitLabel", submitLabel);

        // 4. Forward to the JSP
        RequestDispatcher dispatcher = request.getRequestDispatcher("/form.jsp");
        dispatcher.forward(request, response);
    }
}
