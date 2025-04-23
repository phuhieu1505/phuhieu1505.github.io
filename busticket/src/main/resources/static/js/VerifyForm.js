document.getElementById("verifyForm").addEventListener("submit", function (event) {
    event.preventDefault();
    let email = document.querySelector("strong").innerText;
    let verificationCode = document.getElementById("verificationCode").value;
    fetch("/auth/verify", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            email: email,
            verificationCode: verificationCode,
        }),
    })
        .then((response) => response.json())
        .then((data) => {
            if (data.success) {
                alert("Account verified successfully");
                window.location.href = "/auth/login";
            } else {
                alert("Failed to verify account: " + data.message);
            }
        })
        .catch((error) => {
            console.error("Error:", error);
        });
});
