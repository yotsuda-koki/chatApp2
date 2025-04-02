function previewAvatar(event) {
  const input = event.target;
  const preview = document.getElementById("avatarPreview");
  const file = input.files[0];
  if (file) {
    const reader = new FileReader();
    reader.onload = function (e) {
      preview.src = e.target.result;
      preview.style.display = "block";
    };
    reader.readAsDataURL(file);
  }
}

async function loadUserProfile() {
  try {
    const response = await fetch("/api/user/me");
    if (!response.ok) throw new Error("Failed to fetch user info");
    const data = await response.json();
    document.getElementById("userDisplayName").innerText = `${sanitizeHTML(
      data.displayName
    )} #${sanitizeHTML(data.username)}`;
    document.getElementById("userEmail").innerText = sanitizeHTML(data.email);
    document.getElementById("username").value = sanitizeHTML(data.username);
    document.getElementById("displayName").value = sanitizeHTML(
      data.displayName
    );
    document.getElementById("email").value = sanitizeHTML(data.email);
    const avatarPreview = document.getElementById("avatarPreview");
    if (data.profilePicture) {
      avatarPreview.src = data.profilePicture;
    } else {
      avatarPreview.src = "/images/no-image.png";
    }
  } catch (error) {
    console.error("Error loading profile:", error);
  }
}

async function updateProfile() {
  const username = document.getElementById("username").value;
  const displayName = document.getElementById("displayName").value;
  const email = document.getElementById("email").value;
  const currentPassword = document.getElementById("currentPassword").value;
  const newPassword = document.getElementById("password").value;
  const avatar = document.getElementById("avatar").files[0];

  let base64Image = null;

  if (avatar) {
    base64Image = await new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => resolve(reader.result);
      reader.onerror = reject;
      reader.readAsDataURL(avatar);
    });
  }

  const data = {
    username: username,
    displayName: displayName,
    email: email,
    currentPassword: currentPassword,
    newPassword: newPassword,
    profilePicture: base64Image,
  };

  try {
    const response = await fetch("/api/user/update", {
      method: "POST",
      credentials: "include",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(data),
    });

    if (!response.ok) throw new Error("Failed to update profile");

    alert("Profile updated successfully!");

    logout();
  } catch (error) {
    console.error("Error updating profile:", error);
    const errorMessage = document.getElementById("errorMessage");
    errorMessage.innerText =
      "プロフィールの更新に失敗しました。もう一度お試しください";
    errorMessage.classList.remove("d-none");
  }
}

function toggleCurrentPassword() {
  const currentPasswordInput = document.getElementById("currentPassword");
  const currentIcon = document.getElementById("toggleCurrentPasswordIcon");

  if (currentPasswordInput.type === "password") {
    currentPasswordInput.type = "text";
    currentIcon.classList.remove("bi-eye");
    currentIcon.classList.add("bi-eye-slash");
  } else {
    currentPasswordInput.type = "password";
    currentIcon.classList.remove("bi-eye-slash");
    currentIcon.classList.add("bi-eye");
  }
}

function togglePassword() {
  const passwordInput = document.getElementById("password");
  const icon = document.getElementById("togglePasswordIcon");

  if (passwordInput.type === "password") {
    passwordInput.type = "text";
    icon.classList.remove("bi-eye");
    icon.classList.add("bi-eye-slash");
  } else {
    passwordInput.type = "password";
    icon.classList.remove("bi-eye-slash");
    icon.classList.add("bi-eye");
  }
}

function updatePasswordStrength(password) {
  const result = zxcvbn(password);
  const strengthIndicator = document.getElementById("passwordStrength");
  if (strengthIndicator) {
    if (password.length === 0) {
      strengthIndicator.innerText = "";
      strengthIndicator.className = "text-muted";
      return;
    }

    let strengthText = "";
    let strengthClass = "";

    switch (result.score) {
      case 0:
        strengthText = "非常に弱い";
        strengthClass = "text-danger";
        break;
      case 1:
        strengthText = "弱い";
        strengthClass = "text-warning";
        break;
      case 2:
        strengthText = "普通";
        strengthClass = "text-info";
        break;
      case 3:
        strengthText = "強い";
        strengthClass = "text-primary";
        break;
      case 4:
        strengthText = "非常に強い";
        strengthClass = "text-success";
        break;
    }

    strengthIndicator.innerText = strengthText;
    strengthIndicator.className = strengthClass;
  }
}

window.addEventListener("DOMContentLoaded", loadUserProfile);
