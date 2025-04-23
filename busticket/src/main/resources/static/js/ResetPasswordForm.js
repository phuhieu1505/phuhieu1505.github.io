function getQueryParam(param) {
    let urlParams = new URLSearchParams(window.location.search);
    return urlParams.get(param);
}
document.getElementById("token").value = getQueryParam("token");

document
    .getElementById("resetPasswordForm")
    .addEventListener("submit", function (event) {
        event.preventDefault();
        let token = document.getElementById("token").value;
        let newPassword = document.getElementById("newPassword").value;
        let confirmPassword = document.getElementById("confirmPassword").value;

        if (!newPassword) {
            alert("New password cannot be empty");
            return;
        }

        if (newPassword !== confirmPassword) {
            alert("Passwords do not match");
            return;
        }

        fetch("/auth/reset-password", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                token: token,
                password: newPassword,
            }),
        })
            .then((response) => response.json())
            .then((data) => {
                if (data.success) {
                    alert("Password has been reset successfully.");
                    window.location.href = "/auth/login";
                } else {
                    alert("Failed to reset password: " + data.message);
                }
            })
            .catch((error) => {
                console.error("Error:", error);
            });
    });
