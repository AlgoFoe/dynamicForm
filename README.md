# Dynamic Form Generator

A flexible, configuration-driven web form generator built with Java EE that creates dynamic forms based on JSON schema and UI configuration files. This system eliminates the need to write custom HTML and validation code for each form by making everything configurable through JSON files.

## Features

- **Schema-Driven Form Generation**: Create forms dynamically from JSON schema definitions
- **Multiple Field Types**: Support for text, email, password, number, select, radio, checkbox, textarea, and date fields
- **Comprehensive Validation**: Built-in validation for all data types with custom error messages
- **Real-time Validation**: Both client-side and server-side validation
- **Flexible UI Configuration**: Customize labels, placeholders, field ordering, and styling

## Tech Stack

- **Backend**: Java EE (Servlets)
- **Frontend**: JSP, HTML5, CSS3, JavaScript
- **Server**: Apache Tomcat
- **Data Format**: JSON
- **Build Tool**: Maven
- **IDE**: IntelliJ IDEA

## Project Structure

```
dynamicform/
├── src/
│   └── main/
│       ├── java/com/formcraft/dynamicform/
│       │   ├── DynamicFormServlet.java     # Entry point servlet
│       │   ├── FormGenerator.java          # HTML generation logic
│       │   └── SubmitFormServlet.java      # Form processing & validation
│       ├── resources/
│       │   ├── userSchema.json             # Form structure definition
│       │   └── userUIConfig.json           # UI configuration
│       └── webapp/
│           ├── form.jsp                    # Form template
│           ├── form.js                     # Client-side functionality
│           └── index.jsp                   # Landing page
├── pom.xml
└── README.md
```

## System Workflow

For a detailed visual representation of how the system works, check out our [workflow diagram](https://www.mermaidchart.com/raw/fd775df9-ad6d-43c9-97ec-ba75c91c3052?theme=light&version=v0.1&format=svg).

The system follows this high-level flow:
1. **Initialization**: Load JSON configuration files
2. **Generation**: Create dynamic HTML based on schema and UI config
3. **Rendering**: Display form to user via JSP template
4. **Validation**: Process and validate submitted data
5. **Response**: Show success or error feedback to user

## Supported Field Types

| Field Type | Control Types | Description |
|------------|---------------|-------------|
| **string** | text, email, password, date | Single-line text input with format validation |
| **integer** | number | Numeric input with min/max validation |
| **boolean** | checkbox | Single checkbox for true/false values |
| **array** | checkbox, select (multiple) | Multiple value selection |
| **enum** | select, radio | Single value from predefined options |
| **text** | textarea | Multi-line text input |

## Validation Features

- **Required Field Validation**: Ensure mandatory fields are filled
- **Type Validation**: Verify data types match schema definitions
- **Length Validation**: Check minimum and maximum character limits
- **Pattern Validation**: Custom regex pattern matching
- **Format Validation**: Built-in formats (email, date, etc.)
- **Range Validation**: Numeric min/max value checking
- **Cross-Field Validation**: Password confirmation matching
- **Array Validation**: Item count and value constraints

## UI Customization Options

- **Field Layout**: Control field width (full, half, third)
- **Input Types**: Various HTML5 input types
- **Help Text**: Contextual guidance for users
- **Icons**: Visual indicators for different field types
- **Ordering**: Custom field arrangement
- **Grouping**: Organize related fields together
