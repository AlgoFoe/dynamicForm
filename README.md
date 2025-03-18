## Dynamic Form Generation

### 1.FormGenerator.java:
  -Builds only the field portion of the form as an HTML snippet.
### 2.form.jsp:
  -Acts as the overall layout or template.
  -Uses JSP Expression Language (${formFields}, etc.) to insert dynamic data.
### 3.DynamicFormServlet:
  -Loads the JSON schema & UI config from resources.
  -Uses FormGenerator for the field snippet.
  -Sets request attributes (formTitle, formFields, submitLabel).
  -Forwards to form.jsp.
### 4.SubmitFormServlet:
  -Processes the form submission (doPost)
