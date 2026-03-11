(function () {
    const apiRoot = "/api";

    function showAlert(node, message, level) {
        if (!node) {
            return;
        }
        if (!message) {
            node.className = "alert d-none";
            node.textContent = "";
            return;
        }
        node.className = "alert alert-" + level;
        node.textContent = message;
    }

    async function readPayload(response) {
        if (response.status === 204) {
            return null;
        }
        const contentType = response.headers.get("content-type") || "";
        if (contentType.includes("application/json")) {
            return response.json();
        }
        const text = await response.text();
        return text || null;
    }

    async function request(url, options) {
        const response = await fetch(url, options);
        const payload = await readPayload(response);
        if (!response.ok) {
            if (typeof payload === "string" && payload.trim()) {
                throw new Error(payload);
            }
            if (payload && payload.message) {
                throw new Error(payload.message);
            }
            throw new Error("Ошибка запроса: " + response.status);
        }
        return payload;
    }

    function normalizeDirectoryPath(path) {
        if (!path || !path.trim()) {
            return "/";
        }
        let normalized = path.trim().replace(/\\/g, "/");
        if (normalized === "/") {
            return "/";
        }
        normalized = normalized.replace(/^\/+/, "");
        if (!normalized) {
            return "/";
        }
        return normalized.endsWith("/") ? normalized : normalized + "/";
    }

    function normalizeResourcePath(path) {
        if (!path || !path.trim()) {
            return "/";
        }
        let normalized = path.trim().replace(/\\/g, "/");
        if (normalized === "/") {
            return "/";
        }
        normalized = normalized.replace(/^\/+/, "");
        return normalized || "/";
    }

    function normalizeTargetPath(path, resourceType) {
        let normalized = normalizeResourcePath(path);
        if (resourceType === "DIRECTORY") {
            if (normalized !== "/" && !normalized.endsWith("/")) {
                normalized += "/";
            }
            return normalized;
        }
        if (normalized.endsWith("/") && normalized !== "/") {
            normalized = normalized.slice(0, -1);
        }
        return normalized;
    }

    function joinDirectoryPath(currentDirectory, newFolderName) {
        const cleanName = (newFolderName || "")
            .trim()
            .replace(/\\/g, "/")
            .replace(/^\/+/, "")
            .replace(/\/+$/, "");
        if (!cleanName) {
            return "";
        }
        const prefix = normalizeDirectoryPath(currentDirectory);
        return (prefix === "/" ? "" : prefix) + cleanName + "/";
    }

    function resourcePath(resource) {
        const base = resource.path || "";
        const name = resource.name || "";
        if (resource.type === "DIRECTORY") {
            const directoryPath = base + name;
            return directoryPath.endsWith("/") ? directoryPath : directoryPath + "/";
        }
        return base + name;
    }

    function resourceName(resource) {
        return resource.type === "DIRECTORY" ? (resource.name || "") + "/" : resource.name || "";
    }

    function formatSize(size) {
        if (typeof size !== "number" || size < 0) {
            return "-";
        }
        if (size < 1024) {
            return size + " B";
        }
        if (size < 1024 * 1024) {
            return (size / 1024).toFixed(1) + " KB";
        }
        if (size < 1024 * 1024 * 1024) {
            return (size / (1024 * 1024)).toFixed(1) + " MB";
        }
        return (size / (1024 * 1024 * 1024)).toFixed(1) + " GB";
    }

    function initStoragePage() {
        const app = document.getElementById("storage-app");
        if (!app) {
            return;
        }

        const listNode = document.getElementById("resource-list");
        const emptyDirectoryNode = document.getElementById("empty-directory");
        const breadcrumbNode = document.getElementById("path-breadcrumb");
        const currentPathBadge = document.getElementById("current-path-badge");
        const directoryStatus = document.getElementById("directory-status");
        const searchResultsNode = document.getElementById("search-results");
        const searchEmptyNode = document.getElementById("search-empty");
        const clearSearchButton = document.getElementById("clear-search-button");
        const uploadFileInput = document.getElementById("upload-file-input");
        const addFileButton = document.getElementById("add-file-button");
        const refreshDirectoryButton = document.getElementById("refresh-directory-button");
        const goUpButton = document.getElementById("go-up-button");

        const detailsModalElement = document.getElementById("resource-details-modal");
        const detailsModal = detailsModalElement ? new bootstrap.Modal(detailsModalElement) : null;
        const detailsAlert = document.getElementById("resource-modal-alert");
        const detailsName = document.getElementById("resource-detail-name");
        const detailsPath = document.getElementById("resource-detail-path");
        const detailsType = document.getElementById("resource-detail-type");
        const detailsSize = document.getElementById("resource-detail-size");
        const moveTargetInput = document.getElementById("resource-move-target");
        const downloadResourceButton = document.getElementById("resource-download-button");
        const deleteResourceButton = document.getElementById("resource-delete-button");
        const moveResourceButton = document.getElementById("resource-move-button");

        const createDirectoryModalElement = document.getElementById("create-directory-modal");
        const createDirectoryModal = createDirectoryModalElement ? new bootstrap.Modal(createDirectoryModalElement) : null;
        const createDirectoryAlert = document.getElementById("create-directory-alert");
        const newDirectoryNameInput = document.getElementById("new-directory-name");
        const createDirectoryConfirmButton = document.getElementById("create-directory-confirm");

        let currentPath = normalizeDirectoryPath(app.dataset.currentPath || "/");
        let selectedResource = null;

        function updateUrlPath() {
            const url = new URL(window.location.href);
            if (currentPath === "/") {
                url.searchParams.delete("path");
            } else {
                url.searchParams.set("path", currentPath);
            }
            window.history.replaceState({}, "", url.toString());
        }

        function parentDirectory(path) {
            const normalized = normalizeDirectoryPath(path);
            if (normalized === "/") {
                return "/";
            }
            const parts = normalized.split("/").filter(Boolean);
            parts.pop();
            return parts.length ? parts.join("/") + "/" : "/";
        }

        function renderBreadcrumb() {
            breadcrumbNode.innerHTML = "";

            const rootItem = document.createElement("li");
            rootItem.className = "breadcrumb-item";
            const rootButton = document.createElement("button");
            rootButton.type = "button";
            rootButton.className = "btn btn-link p-0";
            rootButton.textContent = "root";
            rootButton.disabled = currentPath === "/";
            rootButton.addEventListener("click", function () {
                loadDirectory("/");
            });
            rootItem.appendChild(rootButton);
            breadcrumbNode.appendChild(rootItem);

            if (currentPath === "/") {
                rootItem.classList.add("active");
                rootItem.setAttribute("aria-current", "page");
                return;
            }

            const segments = currentPath.split("/").filter(Boolean);
            let pathAccumulator = "";
            segments.forEach(function (segment, index) {
                pathAccumulator += segment + "/";
                const item = document.createElement("li");
                item.className = "breadcrumb-item";
                if (index === segments.length - 1) {
                    item.classList.add("active");
                    item.setAttribute("aria-current", "page");
                    item.textContent = segment;
                } else {
                    const button = document.createElement("button");
                    button.type = "button";
                    button.className = "btn btn-link p-0";
                    button.textContent = segment;
                    const targetPath = pathAccumulator;
                    button.addEventListener("click", function () {
                        loadDirectory(targetPath);
                    });
                    item.appendChild(button);
                }
                breadcrumbNode.appendChild(item);
            });
        }

        function fillDetails(resource) {
            selectedResource = resource;
            detailsName.textContent = resource.name || "-";
            detailsPath.textContent = resourcePath(resource) || "-";
            detailsType.textContent = resource.type === "DIRECTORY" ? "Директория" : "Файл";
            detailsSize.textContent = resource.type === "FILE" ? formatSize(resource.size) : "-";
            moveTargetInput.value = "";
            showAlert(detailsAlert, "", "");
        }

        async function openDetails(resource) {
            try {
                const path = resourcePath(resource);
                const payload = await request(apiRoot + "/resource?path=" + encodeURIComponent(path), { method: "GET" });
                fillDetails(payload);
                detailsModal.show();
            } catch (error) {
                showAlert(directoryStatus, error.message, "danger");
            }
        }

        function buildActionButton(text, className, clickHandler) {
            const button = document.createElement("button");
            button.type = "button";
            button.className = className;
            button.textContent = text;
            button.addEventListener("click", clickHandler);
            return button;
        }

        function renderDirectory(resources) {
            listNode.innerHTML = "";
            const sorted = (resources || []).slice().sort(function (a, b) {
                if (a.type !== b.type) {
                    return a.type === "DIRECTORY" ? -1 : 1;
                }
                return resourceName(a).localeCompare(resourceName(b), "ru");
            });

            emptyDirectoryNode.classList.toggle("d-none", sorted.length > 0);
            if (!sorted.length) {
                return;
            }

            sorted.forEach(function (resource) {
                const row = document.createElement("tr");

                const nameCell = document.createElement("td");
                const nameButton = document.createElement("button");
                nameButton.type = "button";
                nameButton.className = "btn btn-link p-0 text-decoration-none resource-name";
                nameButton.textContent = (resource.type === "DIRECTORY" ? "📁 " : "📄 ") + resourceName(resource);
                nameButton.addEventListener("click", function () {
                    if (resource.type === "DIRECTORY") {
                        loadDirectory(resourcePath(resource));
                        return;
                    }
                    openDetails(resource);
                });
                nameCell.appendChild(nameButton);

                const typeCell = document.createElement("td");
                typeCell.textContent = resource.type === "DIRECTORY" ? "Директория" : "Файл";

                const sizeCell = document.createElement("td");
                sizeCell.className = "text-end";
                sizeCell.textContent = resource.type === "FILE" ? formatSize(resource.size) : "-";

                const actionsCell = document.createElement("td");
                actionsCell.className = "text-end";
                actionsCell.appendChild(
                    buildActionButton("Детали", "btn btn-outline-secondary btn-sm", function () {
                        openDetails(resource);
                    })
                );

                row.appendChild(nameCell);
                row.appendChild(typeCell);
                row.appendChild(sizeCell);
                row.appendChild(actionsCell);
                listNode.appendChild(row);
            });
        }

        function renderSearchResults(resources) {
            searchResultsNode.innerHTML = "";
            const hasResults = Array.isArray(resources) && resources.length > 0;
            searchResultsNode.classList.toggle("d-none", !hasResults);
            searchEmptyNode.classList.toggle("d-none", hasResults);
            clearSearchButton.classList.toggle("d-none", !hasResults);

            if (!hasResults) {
                return;
            }

            resources.forEach(function (resource) {
                const item = document.createElement("li");
                item.className = "list-group-item d-flex justify-content-between align-items-center gap-3";

                const textBox = document.createElement("div");
                const title = document.createElement("div");
                title.className = "fw-semibold";
                title.textContent = resourceName(resource);
                const pathText = document.createElement("div");
                pathText.className = "small text-muted";
                pathText.textContent = resourcePath(resource);
                textBox.appendChild(title);
                textBox.appendChild(pathText);

                const actions = document.createElement("div");
                actions.className = "d-flex gap-2";

                if (resource.type === "DIRECTORY") {
                    actions.appendChild(
                        buildActionButton("Открыть", "btn btn-outline-secondary btn-sm", function () {
                            loadDirectory(resourcePath(resource));
                        })
                    );
                }

                actions.appendChild(
                    buildActionButton("Детали", "btn btn-outline-secondary btn-sm", function () {
                        openDetails(resource);
                    })
                );

                item.appendChild(textBox);
                item.appendChild(actions);
                searchResultsNode.appendChild(item);
            });
        }

        async function loadDirectory(path) {
            const normalizedPath = normalizeDirectoryPath(path);
            try {
                const payload = await request(apiRoot + "/directory?path=" + encodeURIComponent(normalizedPath), { method: "GET" });
                currentPath = normalizedPath;
                currentPathBadge.textContent = currentPath;
                renderBreadcrumb();
                renderDirectory(payload);
                updateUrlPath();
                showAlert(directoryStatus, "", "");
            } catch (error) {
                listNode.innerHTML = "";
                emptyDirectoryNode.classList.remove("d-none");
                showAlert(directoryStatus, error.message, "danger");
            }
        }

        refreshDirectoryButton.addEventListener("click", function () {
            loadDirectory(currentPath);
        });

        goUpButton.addEventListener("click", function () {
            loadDirectory(parentDirectory(currentPath));
        });

        addFileButton.addEventListener("click", function () {
            uploadFileInput.click();
        });

        uploadFileInput.addEventListener("change", function () {
            if (!uploadFileInput.files || !uploadFileInput.files.length) {
                return;
            }
            const body = new FormData();
            body.append("file", uploadFileInput.files[0]);
            request(apiRoot + "/resource?path=" + encodeURIComponent(currentPath), {
                method: "POST",
                body: body
            })
                .then(function () {
                    showAlert(directoryStatus, "Файл загружен", "success");
                    loadDirectory(currentPath);
                })
                .catch(function (error) {
                    showAlert(directoryStatus, error.message, "danger");
                })
                .finally(function () {
                    uploadFileInput.value = "";
                });
        });

        createDirectoryConfirmButton.addEventListener("click", function () {
            const folderName = newDirectoryNameInput.value;
            const targetPath = joinDirectoryPath(currentPath, folderName);
            if (!targetPath) {
                showAlert(createDirectoryAlert, "Введите название папки", "warning");
                return;
            }
            request(apiRoot + "/directory?path=" + encodeURIComponent(targetPath), { method: "POST" })
                .then(function () {
                    showAlert(createDirectoryAlert, "", "");
                    newDirectoryNameInput.value = "";
                    createDirectoryModal.hide();
                    showAlert(directoryStatus, "Директория создана", "success");
                    loadDirectory(currentPath);
                })
                .catch(function (error) {
                    showAlert(createDirectoryAlert, error.message, "danger");
                });
        });

        if (createDirectoryModalElement) {
            createDirectoryModalElement.addEventListener("hidden.bs.modal", function () {
                newDirectoryNameInput.value = "";
                showAlert(createDirectoryAlert, "", "");
            });
        }

        downloadResourceButton.addEventListener("click", function () {
            if (!selectedResource) {
                return;
            }
            window.location.href = apiRoot + "/resource/download?path=" + encodeURIComponent(resourcePath(selectedResource));
        });

        deleteResourceButton.addEventListener("click", function () {
            if (!selectedResource) {
                return;
            }
            const path = resourcePath(selectedResource);
            if (!window.confirm("Удалить ресурс " + path + "?")) {
                return;
            }
            request(apiRoot + "/resource?path=" + encodeURIComponent(path), { method: "DELETE" })
                .then(function () {
                    detailsModal.hide();
                    showAlert(directoryStatus, "Ресурс удалён", "success");
                    loadDirectory(currentPath);
                })
                .catch(function (error) {
                    showAlert(detailsAlert, error.message, "danger");
                });
        });

        moveResourceButton.addEventListener("click", function () {
            if (!selectedResource) {
                return;
            }
            const target = moveTargetInput.value;
            if (!target.trim()) {
                showAlert(detailsAlert, "Укажите новый путь для перемещения", "warning");
                return;
            }
            const fromPath = resourcePath(selectedResource);
            const toPath = normalizeTargetPath(target, selectedResource.type);
            request(
                apiRoot + "/resource/move?from=" + encodeURIComponent(fromPath) + "&to=" + encodeURIComponent(toPath),
                { method: "GET" }
            )
                .then(function () {
                    detailsModal.hide();
                    showAlert(directoryStatus, "Ресурс перемещён", "success");
                    loadDirectory(currentPath);
                })
                .catch(function (error) {
                    showAlert(detailsAlert, error.message, "danger");
                });
        });

        const searchForm = document.getElementById("search-resource-form");
        searchForm.addEventListener("submit", function (event) {
            event.preventDefault();
            const query = searchForm.query.value.trim();
            request(apiRoot + "/resource/search?query=" + encodeURIComponent(query), { method: "GET" })
                .then(function (payload) {
                    renderSearchResults(payload || []);
                })
                .catch(function (error) {
                    showAlert(directoryStatus, error.message, "danger");
                });
        });

        clearSearchButton.addEventListener("click", function () {
            searchResultsNode.innerHTML = "";
            searchResultsNode.classList.add("d-none");
            searchEmptyNode.classList.add("d-none");
            clearSearchButton.classList.add("d-none");
            const searchInput = document.getElementById("search-query");
            searchInput.value = "";
        });

        renderBreadcrumb();
        loadDirectory(currentPath);
    }

    function initSettingsPage() {
        const settingsApp = document.getElementById("settings-app");
        if (!settingsApp) {
            return;
        }

        const statusNode = document.getElementById("settings-status");
        const profileUsername = document.getElementById("profile-username");
        const loadMeButton = document.getElementById("load-me-button");
        const changePasswordForm = document.getElementById("change-password-form");

        if (loadMeButton && profileUsername) {
            loadMeButton.addEventListener("click", function () {
                request(apiRoot + "/user/me", { method: "GET" })
                    .then(function (payload) {
                        profileUsername.textContent = payload.username || "-";
                        showAlert(statusNode, "Данные профиля обновлены", "success");
                    })
                    .catch(function (error) {
                        showAlert(statusNode, error.message, "danger");
                    });
            });
        }

        if (!changePasswordForm) {
            return;
        }

        changePasswordForm.addEventListener("submit", function (event) {
            event.preventDefault();
            const params = new URLSearchParams();
            params.set("oldPassword", changePasswordForm.oldPassword.value);
            params.set("newPassword", changePasswordForm.newPassword.value);

            request(apiRoot + "/user/change-password", {
                method: "PUT",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
                },
                body: params.toString()
            })
                .then(function () {
                    showAlert(statusNode, "Пароль успешно изменён", "success");
                    changePasswordForm.reset();
                })
                .catch(function (error) {
                    showAlert(statusNode, error.message, "danger");
                });
        });
    }

    initStoragePage();
    initSettingsPage();
})();
