package com.tasktier.filter;

import com.tasktier.model.UserRole;
import com.tasktier.util.AppLogger;
import com.tasktier.util.JsonUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Authentication and authorization filter for protected application routes.
 * Intercepts requests, validates active session, and redirects unauthenticated users to the login page.
 */
@WebFilter(filterName = "AuthFilter", urlPatterns = {
        "/dashboard",
        "/dashboard.jsp",
        "/tasks",
        "/tasks/*",
        "/tasks.jsp",
        "/time-entries",
        "/time-entries/*",
        "/categories",
        "/categories/*",
        "/admin",
        "/admin/*",
        "/admin.jsp",
        "/progress",
        "/progress.jsp"
})
public class AuthFilter implements Filter {

    /**
     * Initializes the filter with container filter configuration.
     *
     * @param filterConfig filter configuration
     */
    @Override
    public void init(FilterConfig filterConfig) {
        AppLogger.info(getClass(), "AuthFilter initialized.");
    }

    /**
     * Inspects session state and enforces authentication/authorization rules on incoming requests.
     *
     * @param servletRequest incoming request
     * @param servletResponse outgoing response
     * @param chain filter execution chain
     * @throws IOException on I/O failure
     * @throws ServletException on servlet pipeline error
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = requestUri.substring(contextPath.length());

        HttpSession session = request.getSession(false);
        boolean isLoggedIn = session != null && session.getAttribute("userId") != null;

        if (!isLoggedIn) {
            AppLogger.info(getClass(), "Unauthenticated access attempt to: " + path);

            // Handle AJAX / API requests with JSON 401 instead of redirect
            String acceptHeader = request.getHeader("Accept");
            String requestedWith = request.getHeader("X-Requested-With");
            if ((acceptHeader != null && acceptHeader.contains("application/json")) ||
                    "XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
                JsonUtil.writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required. Please log in.");
                return;
            }

            response.sendRedirect(contextPath + "/index.jsp");
            return;
        }

        // Check Admin Authorization for /admin routes
        if (path.startsWith("/admin")) {
            UserRole userRole = (UserRole) session.getAttribute("userRole");
            Object roleObj = session.getAttribute("role");
            boolean isAdmin = userRole == UserRole.ADMIN || "ADMIN".equalsIgnoreCase(String.valueOf(roleObj));

            if (!isAdmin) {
                AppLogger.warn(getClass(), "Forbidden access attempt to admin path by non-admin user ID: " +
                        session.getAttribute("userId"));

                String acceptHeader = request.getHeader("Accept");
                String requestedWith = request.getHeader("X-Requested-With");
                if ((acceptHeader != null && acceptHeader.contains("application/json")) ||
                        "XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
                    JsonUtil.writeJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Access denied: Admin role required");
                    return;
                }

                response.sendRedirect(contextPath + "/dashboard?error=" +
                        java.net.URLEncoder.encode("Access denied: Admin role required", java.nio.charset.StandardCharsets.UTF_8));
                return;
            }
        }

        chain.doFilter(request, response);
    }

    /**
     * Releases any filter resources upon container teardown.
     */
    @Override
    public void destroy() {
        AppLogger.info(getClass(), "AuthFilter destroyed.");
    }
}
