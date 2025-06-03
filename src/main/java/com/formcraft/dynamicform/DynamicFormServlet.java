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
        // loading resources folder (src/main/resources)
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

        // 1. generate the fields snippet
        String fieldsHtml = FormGenerator.buildFields(schemaJson, uiConfigJson);

        // 2. extract from UI config (like title, submit button label)
        String formTitle = uiConfigJson.optString("formTitle", "Dynamic Form");
        String submitLabel = uiConfigJson.optString("submitButtonLabel", "Submit");

        // 3. setting attributes so JSP can access them
        request.setAttribute("formTitle", formTitle);
        request.setAttribute("formFields", fieldsHtml);
        request.setAttribute("submitLabel", submitLabel);

        // 4. forward to the JSP
        RequestDispatcher dispatcher = request.getRequestDispatcher("/form.jsp");
        dispatcher.forward(request, response);
    }
}
