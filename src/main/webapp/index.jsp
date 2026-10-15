<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    boolean isAdminPortal = Boolean.TRUE.equals(request.getAttribute("isAdminPortal")) ||
            (request.getRequestURI() != null && request.getRequestURI().contains("admin-login"));
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= isAdminPortal ? "Admin Sign In" : "Sign In" %> — TaskTier</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/static/style.css">
</head>
<body>
    <header class="app-header">
        <div class="header-container">
            <a href="<%= request.getContextPath() %>/" class="brand-logo">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" class="brand-mark"><rect x="3" y="3" width="7" height="7"></rect><rect x="14" y="3" width="7" height="7"></rect><rect x="14" y="14" width="7" height="7"></rect><rect x="3" y="14" width="7" height="7"></rect></svg>
                <span>TaskTier</span>
            </a>
            <div class="header-right">
                <button type="button" id="theme-toggle" class="theme-toggle-btn" aria-label="Toggle theme" title="Toggle theme">
                    <svg class="sun-icon" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="5"></circle><line x1="12" y1="1" x2="12" y2="3"></line><line x1="12" y1="21" x2="12" y2="23"></line><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"></line><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"></line><line x1="1" y1="12" x2="3" y2="12"></line><line x1="21" y1="12" x2="23" y2="12"></line><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"></line><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"></line></svg>
                    <svg class="moon-icon" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="display: none;"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path></svg>
                </button>
            </div>
        </div>
    </header>

    <main class="main-container">
        <div class="auth-container">
            <div class="auth-header">
                <% if (isAdminPortal) { %>
                    <div style="margin-bottom: 0.5rem;">
                        <span class="badge badge-medium">Admin Portal</span>
                    </div>
                    <h1>Administrator Sign In</h1>
                    <p>Enter administrator credentials to access the console</p>
                <% } else { %>
                    <h1>Sign In</h1>
                    <p>Access your prioritized tasks and time analytics</p>
                <% } %>
            </div>

            <% if (request.getAttribute("error") != null) { %>
                <div class="alert alert-danger">
                    <%= request.getAttribute("error") %>
                </div>
            <% } %>

            <% if ("true".equals(request.getParameter("registered"))) { %>
                <div class="alert alert-success">
                    Account created successfully! Please sign in.
                </div>
            <% } %>

            <% if ("true".equals(request.getParameter("loggedOut"))) { %>
                <div class="alert alert-success">
                    You have been signed out safely.
                </div>
            <% } %>

            <form action="<%= isAdminPortal ? request.getContextPath() + "/admin-login" : request.getContextPath() + "/login" %>" method="POST">
                <input type="hidden" name="isAdminPortal" value="<%= isAdminPortal ? "true" : "false" %>">
                <div class="form-group">
                    <label for="username">Username</label>
                    <input type="text" id="username" name="username" class="form-control"
                           required autofocus value="<%= request.getAttribute("username") != null ? request.getAttribute("username") : (isAdminPortal ? "admin" : "") %>"
                           placeholder="Enter your username">
                </div>

                <div class="form-group">
                    <label for="password">Password</label>
                    <input type="password" id="password" name="password" class="form-control"
                           required placeholder="Enter your password">
                </div>

                <div class="form-group" style="margin-top: 1.25rem;">
                    <button type="submit" class="btn btn-primary btn-block"><%= isAdminPortal ? "Sign In to Admin Panel" : "Sign In" %></button>
                </div>
            </form>

            <% if (!isAdminPortal) { %>
                <div style="text-align: center; margin-top: 1.25rem; font-size: 13px; color: var(--text-secondary);">
                    Don't have an account?
                    <a href="<%= request.getContextPath() %>/signup" style="font-weight: 500;">Create an account</a>
                </div>
            <% } else { %>
                <div style="text-align: center; margin-top: 1.25rem; font-size: 13px; color: var(--text-secondary);">
                    Return to regular sign in:
                    <a href="<%= request.getContextPath() %>/login" style="font-weight: 500;">User Sign In</a>
                </div>
            <% } %>

            <div style="margin-top: 1.75rem; padding-top: 0.85rem; border-top: 1px solid var(--border-color); font-size: 12px; color: var(--text-muted); text-align: center;">
                Administrator Access: <code>admin</code> / <code>admin123</code>
            </div>
        </div>
    </main>

    <div id="toast-container"></div>

    <footer class="app-footer">
        TaskTier &copy; <%= java.time.Year.now().getValue() %> — Task Prioritization System
    </footer>

    <script src="<%= request.getContextPath() %>/static/app.js"></script>
</body>
</html>
