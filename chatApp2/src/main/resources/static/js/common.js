function logout() {
  fetch("/api/auth/logout", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    credentials: "include",
  }).then((response) => {
    if (response.ok) {
      window.location.href = "/login";
    }
  });
}

function confirmLogout() {
  if (confirm("ログアウトしてもよろしいですか？")) {
    logout();
  }
}

function checkPasswordStrength(password) {
  const regex =
    /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
  return regex.test(password);
}

const passwordInput = document.getElementById("password");
if (passwordInput) {
  passwordInput.addEventListener("input", (event) => {
    updatePasswordStrength(event.target.value);
  });
}

window.addEventListener("DOMContentLoaded", () => {
  const signupPasswordInput = document.getElementById("signupPassword");
  if (signupPasswordInput) {
    signupPasswordInput.addEventListener("input", (event) => {
      updatePasswordStrength(event.target.value);
    });
  }
  const profilePasswordInput = document.getElementById("profilePassword");
  if (profilePasswordInput) {
    profilePasswordInput.addEventListener("input", (event) => {
      updatePasswordStrength(event.target.value);
    });
  }
});

function sanitizeHTML(str) {
  var div = document.createElement("div");
  div.textContent = str;
  return div.innerHTML;
}

function getErrorMessage(status) {
  switch (status) {
    case 400:
      return "リクエストが不正です。";
    case 401:
      return "認証が必要です。ログインしてください。";
    case 403:
      return "アクセス権限がありません。";
    case 404:
      return "リソースが見つかりませんでした。";
    case 500:
      return "サーバー内部でエラーが発生しました。";
    case 502:
      return "ゲートウェイエラーが発生しました。";
    case 503:
      return "サービスが一時的に利用できません。";
    default:
      return `不明なエラーが発生しました。(${status})`;
  }
}

async function handleError(response) {
  if (!response.ok) {
    let errorMessage = getErrorMessage(response.status);
    throw new Error(errorMessage);
  }
  return response.json();
}

function showError(message) {
  const errorMessage = document.getElementById("errorMessage");
  errorMessage.innerText = message;
  errorMessage.classList.remove("d-none");
}
