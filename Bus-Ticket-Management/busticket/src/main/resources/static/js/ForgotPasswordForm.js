document.getElementById("forgotPasswordForm").addEventListener("submit", function (event) {
    event.preventDefault();
    let email = document.getElementById("email").value;
    fetch("/auth/forgot-password", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            email: email,
        }),
    })
        .then((response) => response.json())
        .then((data) => {
            if (data.success) {
                alert(
                    "Password reset instructions have been sent to your email."
                );
            } else {
                alert("Failed to send reset instructions: " + data.message);
            }
        })
        .catch((error) => {
            console.error("Error:", error);
        });
});