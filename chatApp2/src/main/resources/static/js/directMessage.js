document.addEventListener("DOMContentLoaded", async function () {
  const fileInput = document.getElementById("fileInput");
  const fileButton = document.getElementById("fileButton");
  const messageInput = document.getElementById("messageInput");
  const sendButton = document.getElementById("sendButton");
  const dropArea = document.getElementById("dropArea");
  const filePreview = document.getElementById("filePreview");
  const friendList = document.getElementById("friendList");
  const chatBox = document.getElementById("chatBox");
  const chatContainer = document.getElementById("chatContainer");
  const chatTitle = document.getElementById("chatTitle");

  let stompClient = null;
  let uploadedFile = null;
  let currentReceiverId = null;

  try {
    const hashedEmail = await getHashedIdentifier();
    initializeWebSocket(hashedEmail);
  } catch (error) {
    console.log("WebSocket接続エラー");
  }

  async function getHashedIdentifier() {
    try {
      const response = await fetch("/api/security/hashed-identifier", {
        credentials: "include",
      });
      return await response.text();
    } catch (error) {
      console.log("Failed to get hashed identifier");
      throw error;
    }
  }

  function initializeWebSocket(hashedEmail) {
    const socket = new SockJS("/ws");
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function () {
      console.log("WebSocket接続しました。");

      stompClient.subscribe(
        `/user/${hashedEmail}/topic/directMessages`,
        function (message) {
          const data = JSON.parse(message.body);
          if (currentReceiverId === data.senderId) {
            loadDirectMessage(data.senderId);
          }
        }
      );

      stompClient.subscribe("/user/queue/readReceipts", function (message) {
        const data = JSON.parse(message.body);
        markAsRead(data.messageId);
      });
    });
  }

  async function loadFriends() {
    try {
      const response = await fetch("/api/friends", { credentials: "include" });
      const friends = await response.json();
      friendList.innerHTML = "";
      friends.forEach((friend) => {
        const friendItem = document.createElement("li");
        friendItem.classList.add(
          "list-group-item",
          "d-flex",
          "justify-content-between",
          "align-items-center"
        );
        friendItem.innerHTML = `
                        <img src="${sanitizeHTML(
                          friend.profilePicture
                        )}" alt="Avatar" class="friend-avatar rounded-circle me-3" width="50" height="50">
                        <div>
                            <span class="fw-bold">${sanitizeHTML(
                              friend.displayName
                            )}</span>
                        </div>
                        <span class="badge bg-danger">${sanitizeHTML(
                          friend.unreadMessages > 0 ? friend.unreadMessages : ""
                        )}</span>
                    `;
        friendItem.addEventListener("click", () =>
          openChat(friend.id, friend.username)
        );
        friendList.appendChild(friendItem);
      });
    } catch (error) {
      console.error("フレンドリストの取得に失敗しました。");
    }
  }

  async function openChat(receiverId, receiverName) {
    if (currentReceiverId == receiverId) {
      console.log("すでに開いています。");
      return;
    }
    chatContainer.style.visibility = "visible";
    currentReceiverId = receiverId;
    chatTitle.textContent = receiverName;
    chatBox.innerHTML = "";
    await loadDirectMessage(currentReceiverId);
  }

  async function loadDirectMessage(receiverId) {
    try {
      const response = await fetch(`/api/direct/${receiverId}`, {
        credentials: "include",
      });
      const messages = await response.json();
      chatBox.innerHTML = "";
      messages.forEach((message) => {
        const type = message.receiverId === currentReceiverId ? "From" : "To";
        displayMessage(message, type);
        if (!message.read && type === "To") {
          markAsRead(message.id);
        }
      });
    } catch (error) {
      console.error("チャット履歴の習得に失敗しました。", error);
    }
  }

  async function markAsRead(messageId) {
    fetch(`api/direct/${messageId}/read`, {
      method: "POST",
      credentials: "include",
      headers: { "Content-Type": "application/json" },
    })
      .then((response) => {
        if (response.ok) {
          updateReadStatusUI(messageId);
        } else {
          console.log("既読処理に失敗しました。");
        }
      })
      .catch(console.error("既読処理のエラー"));
  }

  function updateReadStatusUI(messageId) {
    const unreadBadge = document.querySelector(
      `.unread-badge[data-id="${messageId}"]`
    );
    if (unreadBadge) {
      unreadBadge.innerHTML = `<i class="bi bi-check-all text-info"></i> 既読`;
      unreadBadge.classList.remove("unread-badge");
      unreadBadge.classList.add("read-badge");
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
      receiverId: currentReceiverId,
      content: content,
      timestamp: new Date().toISOString(),
    };

    stompClient.send(
      "/app/direct.sendMessage",
      {},
      JSON.stringify(messageData)
    );

    setTimeout(() => {
      loadDirectMessage(currentReceiverId);
    }, 300);

    messageInput.value = "";
    filePreview.innerHTML = "";
    uploadedFile = null;
    fileInput.value = "";
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

  function displayMessage(messageData, type) {
    const messageItem = document.createElement("div");
    messageItem.classList.add(
      "d-flex",
      "mb-2",
      type === "From" ? "justify-content-end" : "justify-content-start"
    );
    let contentHtml;
    if (messageData.content.startsWith("/api/files/download/")) {
      const fileName = messageData.content.split("/").pop();
      const linkClass = type === "From" ? "text-white" : "text-primary";
      contentHtml = `
      <a href="${sanitizeHTML(messageData.content)}" class="${sanitizeHTML(
        linkClass
      )}" download style="text-decoration: underline;">
        ${sanitizeHTML(fileName)}
      </a>
    `;
    } else {
      contentHtml = `<p class="mb-1">${sanitizeHTML(messageData.content)}</p>`;
    }

    messageItem.innerHTML = `
    <div class="card ${
      type === "From" ? "bg-primary text-white" : "bg-light text-dark"
    } p-2 rounded shadow-sm" style="max-width: 75%;">
      ${contentHtml}
      <small class="${
        type === "From" ? "text-light" : "text-muted"
      }">${new Date(messageData.timestamp).toLocaleString()}</small>
    </div>
  `;
    chatBox.appendChild(messageItem);
    chatBox.scrollTop = chatBox.scrollHeight;
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

  loadFriends();
});
