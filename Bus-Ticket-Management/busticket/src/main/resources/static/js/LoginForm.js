// Function to get query parameters
function getQueryParam(param) {
    let urlParams = new URLSearchParams(window.location.search);
    return urlParams.get(param);
}

// Display success message
let successMessage = getQueryParam('message');
if (successMessage) {
    alert(successMessage);
}

// Display error message
let errorMessage = getQueryParam('error');
if (errorMessage) {
    alert(errorMessage);
}

document.getElementById("loginForm").addEventListener("submit", function (event) {
    event.preventDefault();
    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;

    $.ajax({
        url: "/auth/login",
        method: "POST",
        contentType: "application/json",
        data: JSON.stringify({ email, password }),
        success: function (data) {
            if (data.success) {
                alert("Login successful");
                window.localStorage.setItem("jwtToken", data.token);
                window.location.href = "/home/index";
            } else {
                alert(`Failed to login: ${data.message}`);
            }
        },
        error: function (xhr) {
            let errorMessage = "An error occurred";
            if (xhr.responseJSON && xhr.responseJSON.message) {
                errorMessage = xhr.responseJSON.message;
            }
            alert(`Error: ${errorMessage}`);
        }
    });
});