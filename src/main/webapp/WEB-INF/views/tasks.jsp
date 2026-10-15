<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.tasktier.model.Task" %>
<%@ page import="com.tasktier.model.TimeEntry" %>
<%@ page import="com.tasktier.model.UserRole" %>
<%@ page import="com.tasktier.util.DateUtil" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Task Details — TaskTier</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/static/style.css">
</head>
<body>
    <%
        Task task = (Task) request.getAttribute("task");
        TimeEntry activeTimer = (TimeEntry) request.getAttribute("activeTimer");

        @SuppressWarnings("unchecked")
        List<TimeEntry> timeEntries = (List<TimeEntry>) request.getAttribute("timeEntries");

        String displayName = (String) session.getAttribute("displayName");
        UserRole userRole = (UserRole) session.getAttribute("userRole");
        boolean isAdmin = userRole == UserRole.ADMIN;
    %>

    <header class="app-header">
        <div class="header-container">
            <div class="header-left">
                <a href="<%= request.getContextPath() %>/dashboard" class="brand-logo">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" class="brand-mark"><rect x="3" y="3" width="7" height="7"></rect><rect x="14" y="3" width="7" height="7"></rect><rect x="14" y="14" width="7" height="7"></rect><rect x="3" y="14" width="7" height="7"></rect></svg>
                    <span>TaskTier</span>
                </a>
                <nav class="nav-links" id="primary-nav">
                    <a href="<%= request.getContextPath() %>/dashboard" class="nav-item">Dashboard</a>
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
        <div style="margin-bottom: 1.5rem;">
            <a href="<%= request.getContextPath() %>/dashboard" class="btn btn-secondary btn-sm">&larr; Back to Dashboard</a>
        </div>

        <div class="grid-main-side">
            <!-- Left: Task Information and Time Logs -->
            <section>
                <div class="card">
                    <div class="card-header">
                        <div>
                            <span class="badge <%= task.getTierBadgeClass() %>" style="margin-bottom: 0.5rem;"><%= task.getTier() %> Tier</span>
                            <h2><%= task.getTitle() %></h2>
                        </div>
                        <div class="task-score-pill">
                            <span class="score-num"><%= task.getPriorityScore() %></span>
                            <span class="score-label">Priority Score</span>
                        </div>
                    </div>

                    <div style="margin-bottom: 1.5rem;">
                        <h4 style="color: var(--text-secondary); margin-bottom: 0.5rem;">Description</h4>
                        <p style="color: var(--text-primary); background-color: var(--bg-input); padding: 1rem; border-radius: var(--radius-sm); border: 1px solid var(--border-color);">
                            <%= task.getDescription() != null && !task.getDescription().isEmpty() ? task.getDescription() : "No detailed description provided." %>
                        </p>
                    </div>

                    <div class="grid-cols-4" style="margin-bottom: 1.5rem;">
                        <div>
                            <span style="font-size: 0.8rem; color: var(--text-muted); display: block;">Importance</span>
                            <strong><%= task.getImportance().getDisplayName() %></strong>
                        </div>
                        <div>
                            <span style="font-size: 0.8rem; color: var(--text-muted); display: block;">Status</span>
                            <span class="badge badge-status-<%= task.getStatus().name().toLowerCase() %>">
                                <%= task.getStatus().getDisplayName() %>
                            </span>
                        </div>
                        <div>
                            <span style="font-size: 0.8rem; color: var(--text-muted); display: block;">Deadline</span>
                            <strong><%= task.getDeadline() != null ? DateUtil.formatForDisplay(task.getDeadline()) : "None" %></strong>
                        </div>
                        <div>
                            <span style="font-size: 0.8rem; color: var(--text-muted); display: block;">Category</span>
                            <strong><%= task.getCategoryName() != null ? task.getCategoryName() : "General" %></strong>
                        </div>
                    </div>
                </div>

                <!-- Time Entry History -->
                <div class="card">
                    <div class="card-header">
                        <h3>Time Log History</h3>
                    </div>
                    <% if (timeEntries == null || timeEntries.isEmpty()) { %>
                        <div style="text-align: center; padding: 2rem; color: var(--text-muted);">
                            No time logs recorded yet. Start the timer to track effort!
                        </div>
                    <% } else { %>
                        <div class="table-responsive">
                            <table class="data-table">
                                <thead>
                                    <tr>
                                        <th>Start Time</th>
                                        <th>End Time</th>
                                        <th>Duration</th>
                                        <th>Notes</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <% for (TimeEntry entry : timeEntries) { %>
                                        <tr>
                                            <td><%= DateUtil.formatForDisplay(entry.getStartTime()) %></td>
                                            <td><%= entry.getEndTime() != null ? DateUtil.formatForDisplay(entry.getEndTime()) : "<span style='color: var(--low);'>Running...</span>" %></td>
                                            <td><strong><%= entry.getDurationMinutes() != null ? entry.getDurationMinutes() + " min" : "—" %></strong></td>
                                            <td><%= entry.getNotes() != null ? entry.getNotes() : "" %></td>
                                        </tr>
                                    <% } %>
                                </tbody>
                            </table>
                        </div>
                    <% } %>
                </div>
            </section>

            <!-- Right Sidebar: Interactive Timer Controller -->
            <aside>
                <div class="card timer-widget">
                    <h3>Focus Time Tracker</h3>

                    <% if (activeTimer != null) { %>
                        <div style="margin: 1rem 0;">
                            <span class="badge badge-status-in_progress">Timer Active</span>
                            <input type="hidden" id="active-timer-start" value="<%= activeTimer.getStartTime().toString() %>">
                            <div class="timer-display" id="active-timer-display">00:00:00</div>
                            <p style="font-size: 0.85rem; color: var(--text-secondary); margin-bottom: 1rem;">
                                Started: <%= DateUtil.formatForDisplay(activeTimer.getStartTime()) %>
                            </p>
                        </div>

                        <form action="<%= request.getContextPath() %>/time-entries" method="POST">
                            <input type="hidden" name="action" value="stop">
                            <input type="hidden" name="taskId" value="<%= task.getId() %>">
                            <button type="submit" class="btn btn-danger btn-block">Stop &amp; Log Time</button>
                        </form>
                    <% } else { %>
                        <div style="margin: 1rem 0;">
                            <div class="timer-display" style="color: var(--text-muted);">00:00:00</div>
                            <p style="font-size: 0.85rem; color: var(--text-secondary); margin-bottom: 1rem;">
                                Ready to track focus session
                            </p>
                        </div>

                        <form action="<%= request.getContextPath() %>/time-entries" method="POST">
                            <input type="hidden" name="action" value="start">
                            <input type="hidden" name="taskId" value="<%= task.getId() %>">
                            <div class="form-group" style="text-align: left;">
                                <label for="timerNotes">Session Focus Notes</label>
                                <input type="text" id="timerNotes" name="notes" class="form-control" placeholder="e.g. Debugging tests">
                            </div>
                            <button type="submit" class="btn btn-success btn-block">Start Stopwatch Timer</button>
                        </form>
                    <% } %>
                </div>

                <!-- Quick Status Change Card -->
                <div class="card">
                    <div class="card-header">
                        <h3>Update Status</h3>
                    </div>
                    <div style="display: flex; flex-direction: column; gap: 0.5rem;">
                        <button type="button" class="btn btn-secondary btn-block"
                                onclick="updateTaskStatus(<%= task.getId() %>, 'PENDING', '<%= request.getContextPath() %>')">
                            Set Pending
                        </button>
                        <button type="button" class="btn btn-secondary btn-block"
                                onclick="updateTaskStatus(<%= task.getId() %>, 'IN_PROGRESS', '<%= request.getContextPath() %>')">
                            Set In Progress
                        </button>
                        <button type="button" class="btn btn-success btn-block"
                                onclick="updateTaskStatus(<%= task.getId() %>, 'COMPLETED', '<%= request.getContextPath() %>')">
                            ✓ Mark as Completed
                        </button>
                    </div>
                </div>
            </aside>
        </div>
    </main>

    <div id="toast-container"></div>

    <footer class="app-footer">
        TaskTier &copy; <%= java.time.Year.now().getValue() %> — Precision Time Tracking &amp; Prioritization
    </footer>

    <script src="<%= request.getContextPath() %>/static/app.js"></script>
</body>
</html>
