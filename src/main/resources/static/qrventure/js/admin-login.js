import { adminApi } from '/qrventure/js/admin-api.js';

const feedback = document.getElementById('feedback');
const form = document.getElementById('loginForm');
const submitButton = document.getElementById('loginSubmit');

const setFeedback = (message, isError = false) => {
  feedback.textContent = message;
  feedback.classList.toggle('error', isError);
};

form.addEventListener('submit', async (event) => {
  event.preventDefault();
  setFeedback('Signing in…');
  submitButton.disabled = true;

  const payload = {
    username: document.getElementById('username').value.trim(),
    password: document.getElementById('password').value
  };

  try {
    await adminApi.login(payload);
    window.location.href = '/admin';
  } catch (error) {
    setFeedback(error.message || 'Unable to sign in right now.', true);
    submitButton.disabled = false;
  }
});
