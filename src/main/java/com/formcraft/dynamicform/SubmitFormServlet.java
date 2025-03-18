package com.formcraft.dynamicform;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "SubmitFormServlet", urlPatterns = {"/submitForm"})
public class SubmitFormServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Grab the form fields (example)
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String ageStr = request.getParameter("age");
        String country = request.getParameter("country");
        String birthDate = request.getParameter("birthDate");

        // Basic server-side checks (example)
        // In a real scenario, you might integrate JSON Schema validation libraries, etc.
        if (username == null || username.trim().length() < 3) {
            sendError(response, "Username must be at least 3 characters.");
            return;
        }
        if (email == null || !email.contains("@")) {
            sendError(response, "Invalid email address.");
            return;
        }
        // ... and so on

        // If all is good, show success
        response.setContentType("text/html");
        try (PrintWriter out = response.getWriter()) {
            out.println("<html><body>");
            out.println("<h1>Form submitted successfully!</h1>");
            out.println("<p>Username: " + username + "</p>");
            out.println("<p>Email: " + email + "</p>");
            out.println("<p>Age: " + ageStr + "</p>");
            out.println("<p>Country: " + country + "</p>");
            out.println("<p>Birth Date: " + birthDate + "</p>");
            out.println("</body></html>");
        }
    }

    private void sendError(HttpServletResponse response, String msg) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        try (PrintWriter out = response.getWriter()) {
            out.println("<html><body>");
            out.println("<h2>Error: " + msg + "</h2>");
            out.println("<a href=\"dynamicForm\">Go Back</a>");
            out.println("</body></html>");
        }
    }
}
