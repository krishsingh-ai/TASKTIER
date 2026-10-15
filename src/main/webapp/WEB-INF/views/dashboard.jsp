<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.tasktier.model.Task" %>
<%@ page import="com.tasktier.model.Category" %>
<%@ page import="com.tasktier.model.Priority" %>
<%@ page import="com.tasktier.model.TaskStatus" %>
<%@ page import="com.tasktier.model.UserRole" %>
<%@ page import="com.tasktier.util.DateUtil" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard — TaskTier</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/static/style.css">
</head>
<body>
    <%
        String displayName = (String) session.getAttribute("displayName");
        UserRole userRole = (UserRole) session.getAttribute("userRole");
        boolean isAdmin = userRole == UserRole.ADMIN;

        @SuppressWarnings("unchecked")
        List<Task> tasks = (List<Task>) request.getAttribute("tasks");

        @SuppressWarnings("unchecked")
        List<Category> categories = (List<Category>) request.getAttribute("categories");

        Long totalTasks = (Long) request.getAttribute("totalTasks");
        Long completedTasks = (Long) request.getAttribute("completedTasks");
        Long pendingTasks = (Long) request.getAttribute("pendingTasks");
        Long criticalTasks = (Long) request.getAttribute("criticalTasks");
        java.time.LocalDate today = java.time.LocalDate.now();
        int currentHour = java.time.LocalTime.now().getHour();
        String timeGreeting = (currentHour < 12) ? "Good morning" : (currentHour < 18) ? "Good afternoon" : "Good evening";
        String userFirstName = (displayName != null && !displayName.trim().isEmpty()) ? displayName.trim().split("\\s+")[0] : "there";
    %>

    <header class="app-header">
        <div class="header-container">
            <div class="header-left">
                <a href="<%= request.getContextPath() %>/dashboard" class="brand-logo">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" class="brand-mark"><rect x="3" y="3" width="7" height="7"></rect><rect x="14" y="3" width="7" height="7"></rect><rect x="14" y="14" width="7" height="7"></rect><rect x="3" y="14" width="7" height="7"></rect></svg>
                    <span>TaskTier</span>
                </a>
                <nav class="nav-links" id="primary-nav">
                    <a href="<%= request.getContextPath() %>/dashboard" class="nav-item active">Dashboard</a>
                    <a href="<%= request.getContextPath() %>/progress" class="nav-item">Analytics &amp; Progress</a>
                    <% if (isAdmin) { %>
                        <a href="<%= request.getContextPath() %>/admin" class="nav-item">Admin Panel</a>
                    <% } %>
                </nav>
            </div>
            <div class="header-right">
                <button type="button" id="theme-toggle" class="theme-toggle-btn" aria-label="Toggle theme" title="Toggle theme">
                    <svg class="sun-icon" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="5"></circle><line x1="12" y1="1" x2="12" y2="3"></line><line x1="12" y1="21" x2="12" y2="23"></line><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"></line><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"></line><line x1="1" y1="12" x2="3" y2="12"></line><line x1="21" y1="12" x2="23" y2="12"></line><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"></line><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"></line></svg>
                    <svg class="moon-icon" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="display: none;"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path></svg>
                </button>
                <div class="header-divider"></div>
                <div class="user-pill">
                    <span class="user-avatar-circle"><%= displayName != null && !displayName.isEmpty() ? displayName.substring(0, 1).toUpperCase() : "U" %></span>
                    <span class="user-pill-name"><%= displayName != null ? displayName : "User" %></span>
                </div>
                <a href="<%= request.getContextPath() %>/logout" class="btn-signout-text">Sign Out</a>
                <button type="button" id="nav-toggle" class="nav-hamburger" aria-label="Toggle navigation menu" aria-expanded="false">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="3" y1="12" x2="21" y2="12"></line><line x1="3" y1="6" x2="21" y2="6"></line><line x1="3" y1="18" x2="21" y2="18"></line></svg>
                </button>
            </div>
        </div>
    </header>

    <main class="main-container">
        <!-- Greeting Section -->
        <div class="page-greeting">
            <h1 class="greeting-title"><%= timeGreeting %>, <%= userFirstName %></h1>
            <p class="greeting-sub">Here's what matters today.</p>
        </div>

        <!-- Tinted Metric Cards -->
        <div class="grid-cols-4 stat-grid">
            <div class="stat-card stat-card-neutral">
                <div class="stat-top">
                    <span class="stat-dot dot-slate"></span>
                    <span class="stat-title">Total Tasks</span>
                </div>
                <div class="stat-value tabular-nums" data-target="<%= totalTasks != null ? totalTasks : 0 %>"><%= totalTasks != null ? totalTasks : 0 %></div>
                <div class="stat-sub">Across all tiers</div>
            </div>
            <div class="stat-card stat-card-critical">
                <div class="stat-top">
                    <span class="stat-dot dot-critical"></span>
                    <span class="stat-title">Critical</span>
                </div>
                <div class="stat-value tabular-nums" data-target="<%= criticalTasks != null ? criticalTasks : 0 %>"><%= criticalTasks != null ? criticalTasks : 0 %></div>
                <div class="stat-sub">Requires immediate action</div>
            </div>
            <div class="stat-card stat-card-pending">
                <div class="stat-top">
                    <span class="stat-dot dot-amber"></span>
                    <span class="stat-title">Pending</span>
                </div>
                <div class="stat-value tabular-nums" data-target="<%= pendingTasks != null ? pendingTasks : 0 %>"><%= pendingTasks != null ? pendingTasks : 0 %></div>
                <div class="stat-sub">In queue or in progress</div>
            </div>
            <div class="stat-card stat-card-completed">
                <div class="stat-top">
                    <span class="stat-dot dot-emerald"></span>
                    <span class="stat-title">Completed</span>
                </div>
                <div class="stat-value tabular-nums" data-target="<%= completedTasks != null ? completedTasks : 0 %>"><%= completedTasks != null ? completedTasks : 0 %></div>
                <div class="stat-sub">Successfully finished</div>
            </div>
        </div>

        <div class="grid-main-side">
            <!-- Left: Task Prioritization List -->
            <section>
                <div class="card">
                    <div class="card-header">
                        <h2>Prioritized Tasks (Ranked by Score)</h2>
                        <div style="font-size: 13px; color: var(--text-muted);">
                            Highest urgency &amp; importance first
                        </div>
                    </div>

                    <!-- Client-side Search and Filter Bar -->
                    <div style="display: flex; gap: 10px; margin-bottom: 20px;">
                        <input type="text" id="filter-search" class="form-control" placeholder="Search tasks..." style="flex: 2;">
                        <select id="filter-tier" class="form-control" style="flex: 1;">
                            <option value="">All Tiers</option>
                            <option value="Critical">🔴 Critical (80+)</option>
                            <option value="High">🟠 High (60-79)</option>
                            <option value="Medium">🟡 Medium (40-59)</option>
                            <option value="Low">🟢 Low (&lt;40)</option>
                        </select>
                        <select id="filter-status" class="form-control" style="flex: 1;">
                            <option value="">All Statuses</option>
                            <option value="PENDING">Pending</option>
                            <option value="IN_PROGRESS">In Progress</option>
                            <option value="COMPLETED">Completed</option>
                            <option value="OVERDUE">Overdue</option>
                        </select>
                    </div>

                    <!-- Task List Items -->
                    <div id="task-list-container">
                        <% if (tasks == null || tasks.isEmpty()) { %>
                            <div class="empty-state">
                                <div class="empty-illustration">
                                    <svg width="120" height="120" viewBox="0 0 120 120" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <rect x="25" y="20" width="70" height="80" rx="8" stroke="currentColor" stroke-width="2" stroke-dasharray="4 4" opacity="0.4"/>
                                        <line x1="38" y1="36" x2="68" y2="36" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" opacity="0.6"/>
                                        <line x1="38" y1="50" x2="82" y2="50" stroke="currentColor" stroke-width="2" stroke-linecap="round" opacity="0.4"/>
                                        <line x1="38" y1="64" x2="74" y2="64" stroke="currentColor" stroke-width="2" stroke-linecap="round" opacity="0.4"/>
                                        <line x1="38" y1="78" x2="60" y2="78" stroke="currentColor" stroke-width="2" stroke-linecap="round" opacity="0.4"/>
                                        <circle cx="78" cy="78" r="18" fill="var(--surface)" stroke="var(--accent)" stroke-width="2"/>
                                        <path d="M72 78L76 82L85 73" stroke="var(--accent)" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                                    </svg>
                                </div>
                                <div class="empty-headline">All clear for now</div>
                                <div class="empty-subtext">Create your first task to get started.</div>
                                <button type="button" class="btn btn-primary" id="btn-focus-create">+ Create Task</button>
                            </div>
                        <% } else { %>
                            <% for (Task task : tasks) { %>
                                <div class="task-item"
                                     data-tier="<%= task.getTier() %>"
                                     data-status="<%= task.getStatus().name() %>"
                                     data-title="<%= task.getTitle() %>">

                                    <div class="task-main">
                                        <div class="task-top-row">
                                            <div style="display: flex; align-items: center; gap: 8px;">
                                                <h3 class="task-title"><%= task.getTitle() %></h3>
                                                <span class="tier-pill tier-pill-<%= task.getTier().toLowerCase() %>"><%= task.getTier() %></span>
                                                <% if (task.getCategoryName() != null) { %>
                                                    <span class="category-chip" style="color: <%= task.getCategoryColor() != null ? task.getCategoryColor() : "inherit" %>;">
                                                        <%= task.getCategoryName() %>
                                                    </span>
                                                <% } %>
                                            </div>
                                            <div class="task-score-chip">
                                                <span>Score</span>
                                                <span class="score-num"><%= task.getPriorityScore() %></span>
                                            </div>
                                        </div>

                                        <% if (task.getDescription() != null && !task.getDescription().trim().isEmpty()) { %>
                                            <div class="task-desc"><%= task.getDescription() %></div>
                                        <% } %>

                                        <div class="task-bottom-row">
                                            <div class="task-chips">
                                                <span class="chip-status chip-status-<%= task.getStatus().name().toLowerCase() %>">
                                                    <%= task.getStatus().getDisplayName() %>
                                                </span>
                                                <% if (task.getDeadline() != null) { %>
                                                    <span class="chip-deadline">
                                                        <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg>
                                                        <%= DateUtil.formatForDisplay(task.getDeadline()) %>
                                                    </span>
                                                <% } %>
                                                <% if (task.getEstimatedHours() != null) { %>
                                                    <span class="chip-hours"><%= task.getEstimatedHours() %> hrs</span>
                                                <% } %>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="task-actions">
                                        <a href="<%= request.getContextPath() %>/tasks?id=<%= task.getId() %>"
                                           class="btn btn-secondary btn-sm" title="Task Details &amp; Timer">
                                            ⏱ Log Time
                                        </a>

                                        <% if (task.getStatus() != TaskStatus.COMPLETED) { %>
                                            <button type="button" class="btn btn-success btn-sm"
                                                    onclick="updateTaskStatus(<%= task.getId() %>, 'COMPLETED', '<%= request.getContextPath() %>')"
                                                    title="Mark Complete">
                                                ✓ Done
                                            </button>
                                        <% } else { %>
                                            <button type="button" class="btn btn-secondary btn-sm"
                                                    onclick="updateTaskStatus(<%= task.getId() %>, 'PENDING', '<%= request.getContextPath() %>')"
                                                    title="Reopen Task">
                                                ↺ Reopen
                                            </button>
                                        <% } %>

                                        <form action="<%= request.getContextPath() %>/tasks" method="POST" style="display: inline;">
                                            <input type="hidden" name="_method" value="DELETE">
                                            <input type="hidden" name="id" value="<%= task.getId() %>">
                                            <button type="submit" class="btn btn-danger btn-sm" data-confirm="Delete this task?" title="Delete Task">
                                                ✕
                                            </button>
                                        </form>
                                    </div>
                                </div>
                            <% } %>
                        <% } %>
                    </div>
                </div>
            </section>

            <!-- Right Sidebar: Create New Task -->
            <aside>
                <div class="card">
                    <div class="card-header">
                        <h3>Create New Task</h3>
                    </div>
                    <form action="<%= request.getContextPath() %>/tasks" method="POST" id="task-create-form">
                        <div class="form-group">
                            <label for="title">Task Title *</label>
                            <input type="text" id="title" name="title" class="form-control" required placeholder="What needs to be done?">
                        </div>

                        <div class="form-group">
                            <label for="description">Description</label>
                            <textarea id="description" name="description" class="form-control" placeholder="Optional details..."></textarea>
                        </div>

                        <div class="form-group">
                            <label for="categoryId">Category</label>
                            <select id="categoryId" name="categoryId" class="form-control">
                                <option value="">None / Uncategorized</option>
                                <% if (categories != null) { %>
                                    <% for (Category category : categories) { %>
                                        <option value="<%= category.getId() %>"><%= category.getName() %></option>
                                    <% } %>
                                <% } %>
                            </select>
                            <div style="margin-top: 6px;">
                                <button type="button" id="openCategoryModal" class="link-btn">+ Add new category</button>
                            </div>
                        </div>

                        <div class="form-row">
                            <div class="form-group">
                                <label for="importance">Importance Level</label>
                                <select id="importance" name="importance" class="form-control">
                                    <option value="LOW">Low</option>
                                    <option value="MEDIUM" selected>Medium</option>
                                    <option value="HIGH">High</option>
                                    <option value="URGENT">Urgent</option>
                                </select>
                            </div>

                            <div class="form-group">
                                <label for="estimatedHours">Est. Hours</label>
                                <input type="number" id="estimatedHours" name="estimatedHours" class="form-control" step="0.5" min="0" placeholder="e.g. 2.5">
                            </div>
                        </div>

                        <div class="form-row">
                            <div class="form-group">
                                <label for="deadlineDate">Deadline Date</label>
                                <input
                                    type="date"
                                    id="deadlineDate"
                                    name="deadlineDate"
                                    min="2026-01-01"
                                    max="2099-12-31"
                                    value="<%= today %>"
                                    required
                                />
                            </div>
                            <div class="form-group">
                                <label for="deadlineTime">Deadline Time <span class="optional">(optional)</span></label>
                                <input
                                    type="time"
                                    id="deadlineTime"
                                    name="deadlineTime"
                                    value="23:59"
                                />
                            </div>
                        </div>

                        <button type="submit" class="btn btn-primary btn-block">Add &amp; Prioritize Task</button>
                    </form>
                </div>

            </aside>
        </div>
    </main>

    <!-- New Category Modal -->
    <div id="categoryModal" class="modal-backdrop" style="display: none;" aria-hidden="true" role="dialog" aria-labelledby="categoryModalTitle">
        <div class="modal-card">
            <div class="modal-header">
                <h3 id="categoryModalTitle">New Category</h3>
                <button type="button" class="modal-close" id="closeCategoryModalBtn" aria-label="Close">&times;</button>
            </div>
            <form id="categoryModalForm">
                <div id="categoryError" class="modal-error" style="display: none;"></div>
                <div class="form-group">
                    <label for="modalCategoryName">Name *</label>
                    <input type="text" id="modalCategoryName" name="name" class="form-control" maxlength="50" required placeholder="e.g. Research">
                </div>
                <div class="form-group">
                    <label for="modalCategoryDescription">Description</label>
                    <textarea id="modalCategoryDescription" name="description" class="form-control" rows="3" placeholder="Optional description..."></textarea>
                </div>
                <div class="form-group">
                    <label for="categoryColor">Color</label>
                    <input type="color" id="categoryColor" name="color" value="#8b5cf6" class="color-picker-input">
                </div>
                <div class="modal-actions">
                    <button type="button" id="cancelCategoryModal" class="btn btn-ghost">Cancel</button>
                    <button type="submit" id="submitCategoryBtn" class="btn btn-primary">Create</button>
                </div>
            </form>
        </div>
    </div>

    <div id="toast-container"></div>

    <footer class="app-footer">
        TaskTier &copy; <%= java.time.Year.now().getValue() %> — Prioritization Powered by Strategy Pattern &amp; Multithreading
    </footer>

    <script src="<%= request.getContextPath() %>/static/app.js"></script>
</body>
</html>
