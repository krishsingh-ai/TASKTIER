<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.tasktier.model.Category" %>
<%@ page import="com.tasktier.model.UserRole" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Analytics &amp; Progress — TaskTier</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/static/style.css">
</head>
<body>
    <%
        String displayName = (String) session.getAttribute("displayName");
        UserRole userRole = (UserRole) session.getAttribute("userRole");
        boolean isAdmin = userRole == UserRole.ADMIN;

        int totalCount = (Integer) request.getAttribute("totalCount");
        long completedCount = (Long) request.getAttribute("completedCount");
        long pendingCount = (Long) request.getAttribute("pendingCount");
        long inProgressCount = (Long) request.getAttribute("inProgressCount");
        long overdueCount = (Long) request.getAttribute("overdueCount");
        int completionPercent = (Integer) request.getAttribute("completionPercent");

        double totalHours = (Double) request.getAttribute("totalHours");
        long totalMinutes = (Long) request.getAttribute("totalMinutes");

        long criticalCount = (Long) request.getAttribute("criticalCount");
        long highCount = (Long) request.getAttribute("highCount");
        long mediumCount = (Long) request.getAttribute("mediumCount");
        long lowCount = (Long) request.getAttribute("lowCount");

        @SuppressWarnings("unchecked")
        Map<String, Long> categoryTaskCounts = (Map<String, Long>) request.getAttribute("categoryTaskCounts");
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
                    <a href="<%= request.getContextPath() %>/progress" class="nav-item active">Analytics &amp; Progress</a>
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
        <div style="margin-bottom: 2rem;">
            <h1>Productivity Analytics</h1>
            <p style="color: var(--text-secondary);">Visual breakdown of task completion, effort hours, and tier distribution.</p>
        </div>

        <!-- Metric Summary Cards -->
        <div class="grid-cols-4" style="margin-bottom: 2rem;">
            <div class="stat-card accent-green">
                <div class="stat-icon">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#10b981" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg>
                </div>
                <div class="stat-title">Overall Completion</div>
                <div class="stat-value" data-target="<%= completionPercent %>"><%= completionPercent %>%</div>
                <div class="progress-bar-container">
                    <div class="progress-bar-fill" style="width: <%= completionPercent %>%;"></div>
                </div>
                <div class="stat-sub"><%= completedCount %> of <%= totalCount %> tasks finished</div>
            </div>

            <div class="stat-card accent-gray">
                <div class="stat-icon">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg>
                </div>
                <div class="stat-title">Total Hours Logged</div>
                <div class="stat-value" data-target="<%= totalHours %>"><%= totalHours %> hrs</div>
                <div class="stat-sub"><%= totalMinutes %> cumulative minutes</div>
            </div>

            <div class="stat-card accent-amber">
                <div class="stat-icon">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#f59e0b" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M8.5 14.5A2.5 2.5 0 0 0 11 12c0-1.38-.5-2-1-3-1.072-2.143-.224-4.054 2-6 .5 2.5 2 4.9 4 6.5 2 1.6 3 3.5 3 5.5a7 7 0 1 1-14 0c0-1.153.433-2.294 1-3a2.5 2.5 0 0 0 2.5 2.5z"></path></svg>
                </div>
                <div class="stat-title">In Progress Tasks</div>
                <div class="stat-value" style="color: #38bdf8;" data-target="<%= inProgressCount %>"><%= inProgressCount %></div>
                <div class="stat-sub">Active work sessions</div>
            </div>

            <div class="stat-card accent-red">
                <div class="stat-icon">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#ef4444" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
                </div>
                <div class="stat-title">Overdue Tasks</div>
                <div class="stat-value" style="color: var(--critical);" data-target="<%= overdueCount %>"><%= overdueCount %></div>
                <div class="stat-sub">Auto-marked by housekeeper</div>
            </div>
        </div>

        <div class="grid-cols-2">
            <!-- Left: Priority Tier Distribution (Pure CSS Bars) -->
            <div class="card">
                <div class="card-header">
                    <h3>Priority Tier Distribution</h3>
                </div>
                <p style="font-size: 0.85rem; color: var(--text-secondary); margin-bottom: 1.5rem;">
                    Tasks categorized by their calculated priority scores (Score &ge; 80 Critical, 60-79 High, 40-59 Medium, &lt; 40 Low).
                </p>

                <div class="css-bar-chart">
                    <!-- Critical Tier -->
                    <%
                        double critPct = totalCount > 0 ? ((double) criticalCount / totalCount) * 100.0 : 0;
                    %>
                    <div class="bar-row">
                        <div class="bar-header">
                            <span><span class="badge badge-critical">Critical Tier</span> (&ge; 80 Score)</span>
                            <strong><%= criticalCount %> (<%= Math.round(critPct) %>%)</strong>
                        </div>
                        <div class="bar-track">
                            <div class="bar-value-fill" style="width: <%= critPct %>%; background-color: var(--critical);"></div>
                        </div>
                    </div>

                    <!-- High Tier -->
                    <%
                        double highPct = totalCount > 0 ? ((double) highCount / totalCount) * 100.0 : 0;
                    %>
                    <div class="bar-row">
                        <div class="bar-header">
                            <span><span class="badge badge-high">High Tier</span> (60 – 79 Score)</span>
                            <strong><%= highCount %> (<%= Math.round(highPct) %>%)</strong>
                        </div>
                        <div class="bar-track">
                            <div class="bar-value-fill" style="width: <%= highPct %>%; background-color: var(--high);"></div>
                        </div>
                    </div>

                    <!-- Medium Tier -->
                    <%
                        double medPct = totalCount > 0 ? ((double) mediumCount / totalCount) * 100.0 : 0;
                    %>
                    <div class="bar-row">
                        <div class="bar-header">
                            <span><span class="badge badge-medium">Medium Tier</span> (40 – 59 Score)</span>
                            <strong><%= mediumCount %> (<%= Math.round(medPct) %>%)</strong>
                        </div>
                        <div class="bar-track">
                            <div class="bar-value-fill" style="width: <%= medPct %>%; background-color: var(--medium);"></div>
                        </div>
                    </div>

                    <!-- Low Tier -->
                    <%
                        double lowPct = totalCount > 0 ? ((double) lowCount / totalCount) * 100.0 : 0;
                    %>
                    <div class="bar-row">
                        <div class="bar-header">
                            <span><span class="badge badge-low">Low Tier</span> (&lt; 40 Score)</span>
                            <strong><%= lowCount %> (<%= Math.round(lowPct) %>%)</strong>
                        </div>
                        <div class="bar-track">
                            <div class="bar-value-fill" style="width: <%= lowPct %>%; background-color: var(--low);"></div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Right: Category Breakdown (Pure CSS Bars) -->
            <div class="card">
                <div class="card-header">
                    <h3>Category Breakdown</h3>
                </div>
                <p style="font-size: 0.85rem; color: var(--text-secondary); margin-bottom: 1.5rem;">
                    Distribution of workload across distinct categories (pure CSS width bars, no external chart dependencies).
                </p>

                <% if (categoryTaskCounts == null || categoryTaskCounts.isEmpty()) { %>
                    <div style="text-align: center; padding: 2rem; color: var(--text-muted);">
                        No categorical task data available yet.
                    </div>
                <% } else { %>
                    <div class="css-bar-chart">
                        <% for (Map.Entry<String, Long> entry : categoryTaskCounts.entrySet()) { %>
                            <%
                                double catPct = totalCount > 0 ? ((double) entry.getValue() / totalCount) * 100.0 : 0;
                            %>
                            <div class="bar-row">
                                <div class="bar-header">
                                    <span><strong><%= entry.getKey() %></strong></span>
                                    <span><%= entry.getValue() %> tasks (<%= Math.round(catPct) %>%)</span>
                                </div>
                                <div class="bar-track">
                                    <div class="bar-value-fill" style="width: <%= catPct %>%; background: linear-gradient(90deg, #6366f1, #38bdf8);"></div>
                                </div>
                            </div>
                        <% } %>
                    </div>
                <% } %>
            </div>
        </div>
    </main>

    <div id="toast-container"></div>

    <footer class="app-footer">
        TaskTier &copy; <%= java.time.Year.now().getValue() %> — Analytics Rendered with Pure CSS Bars
    </footer>

    <script src="<%= request.getContextPath() %>/static/app.js"></script>
</body>
</html>
