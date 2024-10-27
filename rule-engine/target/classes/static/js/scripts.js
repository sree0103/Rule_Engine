document.addEventListener("DOMContentLoaded", function() {
    const evaluateForm = document.getElementById("evaluate-form");

    if (evaluateForm) {
        evaluateForm.addEventListener("submit", function(event) {
            event.preventDefault(); // Prevent default form submission
            const formData = new FormData(this);
            const data = {};
            formData.forEach((value, key) => {
                data[key] = value; // Collect data in a key-value format
            });

            // Use fetch API to submit form data via AJAX
            fetch(this.action, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(data),
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(data => {
                // Handle successful response here
                console.log(data);
                // Optionally, redirect or update the UI with the result
                alert('Evaluation Result: ' + JSON.stringify(data));
            })
            .catch(error => {
                console.error('Error:', error);
                alert('An error occurred during evaluation: ' + error.message);
            });
        });
    }
});
