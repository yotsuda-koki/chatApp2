document.addEventListener("DOMContentLoaded", async function () {
  const fileInput = document.getElementById("fileInput");
  const fileButton = document.getElementById("fileButton");
  const messageInput = document.getElementById("messageInput");
  const sendButton = document.getElementById("sendButton");
  const dropArea = document.getElementById("dropArea");
  const filePreview = document.getElementById("filePreview");
  const chatBox = document.getElementById("chatBox");
  const chatTitle = document.getElementById("chatTitle");
  const requestList = document.getElementById("requestList");
  const requestListContainer = document.getElementById("requestListContainer");

  let stompClient = null;
  let uploadedFile = null;
  let groupId = null;
  let currentUserId = null;
  let isAdmin = null;

  function connectWebSocket() {
    const socket = new SockJS("/ws");
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function () {
      console.log("WebSocket 接続開始");
      subscribeToGroupMessages();
    });
  }

  async function getGroupRole(groupId) {
    try {
      const response = await fetch(`/api/member/${groupId}/role`, {
        credentials: "include",
      });
      const data = await response.text();
      console.log(data);
      if (data == "ADMIN") {
        isAdmin = true;
        requestListContainer.style.display = "block";
        loadJoinRequests(groupId);
      }
    } catch (error) {
      console.log(error);
    }
  }

  function subscribeToGroupMessages() {
    if (!stompClient || !groupId) return;
    stompClient.subscribe(`/topic/group/${groupId}`, function (message) {
      const messageData = JSON.parse(message.body);
      const type =
        Number(messageData.senderId) === Number(currentUserId) ? "To" : "From";
      displayMessage(messageData, type);
    });
  }

  async function loadChat() {
    try {
      groupId = window.location.pathname.split("/").pop();
      await getGroup(groupId);

      currentUserId = await getCurrentUserId();
      await getGroupRole(groupId);

      const response = await fetch(`/api/chat/${groupId}`, {
        credentials: "include",
      });
      const messages = await response.json();
      chatBox.innerHTML = "";
      messages.forEach((message) => {
        const type =
          Number(message.senderId) === Number(currentUserId) ? "To" : "From";
        displayMessage(message, type);
      });
    } catch (error) {
      console.error("チャット履歴の習得に失敗しました。", error);
    }
  }

  async function getGroup(groupId) {
    try {
      const response = await fetch(`/api/groups/${groupId}`, {
        credentials: "include",
      });
      const group = await response.json();
      chatTitle.textContent = `${sanitizeHTML(group.name)}`;
    } catch (error) {
      console.log("グループの習得に失敗しました。");
    }
  }

  async function getCurrentUserId() {
    try {
      const response = await fetch("/api/user/me", {
        credentials: "include",
      });
      const data = await response.json();
      return data.id;
    } catch (error) {
      console.log(error);
    }
  }

  async function uploadFile(file) {
    const formData = new FormData();
    formData.append("file", file);

    try {
      const response = await fetch("/api/files/upload", {
        method: "POST",
        credentials: "include",
        body: formData,
      });

      if (response.ok) {
        const fileUrl = await response.text();
        console.log("ファイルアップロード成功:", fileUrl);
        return fileUrl;
      } else {
        console.error("ファイルアップロード失敗");
        return null;
      }
    } catch (error) {
      console.error("アップロードエラー:", error);
      return null;
    }
  }

  async function sendMessage() {
    const message = messageInput.value.trim();
    if (message === "" && !uploadedFile) return;

    let fileUrl = null;
    if (uploadedFile) {
      fileUrl = await uploadFile(uploadedFile);
      if (!fileUrl) {
        alert("ファイルアップロードに失敗しました。");
        return;
      }
    }

    const content = fileUrl ? fileUrl : message;
    const messageData = {
      groupId: groupId,
      content: content,
      timestamp: new Date().toISOString(),
    };

    if (stompClient && stompClient.connected) {
      stompClient.send(
        "/app/group.sendMessage",
        {},
        JSON.stringify(messageData)
      );
    } else {
      console.log("WebSocketが接続されていません");
    }

    setTimeout(() => {
      loadChat();
    }, 300);

    messageInput.value = "";
    filePreview.innerHTML = "";
    uploadedFile = null;
    fileInput.value = "";
  }

  function displayMessage(messageData, type) {
    const messageItem = document.createElement("div");
    messageItem.classList.add(
      "d-flex",
      "mb-2",
      type === "To" ? "justify-content-end" : "justify-content-start"
    );

    let contentHtml;

    if (messageData.content.startsWith("/api/files/download/")) {
      const fileName = messageData.content.split("/").pop();
      const linkClass = type === "To" ? "text-white" : "text-primary";
      contentHtml = `
      <a href="${
        messageData.content
      }" class="${linkClass}" download style="text-decoration: underline;">
        ${sanitizeHTML(fileName)}
      </a>
    `;
    } else {
      contentHtml = `<p class="mb-1">${sanitizeHTML(messageData.content)}</p>`;
    }

    const profileImage = `<img src="${sanitizeHTML(
      messageData.profilePicture
    )}" alt="Profile" class="rounded-circle me-2" style="width: 40px; height: 40px;">`;
    const senderName = `<p class="mb-1 fw-bold">${sanitizeHTML(
      messageData.senderDisplayName
    )}</p>`;
    messageItem.innerHTML = `
      <div class="d-flex align-items-center">
        ${profileImage}
        <div class="card ${
          type === "To" ? "bg-primary text-white" : "bg-light text-dark"
        } p-2 rounded shadow-sm" style="max-width: 75%;">
          ${senderName}
          <p class="mb-1">${contentHtml}</p>
          <small class="${
            type === "To" ? "text-light" : "text-muted"
          }">${new Date(messageData.timestamp).toLocaleString()}</small>
        </div>
      </div>
    `;
    chatBox.appendChild(messageItem);
    chatBox.scrollTop = chatBox.scrollHeight;
  }

  async function loadJoinRequests(groupId) {
    try {
      const response = await fetch(`/api/member/${groupId}/requests`, {
        credentials: "include",
      });
      const requests = await response.json();

      requestList.innerHTML = "";

      requests.forEach((request) => {
        if (request.requestStatus == "PENDING") {
          const listItem = document.createElement("div");
          listItem.classList.add(
            "list-group-item",
            "d-flex",
            "justify-content-between",
            "align-items-center"
          );

          const approveButton = document.createElement("button");
          approveButton.classList.add("btn", "btn-success", "btn-sm", "me-2");
          approveButton.innerHTML = '<i class="bi bi-check-circle"></i> 承認';
          approveButton.addEventListener("click", () =>
            approveRequest(request.id, groupId)
          );

          const rejectButton = document.createElement("button");
          rejectButton.classList.add("btn", "btn-danger", "btn-sm");
          rejectButton.innerHTML = '<i class="bi bi-x-circle"></i> 拒否';
          rejectButton.addEventListener("click", () =>
            rejectRequest(request.id, groupId)
          );

          const userInfo = document.createElement("span");
          userInfo.textContent = `${request.displayName} #${sanitizeHTML(
            request.userName
          )}`;

          const buttonContainer = document.createElement("div");
          buttonContainer.appendChild(approveButton);
          buttonContainer.appendChild(rejectButton);

          listItem.appendChild(userInfo);
          listItem.appendChild(buttonContainer);
          requestList.appendChild(listItem);
        }
      });
    } catch (error) {
      console.error("参加リクエストの取得に失敗しました。", error);
    }
  }

  async function approveRequest(requestId, groupId) {
    try {
      const response = await fetch(`/api/member/${requestId}/approve`, {
        method: "POST",
        credentials: "include",
      });
      if (response.ok) {
        loadJoinRequests(groupId);
      }
    } catch (error) {
      console.log(error);
    }
  }

  async function rejectRequest(requestId, groupId) {
    try {
      const response = await fetch(`/api/member/${requestId}/reject`, {
        method: "POST",
        credentials: "include",
      });
      if (response.ok) {
        loadJoinRequests(groupId);
      }
    } catch (error) {
      console.log(error);
    }
  }

  fileButton.addEventListener("click", () => {
    fileInput.click();
  });

  fileInput.addEventListener("change", handleFileUpload);

  dropArea.addEventListener("dragover", (event) => {
    event.preventDefault();
    dropArea.classList.add("active");
  });

  dropArea.addEventListener("dragleave", () => {
    dropArea.classList.remove("active");
  });

  dropArea.addEventListener("drop", (event) => {
    event.preventDefault();
    dropArea.classList.remove("active");

    const file = event.dataTransfer.files[0];
    if (file) {
      uploadedFile = file;
      previewFile(file);
    }
  });

  function handleFileUpload(event) {
    const file = event.target.files[0];
    uploadedFile = file;
    previewFile(file);
  }

  function previewFile(file) {
    filePreview.innerHTML = "";
    const previewContainer = document.createElement("div");
    previewContainer.classList.add("preview-container");
    if (file.type.startsWith("image/")) {
      const img = document.createElement("img");
      img.src = URL.createObjectURL(file);
      img.classList.add("preview-image");
      previewContainer.appendChild(img);
    } else {
      const fileDiv = document.createElement("div");
      fileDiv.innerHTML = `<i class="bi bi-file-earmark"></i> ${file.name}`;
      fileDiv.classList.add("preview-file");
      previewContainer.appendChild(fileDiv);
    }
    const removeButton = document.createElement("button");
    removeButton.classList.add("remove-file");
    removeButton.innerHTML = "×";
    removeButton.addEventListener("click", () => {
      uploadedFile = null;
      filePreview.innerHTML = "";
      fileInput.value = "";
    });

    previewContainer.appendChild(removeButton);
    filePreview.appendChild(previewContainer);
  }

  messageInput.addEventListener("input", function () {
    this.style.height = "auto";
    this.style.height = Math.min(this.scrollHeight, 150) + "px";
  });

  messageInput.addEventListener("keydown", (event) => {
    if (event.key === "Enter" && !event.shiftKey) {
      event.preventDefault();
      sendMessage();
    }
  });

  sendButton.addEventListener("click", sendMessage);

  window.approveRequest = approveRequest;
  window.rejectRequest = rejectRequest;
  window.loadJoinRequests = loadJoinRequests;

  connectWebSocket();
  loadChat();
});
