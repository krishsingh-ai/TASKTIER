<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.Map" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Create Account — TaskTier</title>
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
                <h1>Create Account</h1>
                <p>Register to start optimizing your tasks by priority</p>
            </div>

            <% if (request.getAttribute("error") != null) { %>
                <div class="alert alert-danger">
                    <%= request.getAttribute("error") %>
                </div>
            <% } %>

            <%
                @SuppressWarnings("unchecked")
                Map<String, String> fieldErrors = (Map<String, String>) request.getAttribute("fieldErrors");
            %>

            <form action="<%= request.getContextPath() %>/signup" method="POST">
                <div class="form-group">
                    <label for="displayName">Full Display Name</label>
                    <input type="text" id="displayName" name="displayName" class="form-control"
                           required autofocus value="<%= request.getAttribute("displayName") != null ? request.getAttribute("displayName") : "" %>"
                           placeholder="e.g. John Doe">
                    <% if (fieldErrors != null && fieldErrors.containsKey("displayName")) { %>
                        <span class="error-text"><%= fieldErrors.get("displayName") %></span>
                    <% } %>
                </div>

                <div class="form-group">
                    <label for="username">Username</label>
                    <input type="text" id="username" name="username" class="form-control"
                           required value="<%= request.getAttribute("username") != null ? request.getAttribute("username") : "" %>"
                           placeholder="Choose a unique username">
                    <% if (fieldErrors != null && fieldErrors.containsKey("username")) { %>
                        <span class="error-text"><%= fieldErrors.get("username") %></span>
                    <% } %>
                </div>

                <div class="form-group">
                    <label for="password">Password (min. 6 characters)</label>
                    <input type="password" id="password" name="password" class="form-control"
                           required minlength="6" placeholder="Choose a secure password">
                    <% if (fieldErrors != null && fieldErrors.containsKey("password")) { %>
                        <span class="error-text"><%= fieldErrors.get("password") %></span>
                    <% } %>
                </div>

                <div class="form-group" style="margin-top: 1.25rem;">
                    <button type="submit" class="btn btn-primary btn-block">Complete Registration</button>
                </div>
            </form>

            <div style="text-align: center; margin-top: 1.25rem; font-size: 13px; color: var(--text-secondary);">
                Already have an account?
                <a href="<%= request.getContextPath() %>/login" style="font-weight: 500;">Sign in here</a>
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
