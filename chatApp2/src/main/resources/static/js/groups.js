document.addEventListener("DOMContentLoaded", function () {
  const groupList = document.getElementById("groupList");
  const createGroupForm = document.getElementById("createGroupForm");
  const groupNameInput = document.getElementById("groupName");
  const isPublicCheckbox = document.getElementById("isPublic");
  const searchInput = document.getElementById("searchInput");
  const clearSearchButton = document.getElementById("clearSearch");
  let allGroups = [];

  const urlParams = new URLSearchParams(window.location.search);
  const error = urlParams.get("error");

  if (error) {
    let errorMessage = "";
    if (error === "pending") {
      errorMessage = "このグループへの参加リクエストは承認待ちです。";
    } else if (error === "rejected") {
      errorMessage = "このグループへの参加リクエストは拒否されました。";
    } else if (error === "not_allowed") {
      errorMessage = "このグループには参加できません。";
    } else if (error === "not_found") {
      errorMessage = "このグループは存在しません。";
    } else if (error === "internal_error") {
      errorMessage = "内部エラーが発生しました。";
    }

    if (errorMessage) {
      const errorDiv = document.createElement("div");
      errorDiv.classList.add(
        "alert",
        "alert-danger",
        "alert-dismissible",
        "fade",
        "show"
      );
      errorDiv.setAttribute("role", "alert");
      errorDiv.innerHTML = `
          <i class="bi bi-exclamation-triangle"></i> ${errorMessage}
          <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        `;

      document.getElementById("errorContainer").appendChild(errorDiv);

      setTimeout(() => {
        errorDiv.classList.remove("show");
        errorDiv.classList.add("fade");
        setTimeout(() => errorDiv.remove(), 500);
      }, 5000);

      window.history.replaceState({}, document.title, window.location.pathname);
    }
  }

  async function loadGroups() {
    try {
      const response = await fetch("/api/groups/");

      const groups = await response.json();
      allGroups = groups;
      displayGroups(groups);
    } catch (error) {
      console.error("Error:", error);
    }
  }

  async function displayGroups(groups) {
    groupList.innerHTML = "";

    const groupStatuses = await Promise.all(
      groups.map(async (group) => {
        const status = group.public ? "PUBLIC" : await getGroupStatus(group.id);
        return { ...group, status };
      })
    );

    groupStatuses.sort((a, b) => {
      const order = ["APPROVED", "NONE", "PENDING", "PUBLIC", "REJECTED"];
      return order.indexOf(a.status) - order.indexOf(b.status);
    });

    groupStatuses.forEach((group) => {
      const li = document.createElement("li");
      li.classList.add(
        "list-group-item",
        "d-flex",
        "justify-content-between",
        "align-items-center"
      );

      let buttonsHtml = "";
      let groupId = group.id;

      if (group.status === "PUBLIC") {
        buttonsHtml = `<button class="btn btn-sm btn-success disabled">公開</button>`;
      } else if (group.status === "APPROVED") {
        buttonsHtml = `<button class="btn btn-sm btn-primary disabled">参加済み</button>`;
      } else if (group.status === "PENDING") {
        buttonsHtml = `<button class="btn btn-sm btn-warning disabled">リクエスト済み</button>`;
      } else if (group.status === "REJECTED") {
        buttonsHtml = `<button class="btn btn-sm btn-danger disabled">リクエスト拒否済み</button>`;
      } else {
        buttonsHtml = `<button class="btn btn-sm btn-secondary" onclick="setupJoinRequestButtons(this, ${groupId})">参加リクエスト</button>`;
      }

      li.innerHTML = `<a href="/groups/${group.id}">${sanitizeHTML(
        group.name
      )}</a> ${buttonsHtml}`;
      groupList.appendChild(li);
    });
  }

  function setupJoinRequestButtons(button, groupId) {
    try {
      fetch(`/api/member/${groupId}/request-join`, {
        method: "POST",
        credentials: "include",
      })
        .then(() => {
          button.textContent = "リクエスト済み";
          button.classList.remove("btn-secondary");
          button.classList.add("btn-warning", "disabled");
          loadGroups();
        })
        .catch((error) => {
          console.error("エラー:", error);
        });
    } catch (error) {
      console.error("エラー:", error);
    }
  }

  async function getGroupStatus(groupId) {
    try {
      const response = await fetch(`/api/member/${groupId}/status`, {
        credentials: "include",
      });
      return await response.text();
    } catch (error) {
      console.error("エラー:", error);
      return "NONE";
    }
  }

  createGroupForm.addEventListener("submit", function (event) {
    event.preventDefault();

    const name = groupNameInput.value.trim();
    const isPublic = isPublicCheckbox.checked;
    const groupNameError = document.getElementById("groupNameError");
    groupNameError.textContent = "";
    groupNameInput.classList.remove("is-invalid");
    if (name.length < 3 || name.length > 50) {
      groupNameError.textContent =
        "グループ名は3文字以上50文字以内で入力してください";
      groupNameInput.classList.add("is-invalid");
      return;
    }

    fetch("/api/groups/create", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ name, isPublic }),
    })
      .then((response) => response.json())
      .then(() => {
        loadGroups();
        createGroupForm.reset();

        const modal = bootstrap.Modal.getInstance(
          document.getElementById("createGroupModal")
        );
        modal.hide();
      })
      .catch((error) => console.log("error"));
  });

  searchInput.addEventListener("input", function () {
    const query = searchInput.value.toLowerCase();
    const filteredGroups = allGroups.filter((group) =>
      group.name.toLowerCase().includes(query)
    );
    displayGroups(filteredGroups);
  });

  clearSearchButton.addEventListener("click", function () {
    searchInput.value = "";
    displayGroups(allGroups);
  });

  window.setupJoinRequestButtons = setupJoinRequestButtons;
  window.loadGroups = loadGroups;

  loadGroups();
});
