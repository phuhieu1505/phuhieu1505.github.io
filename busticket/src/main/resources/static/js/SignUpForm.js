document.getElementById("signupForm").addEventListener("submit", async function (event) {
    event.preventDefault();
    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;
    const confirmPassword = document.getElementById("confirmPassword").value;

    if (password !== confirmPassword) {
        alert("Passwords do not match");
        return;
    }

    try {
        const response = await fetch("/auth/signup", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({ email, password }),
        });

        if (!response.ok) {
            throw new Error(`Network response was not ok: ${response.statusText}`);
        }

        const data = await response.json();

        if (data.success) {
            alert("Signup successful. Please verify your account");
        } else {
            alert(`Failed to signup: ${data.message}`);
        }
    } catch (error) {
        console.error("Error:", error);
        alert(`Error: ${error.message}`);
    }
});
