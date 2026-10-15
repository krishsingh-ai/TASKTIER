package com.tasktier.servlet;

import com.tasktier.exception.ValidationException;
import com.tasktier.model.User;
import com.tasktier.service.AuthService;
import com.tasktier.util.AppLogger;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Servlet handling user registration and input validation.
 * URL Pattern: /signup
 */
@WebServlet(name = "SignupServlet", urlPatterns = {"/signup"})
public class SignupServlet extends BaseServlet {

    private final AuthService authService;

    /**
     * Default constructor initializing AuthService.
     */
    public SignupServlet() {
        this.authService = new AuthService();
    }

    /**
     * Parameterized constructor for dependency injection and testing.
     *
     * @param authService authentication service
     */
    public SignupServlet(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/signup.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String displayName = request.getParameter("displayName");
        String password = request.getParameter("password");
        String acceptHeader = request.getHeader("Accept");
        boolean isAjax = (acceptHeader != null && acceptHeader.contains("application/json")) ||
                "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"));

        try {
            User registeredUser = authService.register(username, displayName, password);
            AppLogger.info(getClass(), "User registration completed: " + registeredUser.getUsername());

            if (isAjax) {
                sendJsonResponse(response, HttpServletResponse.SC_CREATED, java.util.Map.of(
                        "success", true,
                        "message", "Account registered successfully!",
                        "redirectUrl", request.getContextPath() + "/index.jsp?registered=true"
                ));
            } else {
                response.sendRedirect(request.getContextPath() + "/index.jsp?registered=true");
            }
        } catch (ValidationException exception) {
            AppLogger.warn(getClass(), "Registration validation failed: " + exception.getMessage());
            if (isAjax) {
                handleException(response, exception);
            } else {
                request.setAttribute("error", exception.getMessage());
                request.setAttribute("fieldErrors", exception.getFieldErrors());
                request.setAttribute("username", username);
                request.setAttribute("displayName", displayName);
                request.getRequestDispatcher("/signup.jsp").forward(request, response);
            }
        } catch (Exception exception) {
            AppLogger.error(getClass(), "Unexpected error during registration", exception);
            if (isAjax) {
                handleException(response, exception);
            } else {
                request.setAttribute("error", "An internal error occurred during registration.");
                request.getRequestDispatcher("/signup.jsp").forward(request, response);
            }
        }
    }
}
