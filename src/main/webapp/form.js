document.addEventListener("DOMContentLoaded", function() {
    const form = document.getElementById("dynamicForm");
    if (!form) return;

    form.addEventListener("submit", function(event) {
        // Example: check if 'username' is at least 3 chars
        const usernameInput = document.getElementById("username");
        if (usernameInput) {
            const username = usernameInput.value.trim();
            if (username.length < 3) {
                alert("Username must be at least 3 characters long.");
                event.preventDefault(); // stop form submission
                return;
            }
        }

        // Example: check if 'email' contains '@'
        const emailInput = document.getElementById("email");
        if (emailInput) {
            const email = emailInput.value.trim();
            if (!email.includes("@")) {
                alert("Invalid email address.");
                event.preventDefault();
                return;
            }
        }

        // Add more client-side checks if needed...
        // For advanced scenarios, you could parse a UI config
        // or schema on the client to handle dynamic rules.

        // If all checks pass, the form will be submitted to SubmitFormServlet
    });
});
