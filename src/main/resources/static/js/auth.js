(function () {
    const registerForm = document.getElementById('register-form');
    const registerMessage = document.getElementById('auth-message');

    if (!registerForm) {
        return;
    }

    registerForm.addEventListener('submit', async function (event) {
        event.preventDefault();

        const params = new URLSearchParams();
        params.set('username', registerForm.username.value.trim());
        params.set('password', registerForm.password.value);

        try {
            const response = await fetch('/api/auth/process-sign-up', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
                },
                body: params.toString()
            });

            const text = await response.text();
            if (!response.ok) {
                registerMessage.textContent = text || 'Registration failed';
                registerMessage.classList.remove('d-none');
                registerMessage.classList.add('alert-danger');
                return;
            }

            registerMessage.textContent = 'Registration completed. Redirecting to sign-in...';
            registerMessage.classList.remove('d-none', 'alert-danger');
            registerMessage.classList.add('alert-success');
            setTimeout(function () {
                window.location.href = '/api/auth/sign-in';
            }, 800);
        } catch (error) {
            registerMessage.textContent = error.message;
            registerMessage.classList.remove('d-none');
            registerMessage.classList.add('alert-danger');
        }
    });
})();
