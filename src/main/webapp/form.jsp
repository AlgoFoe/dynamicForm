<%--
  Created by IntelliJ IDEA.
  User: varun
  Date: 12-03-2025
  Time: 15:02
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>${formTitle}</title>
</head>
<body>
<h1>${formTitle}</h1>

<form method="POST" action="submitForm" id="dynamicForm">
    <!-- Insert the dynamic fields snippet -->
    ${formFields}

    <button type="submit">${submitLabel}</button>
</form>

<!-- Client-side JS for optional validations -->
<script src="form.js"></script>
</body>
</html>
