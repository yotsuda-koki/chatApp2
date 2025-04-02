var stompClient = null;

function connect() {
  var socket = new SockJS("/ws");
  stompClient = Stomp.over(socket);
  stompClient.reconnect_delay = 0;
  stompClient.connect(
    {},
    () => {
      reconnectAttempts = 0;
      stompClient.subscribe("/topic/status", function (statusUpdate) {
        updateFriendStatus(JSON.parse(statusUpdate.body));
      });
    },
    (error) => {
      console.warn(`WebSocket接続失敗 (${reconnectAttempts + 1}回目):`, error);
      if (reconnectAttempts < MAX_RECONNECT) {
        reconnectAttempts++;
        setTimeout(connect, 3000);
      } else {
        alert(
          "サーバーとの接続に繰り返し失敗しました。時間をおいて再度お試しください。"
        );
      }
    }
  );
}

function updateFriendStatus(update) {
  document.querySelectorAll(".friend-status").forEach((element) => {
    if (parseInt(element.closest("li").dataset.userid) === update.userId) {
      element.textContent = update.status;
      element.className = `friend-status ${
        update.status === "ONLINE"
          ? "status-online"
          : update.status === "AWAY"
          ? "status-away"
          : "status-offline"
      }`;
    }
  });
}

async function loadFriends() {
  fetch("/api/friends", {
    method: "GET",
    credentials: "include",
    headers: { "Content-Type": "application/json" },
  })
    .then((response) => response.json())
    .then((friends) => {
      const friendList = document.querySelector(".friend-list");
      friendList.innerHTML = "";
      friends.forEach((friend) => {
        const li = document.createElement("li");
        li.classList.add("friend-item", "list-group-item");
        li.setAttribute("data-userid", friend.id);
        li.innerHTML = `
          <img src="${sanitizeHTML(
            friend.profilePicture
          )}" alt="Avatar" class="friend-avatar rounded-circle me-3" width="50" height="50">
          <span class="friend-name">${sanitizeHTML(
            friend.displayName
          )} <span class="text-muted">#${sanitizeHTML(
          friend.username
        )}</span></span>
          <span class="friend-status ${
            friend.status === "ONLINE"
              ? "status-online"
              : friend.status === "AWAY"
              ? "status-away"
              : "status-offline"
          }">
            ${friend.status}
          </span>
          <div class="ms-3" role="group">
            <button class="btn btn-danger btn-sm" onclick="removeFriend(${
              friend.id
            })">Remove</button>
          </div>
          <div class="ms-3" role="group">
            <button class="btn btn-secondary btn-sm" onclick="blockUser(${
              friend.id
            })">Block</button>
          </div>
        `;
        friendList.appendChild(li);
      });
    })
    .catch((error) => console.error("Error loading friends:", error));
}

async function addFriend(username) {
  if (!username) {
    alert("Please enter a valid user ID");
    return;
  }
  fetch(`/api/friends/add`, {
    method: "POST",
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ username }),
  })
    .then((response) => response.text())
    .then((data) => {
      alert(
        data === "Already"
          ? "すでにフレンドです。"
          : data === "Pending"
          ? "フレンドリクエストを申請中です。"
          : data === "Blocked"
          ? "フレンドリクエストに失敗しました。"
          : data === "Sent"
          ? "フレンドリクエストを送信しました。"
          : "フレンドリクエストに失敗しました。"
      );
      if (data === "Sent") {
        loadFriends();
        document.getElementById("friendIdInput").value = "";
      }
    })
    .catch((error) => console.error("Error adding friend:", error));
}

async function removeFriend(userId) {
  if (!confirm("本当にフレンドを削除しますか？")) return;
  fetch(`/api/friends/remove/${userId}`, {
    method: "DELETE",
    credentials: "include",
  })
    .then((response) => {
      if (response.ok)
        document.querySelector(`[data-userid="${userId}"]`).remove();
      else alert("Failed to remove friend");
    })
    .catch((error) => console.error("Error removing friend:", error));
}

async function blockUser(userId) {
  if (!confirm("本当にこのユーザーをブロックしますか？")) return;
  fetch(`/api/friends/block/${userId}`, {
    method: "POST",
    credentials: "include",
  })
    .then((response) => {
      if (response.ok)
        document.querySelector(`[data-userid="${userId}"]`).remove();
      else alert("Failed to remove friend");
    })
    .catch((error) => console.error("Error blocking user:", error));
}

async function loadFriendRequests() {
  fetch(`/api/friends/requests`, {
    method: "GET",
    credentials: "include",
    headers: { "Content-Type": "application/json" },
  })
    .then((response) => response.json())
    .then((requests) => {
      const requestList = document.querySelector(".request-list");
      requestList.innerHTML = "";
      requests.forEach((request) => {
        const li = document.createElement("li");
        li.classList.add(
          "request-item",
          "list-group-item",
          "d-flex",
          "justify-content-between",
          "align-items-center"
        );
        li.setAttribute("data-requestId", request.id);
        li.innerHTML = `
          <span>${sanitizeHTML(
            request.displayName
          )} <span class="text-muted">#${sanitizeHTML(
          request.username
        )}</span></span>
          <div>
            <button class="btn btn-success me-2" onclick="acceptFriendRequest(${
              request.id
            })">
              <i class="bi bi-check-lg"></i>承認
            </button>
            <button class="btn btn-danger" onclick="rejectFriendRequest(${
              request.id
            })">
              <i class="bi bi-x-lg"></i>拒否
            </button>
          </div>
        `;
        requestList.appendChild(li);
      });
    })
    .catch((error) => console.log("Error loading friend requests" + error));
}

async function acceptFriendRequest(requestId) {
  fetch(`/api/friends/accept/${requestId}`, {
    method: "POST",
    credentials: "include",
  })
    .then((response) => {
      if (response.ok) {
        alert("フレンドリクエストを承認しました。");
        loadFriends();
        loadFriendRequests();
      } else {
        alert("フレンドリクエストの承認に失敗しました。");
      }
    })
    .catch((error) => console.error("Error accepting friend request:", error));
}

async function rejectFriendRequest(requestId) {
  fetch(`/api/friends/reject/${requestId}`, {
    method: "POST",
    credentials: "include",
  })
    .then((response) => {
      if (response.ok) {
        alert("フレンドリクエストを拒否しました。");
        loadFriendRequests();
      } else {
        alert("フレンドリクエストの拒否に失敗しました。");
      }
    })
    .catch((error) => console.error("Error rejecting friend request:", error));
}

window.onload = function () {
  connect();
  loadFriends();
  loadFriendRequests();
};
