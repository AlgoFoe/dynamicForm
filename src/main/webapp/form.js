document.addEventListener("DOMContentLoaded", function() {
    const form = document.getElementById("dynamicForm");
    const submitBtn = document.getElementById("submitBtn");

    if (!form) return;

    // validation rules based on schema
    const validationRules = {
        username: {
            minLength: 3,
            maxLength: 20,
            pattern: /^[a-zA-Z0-9_]+$/,
            message: "Username must be 3-20 characters, alphanumeric and underscore only"
        },
        email: {
            pattern: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
            message: "Please enter a valid email address"
        },
        password: {
            minLength: 8,
            message: "Password must be at least 8 characters long"
        },
        confirmPassword: {
            matchField: 'password',
            message: "Passwords do not match"
        },
        firstName: {
            minLength: 1,
            maxLength: 50,
            message: "First name is required and must be 1-50 characters"
        },
        lastName: {
            minLength: 1,
            maxLength: 50,
            message: "Last name is required and must be 1-50 characters"
        },
        age: {
            min: 13,
            max: 80,
            type: 'number',
            message: "Age must be between 13 and 80"
        },
        birthDate: {
            type: 'date',
            message: "Please enter a valid birth date"
        },
        country: {
            required: true,
            message: "Please select your country"
        },
        interests: {
            minItems: 1,
            maxItems: 5,
            type: 'checkbox-group',
            message: "Please select 1-5 interests"
        },
        terms: {
            required: true,
            type: 'checkbox',
            message: "You must accept the terms and conditions"
        }
    };

    // Initialize form enhancements
    initializeFormValidation();
    initializePasswordStrength();
    initializeCheckboxValidation();
    addFieldAnimations();

    function initializeFormValidation() {
        const inputs = form.querySelectorAll('input, select, textarea');

        inputs.forEach(input => {
            input.addEventListener('blur', function() {
                validateField(this);
            });

            input.addEventListener('focus', function() {
                clearFieldError(this);
            });

            if (input.type === 'email' || input.name === 'username' || input.name === 'confirmPassword') {
                input.addEventListener('input', function() {
                    debounce(() => validateField(this), 500)();
                });
            }1
        });

        // Form submission validation
        form.addEventListener('submit', function(event) {
            event.preventDefault();

            if (validateForm()) {
                // Show loading state
                submitBtn.disabled = true;
                submitBtn.textContent = 'Creating Account...';

                // Submit form after short delay for better UX
                setTimeout(() => {
                    this.submit();
                }, 1000);
            }
        });
    }

    function validateField(field) {
        const fieldName = field.name;
        const rule = validationRules[fieldName];

        if (!rule) return true;

        let isValid = true;
        let errorMessage = '';

        const value = field.value.trim();

        // Required field check
        if (rule.required && !value) {
            isValid = false;
            errorMessage = rule.message || `${fieldName} is required`;
        }
        // Length validations
        else if (rule.minLength && value.length < rule.minLength) {
            isValid = false;
            errorMessage = rule.message || `Minimum ${rule.minLength} characters required`;
        }
        else if (rule.maxLength && value.length > rule.maxLength) {
            isValid = false;
            errorMessage = rule.message || `Maximum ${rule.maxLength} characters allowed`;
        }
        // Pattern validation
        else if (rule.pattern && !rule.pattern.test(value)) {
            isValid = false;
            errorMessage = rule.message || 'Invalid format';
        }
        // Number validations
        else if (rule.type === 'number' && value) {
            const numValue = parseInt(value);
            if (rule.min && numValue < rule.min) {
                isValid = false;
                errorMessage = rule.message || `Minimum value is ${rule.min}`;
            }
            else if (rule.max && numValue > rule.max) {
                isValid = false;
                errorMessage = rule.message || `Maximum value is ${rule.max}`;
            }
        }
        // Match field validation (like confirm password)
        else if (rule.matchField) {
            const matchField = form.querySelector(`[name="${rule.matchField}"]`);
            if (matchField && value !== matchField.value) {
                isValid = false;
                errorMessage = rule.message || 'Fields do not match';
            }
        }
        // Checkbox validation
        else if (rule.type === 'checkbox' && rule.required) {
            if (!field.checked) {
                isValid = false;
                errorMessage = rule.message || 'This field is required';
            }
        }

        // Show/hide error
        if (isValid) {
            showFieldSuccess(field);
        } else {
            showFieldError(field, errorMessage);
        }

        return isValid;
    }

    function validateForm() {
        let isFormValid = true;
        const inputs = form.querySelectorAll('input, select, textarea');

        inputs.forEach(input => {
            if (!validateField(input)) {
                isFormValid = false;
            }
        });

        // Validate checkbox groups
        if (!validateCheckboxGroups()) {
            isFormValid = false;
        }

        return isFormValid;
    }

    function validateCheckboxGroups() {
        const interestsCheckboxes = form.querySelectorAll('input[name="interests"]:checked');
        const interestsRule = validationRules.interests;

        if (interestsRule) {
            const checkedCount = interestsCheckboxes.length;

            if (checkedCount < interestsRule.minItems || checkedCount > interestsRule.maxItems) {
                showGroupError('interests', interestsRule.message);
                return false;
            } else {
                clearGroupError('interests');
            }
        }

        return true;
    }

    function showFieldError(field, message) {
        const errorDiv = document.getElementById(`error-${field.name}`);
        if (errorDiv) {
            errorDiv.textContent = message;
            errorDiv.classList.add('show');
        }

        field.classList.add('error');
        field.classList.remove('success');
    }

    function showFieldSuccess(field) {
        const errorDiv = document.getElementById(`error-${field.name}`);
        if (errorDiv) {
            errorDiv.classList.remove('show');
        }

        field.classList.remove('error');
        field.classList.add('success');
    }

    function clearFieldError(field) {
        const errorDiv = document.getElementById(`error-${field.name}`);
        if (errorDiv) {
            errorDiv.classList.remove('show');
        }

        field.classList.remove('error');
    }

    function showGroupError(groupName, message) {
        const errorDiv = document.getElementById(`error-${groupName}`);
        if (errorDiv) {
            errorDiv.textContent = message;
            errorDiv.classList.add('show');
        }
    }

    function clearGroupError(groupName) {
        const errorDiv = document.getElementById(`error-${groupName}`);
        if (errorDiv) {
            errorDiv.classList.remove('show');
        }
    }

    function initializePasswordStrength() {
        const passwordField = document.getElementById('password');
        const strengthDiv = document.getElementById('strength-password');

        if (!passwordField || !strengthDiv) return;

        passwordField.addEventListener('input', function() {
            const password = this.value;
            const strength = calculatePasswordStrength(password);

            strengthDiv.className = `password-strength ${strength.level}`;
            strengthDiv.innerHTML = `<div class="strength-bar"></div>`;

            // Add strength text
            const strengthText = document.createElement('div');
            strengthText.style.cssText = 'font-size: 12px; margin-top: 4px; font-weight: 500;';
            strengthText.textContent = `Password strength: ${strength.level.toUpperCase()}`;
            strengthDiv.appendChild(strengthText);
        });
    }

    function calculatePasswordStrength(password) {
        let score = 0;

        if (password.length >= 8) score++;
        if (password.length >= 12) score++;
        if (/[a-z]/.test(password)) score++;
        if (/[A-Z]/.test(password)) score++;
        if (/\d/.test(password)) score++;
        if (/[^a-zA-Z\d]/.test(password)) score++;

        if (score <= 2) return { level: 'weak', score };
        if (score <= 3) return { level: 'fair', score };
        if (score <= 4) return { level: 'good', score };
        return { level: 'strong', score };
    }

    function initializeCheckboxValidation() {
        const interestsCheckboxes = form.querySelectorAll('input[name="interests"]');

        interestsCheckboxes.forEach(checkbox => {
            checkbox.addEventListener('change', function() {
                validateCheckboxGroups();
            });
        });
    }

    function addFieldAnimations() {
        const fieldWrappers = form.querySelectorAll('.field-wrapper');

        fieldWrappers.forEach((wrapper, index) => {
            wrapper.style.animationDelay = `${index * 0.1}s`;
        });
    }

    // Utility function for debouncing
    function debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }

    let formData = {};

    function saveFormData() {
        const inputs = form.querySelectorAll('input, select, textarea');
        inputs.forEach(input => {
            if (input.type === 'checkbox') {
                formData[input.name] = input.checked;
            } else if (input.type === 'radio') {
                if (input.checked) {
                    formData[input.name] = input.value;
                }
            } else {
                formData[input.name] = input.value;
            }
        });
    }

    function restoreFormData() {
        const inputs = form.querySelectorAll('input, select, textarea');
        inputs.forEach(input => {
            if (formData[input.name] !== undefined) {
                if (input.type === 'checkbox') {
                    input.checked = formData[input.name];
                } else if (input.type === 'radio') {
                    input.checked = (input.value === formData[input.name]);
                } else {
                    input.value = formData[input.name];
                }
            }
        });
    }

    setInterval(saveFormData, 5000);
    window.addEventListener('beforeunload', saveFormData);
});