<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${formTitle}</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 20px;
        }

        .container {
            max-width: 800px;
            margin: 0 auto;
            background: white;
            border-radius: 12px;
            box-shadow: 0 20px 40px rgba(0,0,0,0.1);
            overflow: hidden;
        }

        .form-header {
            background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
            color: white;
            padding: 30px;
            text-align: center;
        }

        .form-header h1 {
            font-size: 2.2em;
            margin-bottom: 10px;
            font-weight: 300;
        }

        .form-body {
            padding: 40px;
        }

        .form-container {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 20px;
        }

        .field-wrapper {
            margin-bottom: 25px;
        }

        .field-wrapper.full {
            grid-column: 1 / -1;
        }

        .field-wrapper.half {
            grid-column: span 1;
        }

        .field-label {
            display: block;
            margin-bottom: 8px;
            font-weight: 600;
            color: #2d3748;
            font-size: 14px;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        .field-label.required::after {
            content: " *";
            color: #e53e3e;
        }

        .field-label i {
            margin-right: 8px;
            color: #4299e1;
        }

        .form-input, .form-select, .form-textarea {
            width: 100%;
            padding: 12px 16px;
            border: 2px solid #e2e8f0;
            border-radius: 8px;
            font-size: 16px;
            transition: all 0.3s ease;
            background: #f7fafc;
        }

        .form-input:focus, .form-select:focus, .form-textarea:focus {
            outline: none;
            border-color: #4299e1;
            background: white;
            box-shadow: 0 0 0 3px rgba(66, 153, 225, 0.1);
        }

        .form-input:invalid {
            border-color: #e53e3e;
        }

        .form-input:valid {
            border-color: #48bb78;
        }

        .radio-group, .checkbox-group {
            display: flex;
            flex-wrap: wrap;
            gap: 15px;
        }

        .radio-group.vertical, .checkbox-group.vertical {
            flex-direction: column;
            gap: 10px;
        }

        .checkbox-group.grid {
            display: grid;
            gap: 12px;
        }

        .checkbox-group.columns-2 { grid-template-columns: repeat(2, 1fr); }
        .checkbox-group.columns-3 { grid-template-columns: repeat(3, 1fr); }
        .checkbox-group.columns-4 { grid-template-columns: repeat(4, 1fr); }

        .radio-item, .checkbox-item {
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .radio-item input, .checkbox-item input, .checkbox-single input {
            width: 18px;
            height: 18px;
            accent-color: #4299e1;
        }

        .radio-item label, .checkbox-item label, .checkbox-single label {
            cursor: pointer;
            color: #4a5568;
            font-weight: 500;
        }

        .checkbox-single {
            display: flex;
            align-items: center;
            gap: 12px;
            padding: 12px;
            background: #f7fafc;
            border-radius: 8px;
            border: 2px solid #e2e8f0;
            transition: all 0.3s ease;
        }

        .checkbox-single:hover {
            background: #edf2f7;
            border-color: #cbd5e0;
        }

        .help-text {
            margin-top: 6px;
            font-size: 13px;
            color: #718096;
            font-style: italic;
        }

        .error-message {
            margin-top: 6px;
            font-size: 13px;
            color: #e53e3e;
            font-weight: 500;
            display: none;
        }

        .error-message.show {
            display: block;
        }

        .password-strength {
            margin-top: 8px;
            height: 4px;
            background: #e2e8f0;
            border-radius: 2px;
            overflow: hidden;
        }

        .password-strength .strength-bar {
            height: 100%;
            transition: all 0.3s ease;
            border-radius: 2px;
        }

        .password-strength.weak .strength-bar { width: 25%; background: #e53e3e; }
        .password-strength.fair .strength-bar { width: 50%; background: #ed8936; }
        .password-strength.good .strength-bar { width: 75%; background: #ecc94b; }
        .password-strength.strong .strength-bar { width: 100%; background: #48bb78; }

        .submit-section {
            margin-top: 40px;
            padding-top: 30px;
            border-top: 1px solid #e2e8f0;
            text-align: center;
        }

        .submit-btn {
            background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
            color: white;
            border: none;
            padding: 16px 40px;
            font-size: 16px;
            font-weight: 600;
            border-radius: 50px;
            cursor: pointer;
            transition: all 0.3s ease;
            text-transform: uppercase;
            letter-spacing: 1px;
            box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
        }

        .submit-btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 25px rgba(0, 0, 0, 0.3);
        }

        .submit-btn:active {
            transform: translateY(0);
        }

        .submit-btn:disabled {
            background: #a0aec0;
            cursor: not-allowed;
            transform: none;
            box-shadow: none;
        }

        /* Icons */
        .icon-user::before { content: "👤"; }
        .icon-email::before { content: "📧"; }
        .icon-lock::before { content: "🔒"; }
        .icon-calendar::before { content: "📅"; }
        .icon-globe::before { content: "🌍"; }

        /* Responsive */
        @media (max-width: 768px) {
            .form-container {
                grid-template-columns: 1fr;
            }

            .field-wrapper.half {
                grid-column: 1;
            }

            .container {
                margin: 10px;
            }

            .form-body {
                padding: 20px;
            }
        }

        /* Animation */
        .field-wrapper {
            opacity: 0;
            transform: translateY(20px);
            animation: slideUp 0.6s ease forwards;
        }

        @keyframes slideUp {
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }
    </style>
</head>
<body>
<div class="container">
    <div class="form-header">
        <h1>${formTitle}</h1>
    </div>

    <div class="form-body">
        <form method="POST" action="submitForm" id="dynamicForm" novalidate>
            ${formFields}

            <div class="submit-section">
                <button type="submit" class="submit-btn" id="submitBtn">
                    ${submitLabel}
                </button>
            </div>
        </form>
    </div>
</div>

<script src="form.js"></script>
</body>
</html>