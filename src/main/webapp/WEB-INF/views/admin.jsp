<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.tasktier.model.User" %>
<%@ page import="com.tasktier.model.UserSummaryDTO" %>
<%@ page import="com.tasktier.model.Task" %>
<%@ page import="com.tasktier.model.UserRole" %>
<%@ page import="com.tasktier.util.DateUtil" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Administration Console — TaskTier</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/static/style.css">
</head>
<body>
    <%
        String displayName = (String) session.getAttribute("displayName");
        Integer currentUserId = (Integer) session.getAttribute("userId");

        @SuppressWarnings("unchecked")
        List<User> users = (List<User>) request.getAttribute("users");

        @SuppressWarnings("unchecked")
        List<UserSummaryDTO> userSummaries = (List<UserSummaryDTO>) request.getAttribute("userSummaries");

        @SuppressWarnings("unchecked")
        List<Task> tasks = (List<Task>) request.getAttribute("tasks");

        Integer activeSessionCount = (Integer) request.getAttribute("activeSessionCount");
        String currentStrategyName = (String) request.getAttribute("currentStrategyName");
        String currentStrategyDescription = (String) request.getAttribute("currentStrategyDescription");
    %>

    <header class="app-header">
        <div class="header-container">
            <div class="header-left">
                <a href="<%= request.getContextPath() %>/admin" class="brand-logo">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" class="brand-mark"><rect x="3" y="3" width="7" height="7"></rect><rect x="14" y="3" width="7" height="7"></rect><rect x="14" y="14" width="7" height="7"></rect><rect x="3" y="14" width="7" height="7"></rect></svg>
                    <span>TaskTier</span>
                </a>
                <nav class="nav-links" id="primary-nav">
                    <a href="<%= request.getContextPath() %>/dashboard" class="nav-item">Dashboard</a>
                    <a href="<%= request.getContextPath() %>/progress" class="nav-item">Analytics &amp; Progress</a>
                    <a href="<%= request.getContextPath() %>/admin" class="nav-item active">Admin Panel</a>
                </nav>
            </div>
            <div class="header-right">
                <button type="button" id="theme-toggle" class="theme-toggle-btn" aria-label="Toggle theme" title="Toggle theme">
                    <svg class="sun-icon" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="5"></circle><line x1="12" y1="1" x2="12" y2="3"></line><line x1="12" y1="21" x2="12" y2="23"></line><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"></line><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"></line><line x1="1" y1="12" x2="3" y2="12"></line><line x1="21" y1="12" x2="23" y2="12"></line><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"></line><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"></line></svg>
                    <svg class="moon-icon" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="display: none;"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path></svg>
                </button>
                <div class="header-divider"></div>
                <div class="user-pill">
                    <span class="user-avatar-circle">A</span>
                    <span class="user-pill-name"><%= displayName != null ? displayName : "Admin" %></span>
                </div>
                <a href="<%= request.getContextPath() %>/logout" class="btn-signout-text">Sign Out</a>
                <button type="button" id="nav-toggle" class="nav-hamburger" aria-label="Toggle navigation menu" aria-expanded="false">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="3" y1="12" x2="21" y2="12"></line><line x1="3" y1="6" x2="21" y2="6"></line><line x1="3" y1="18" x2="21" y2="18"></line></svg>
                </button>
            </div>
        </div>
    </header>

    <main class="main-container">
        <% if ("true".equals(request.getParameter("strategyUpdated"))) { %>
            <div class="alert alert-success">
                Strategy updated successfully! All task scores were recalculated in parallel using the 3-thread ExecutorService.
            </div>
        <% } %>

        <% if ("true".equals(request.getParameter("recalculated"))) { %>
            <div class="alert alert-success">
                All task scores successfully recalculated across the database.
            </div>
        <% } %>

        <!-- Admin Overview Metrics -->
        <div class="grid-cols-4" style="margin-bottom: 1.5rem;">
            <div class="stat-card">
                <div class="stat-title"><span class="stat-dot dot-medium"></span>Active Sessions</div>
                <div class="stat-value" data-target="<%= activeSessionCount != null ? activeSessionCount : 0 %>"><%= activeSessionCount != null ? activeSessionCount : 0 %></div>
            </div>
            <div class="stat-card">
                <div class="stat-title"><span class="stat-dot dot-gray"></span>Registered Users</div>
                <div class="stat-value" data-target="<%= users != null ? users.size() : 0 %>"><%= users != null ? users.size() : 0 %></div>
            </div>
            <div class="stat-card">
                <div class="stat-title"><span class="stat-dot dot-success"></span>Global Tasks</div>
                <div class="stat-value" data-target="<%= tasks != null ? tasks.size() : 0 %>"><%= tasks != null ? tasks.size() : 0 %></div>
            </div>
            <div class="stat-card">
                <div class="stat-title"><span class="stat-dot dot-critical"></span>Worker Threads</div>
                <div class="stat-value" data-target="3">3</div>
            </div>
        </div>

        <!-- Strategy Management Card (Polymorphism & Multithreading Control) -->
        <div class="card" style="border-left: 4px solid #a855f7;">
            <div class="card-header">
                <div>
                    <h3>Runtime Prioritization Strategy (OOP Polymorphism)</h3>
                    <p style="font-size: 0.85rem; color: var(--text-secondary); margin-top: 0.25rem;">
                        Switch between polymorphic implementations of <code>PriorityStrategy</code> at runtime.
                    </p>
                </div>
                <form action="<%= request.getContextPath() %>/admin" method="POST" style="margin: 0;">
                    <input type="hidden" name="action" value="recalculate">
                    <button type="submit" class="btn btn-secondary btn-sm" title="Invoke synchronized recalculateAll()">
                        &#x21bb; Parallel Recalculate All
                    </button>
                </form>
            </div>

            <div style="background-color: var(--bg-input); padding: 1rem; border-radius: var(--radius-sm); margin-bottom: 1.5rem;">
                <div><strong>Current Active Strategy:</strong> <span style="color: #a855f7; font-weight: 600;"><%= currentStrategyName %></span></div>
                <div style="font-size: 0.85rem; color: var(--text-secondary); margin-top: 0.35rem;"><%= currentStrategyDescription %></div>
            </div>

            <form action="<%= request.getContextPath() %>/admin" method="POST">
                <input type="hidden" name="action" value="switch_strategy">
                <div class="form-row">
                    <div class="form-group" style="flex: 2;">
                        <label for="strategyType">Select Strategy</label>
                        <select id="strategyType" name="strategyType" class="form-control" onchange="toggleWeights(this.value)">
                            <option value="WEIGHTED" <%= currentStrategyName.contains("Weighted") ? "selected" : "" %>>
                                Weighted Strategy (Importance + Urgency)
                            </option>
                            <option value="URGENCY_ONLY" <%= currentStrategyName.contains("Urgency-Only") ? "selected" : "" %>>
                                Urgency-Only Strategy (Deadline Proximity Exclusively)
                            </option>
                        </select>
                    </div>

                    <div class="form-group weight-field" style="flex: 1;">
                        <label for="weightImportance">Importance Weight (W₁)</label>
                        <input type="number" id="weightImportance" name="weightImportance" class="form-control"
                               step="0.05" min="0" max="1" value="0.5">
                    </div>

                    <div class="form-group weight-field" style="flex: 1;">
                        <label for="weightUrgency">Urgency Weight (W₂)</label>
                        <input type="number" id="weightUrgency" name="weightUrgency" class="form-control"
                               step="0.05" min="0" max="1" value="0.5">
                    </div>
                </div>

                <button type="submit" class="btn btn-primary">Apply Strategy &amp; Recalculate Tasks</button>
            </form>
        </div>

        <!-- User Management Table -->
        <div class="card">
            <div class="card-header">
                <h3>User Accounts Management</h3>
            </div>
            <div class="table-responsive">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Username</th>
                            <th>Display Name</th>
                            <th>Tasks Created</th>
                            <th>Tasks Completed</th>
                            <th>Hours Logged</th>
                            <th>Last Login</th>
                            <th>Status</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (userSummaries != null) { %>
                            <% for (UserSummaryDTO user : userSummaries) { %>
                                <tr>
                                    <td><strong><%= user.getUsername() %></strong></td>
                                    <td><%= user.getDisplayName() %></td>
                                    <td class="tabular-nums"><%= user.getTasksCreated() %></td>
                                    <td class="tabular-nums"><%= user.getTasksCompleted() %></td>
                                    <td class="tabular-nums"><%= String.format(java.util.Locale.US, "%.1fh", user.getHoursLogged()) %></td>
                                    <td><%= user.getLastLoginAt() != null ? DateUtil.formatForDisplay(user.getLastLoginAt()) : "Never" %></td>
                                    <td>
                                        <% if (user.isActive()) { %>
                                            <span class="badge badge-active">Active</span>
                                        <% } else { %>
                                            <span class="badge badge-deactivated">Deactivated</span>
                                        <% } %>
                                    </td>
                                    <td>
                                        <% if (currentUserId != null && currentUserId != user.getId()) { %>
                                            <% if (user.isActive()) { %>
                                                <form action="<%= request.getContextPath() %>/admin" method="POST" style="display: inline;">
                                                    <input type="hidden" name="action" value="deactivate">
                                                    <input type="hidden" name="userId" value="<%= user.getId() %>">
                                                    <button type="submit" class="btn btn-danger btn-sm" data-confirm="Deactivate user '<%= user.getUsername() %>'?">
                                                        Deactivate
                                                    </button>
                                                </form>
                                            <% } else { %>
                                                <form action="<%= request.getContextPath() %>/admin" method="POST" style="display: inline;">
                                                    <input type="hidden" name="action" value="reactivate">
                                                    <input type="hidden" name="userId" value="<%= user.getId() %>">
                                                    <button type="submit" class="btn btn-success btn-sm">
                                                        Reactivate
                                                    </button>
                                                </form>
                                            <% } %>
                                        <% } else { %>
                                            <span style="font-size: 0.8rem; color: var(--text-muted);">(Current Admin)</span>
                                        <% } %>
                                    </td>
                                </tr>
                            <% } %>
                        <% } %>
                    </tbody>
                </table>
            </div>
        </div>

        <!-- Global Tasks Overview Table -->
        <div class="card">
            <div class="card-header">
                <h3>Global Tasks Overview</h3>
            </div>
            <div class="table-responsive">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Task ID</th>
                            <th>User ID</th>
                            <th>Title</th>
                            <th>Importance</th>
                            <th>Status</th>
                            <th>Priority Score</th>
                            <th>Tier</th>
                            <th>Deadline</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (tasks != null && !tasks.isEmpty()) { %>
                            <% for (Task task : tasks) { %>
                                <tr>
                                    <td><%= task.getId() %></td>
                                    <td><%= task.getUserId() %></td>
                                    <td><strong><%= task.getTitle() %></strong></td>
                                    <td><%= task.getImportance().getDisplayName() %></td>
                                    <td>
                                        <span class="badge badge-status-<%= task.getStatus().name().toLowerCase() %>">
                                            <%= task.getStatus().getDisplayName() %>
                                        </span>
                                    </td>
                                    <td><strong><%= task.getPriorityScore() %></strong></td>
                                    <td><span class="badge <%= task.getTierBadgeClass() %>"><%= task.getTier() %></span></td>
                                    <td><%= task.getDeadline() != null ? DateUtil.formatForDisplay(task.getDeadline()) : "None" %></td>
                                </tr>
                            <% } %>
                        <% } else { %>
                            <tr>
                                <td colspan="8" style="text-align: center; color: var(--text-muted); padding: 1.5rem;">
                                    No tasks registered across the system.
                                </td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        </div>
    </main>

    <div id="toast-container"></div>

    <footer class="app-footer">
        TaskTier &copy; <%= java.time.Year.now().getValue() %> — Admin Console &amp; Multithreaded Strategy Orchestrator
    </footer>

    <script src="<%= request.getContextPath() %>/static/app.js"></script>
    <script>
        function toggleWeights(strategyType) {
            const weightFields = document.querySelectorAll('.weight-field');
            weightFields.forEach(field => {
                field.style.display = (strategyType === 'WEIGHTED') ? 'block' : 'none';
            });
        }
    </script>
</body>
</html>
