package com.tasktier.servlet;

import com.tasktier.exception.AuthException;
import com.tasktier.model.User;
import com.tasktier.service.AuthService;
import com.tasktier.util.AppLogger;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Optional;

/**
 * Servlet handling user authentication and session establishment.
 * URL Pattern: /login
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/login", "/admin-login"})
public class LoginServlet extends BaseServlet {

    private final AuthService authService;

    /**
     * Default constructor initializing AuthService.
     */
    public LoginServlet() {
        this.authService = new AuthService();
    }

    /**
     * Parameterized constructor for dependency injection and testing.
     *
     * @param authService authentication service
     */
    public LoginServlet(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("userId") != null) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        String uri = request.getRequestURI();
        if (uri != null && uri.endsWith("/admin-login")) {
            request.setAttribute("isAdminPortal", true);
        }

        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String acceptHeader = request.getHeader("Accept");
        boolean isAjax = (acceptHeader != null && acceptHeader.contains("application/json")) ||
                "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"));

        try {
            Optional<User> userOptional = authService.authenticate(username, password);

            if (userOptional.isPresent()) {
                User user = userOptional.get();

                // Prevent session fixation by invalidating old session and creating new
                HttpSession oldSession = request.getSession(false);
                if (oldSession != null) {
                    oldSession.invalidate();
                }

                HttpSession session = request.getSession(true);
                session.setAttribute("userId", user.getId());
                session.setAttribute("username", user.getUsername());
                session.setAttribute("displayName", user.getDisplayName());
                session.setAttribute("userRole", user.getRole());
                session.setAttribute("role", user.getRole().name());

                AppLogger.info(getClass(), "Session established for user: " + user.getUsername() + " (role: " + user.getRole() + ")");

                String redirectTarget = (user.getRole() == com.tasktier.model.UserRole.ADMIN &&
                        (request.getRequestURI().endsWith("/admin-login") || "true".equals(request.getParameter("isAdminPortal"))))
                        ? request.getContextPath() + "/admin"
                        : request.getContextPath() + "/dashboard";

                if (isAjax) {
                    sendJsonResponse(response, HttpServletResponse.SC_OK, java.util.Map.of(
                            "success", true,
                            "redirectUrl", redirectTarget,
                            "user", user
                    ));
                } else {
                    response.sendRedirect(redirectTarget);
                }
            } else {
                throw new AuthException("Invalid credentials.");
            }
        } catch (AuthException exception) {
            if (request.getRequestURI().endsWith("/admin-login") || "true".equals(request.getParameter("isAdminPortal"))) {
                request.setAttribute("isAdminPortal", true);
            }
            AppLogger.warn(getClass(), "Login failed for username: " + username + " -> " + exception.getMessage());
            if (isAjax) {
                sendJsonError(response, exception.getHttpStatusCode(), exception.getMessage());
            } else {
                request.setAttribute("error", exception.getMessage());
                request.setAttribute("username", username);
                request.getRequestDispatcher("/index.jsp").forward(request, response);
            }
        } catch (Exception exception) {
            AppLogger.error(getClass(), "Unexpected error during login", exception);
            if (isAjax) {
                handleException(response, exception);
            } else {
                request.setAttribute("error", "An internal error occurred. Please try again.");
                request.getRequestDispatcher("/index.jsp").forward(request, response);
            }
        }
    }
}
