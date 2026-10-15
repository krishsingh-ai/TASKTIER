package com.tasktier.servlet;

import com.tasktier.exception.AuthException;
import com.tasktier.exception.DataAccessException;
import com.tasktier.exception.TaskTierException;
import com.tasktier.exception.ValidationException;
import com.tasktier.model.UserRole;
import com.tasktier.util.AppLogger;
import com.tasktier.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Base abstract servlet providing common session extraction, method tunneling,
 * JSON response serialization, and uniform exception handling across all servlets.
 * Demonstrates OOP Inheritance (BaseServlet extends HttpServlet).
 */
public abstract class BaseServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String method = req.getMethod();
        // Support HTTP Method Tunneling via hidden "_method" form parameter
        if ("POST".equalsIgnoreCase(method)) {
            String overrideMethod = req.getParameter("_method");
            if (overrideMethod != null) {
                if ("PUT".equalsIgnoreCase(overrideMethod)) {
                    doPut(req, resp);
                    return;
                } else if ("DELETE".equalsIgnoreCase(overrideMethod)) {
                    doDelete(req, resp);
                    return;
                }
            }
        }
        super.service(req, resp);
    }

    /**
     * Retrieves the authenticated user's ID from the session.
     *
     * @param request current HTTP request
     * @return user ID or null if unauthenticated
     */
    protected Integer getAuthenticatedUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object idObj = session.getAttribute("userId");
            if (idObj instanceof Integer) {
                return (Integer) idObj;
            }
        }
        return null;
    }

    /**
     * Checks if current session user has the ADMIN role.
     *
     * @param request current HTTP request
     * @return true if admin, false otherwise
     */
    protected boolean isAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object roleObj = session.getAttribute("userRole");
            return roleObj == UserRole.ADMIN;
        }
        return false;
    }

    /**
     * Writes a JSON success response.
     *
     * @param response HTTP response
     * @param statusCode HTTP status
     * @param data response payload
     * @throws IOException on I/O failure
     */
    protected void sendJsonResponse(HttpServletResponse response, int statusCode, Object data) throws IOException {
        JsonUtil.writeJsonResponse(response, statusCode, data);
    }

    /**
     * Writes a JSON error response.
     *
     * @param response HTTP response
     * @param statusCode HTTP error status
     * @param message error message
     * @throws IOException on I/O failure
     */
    protected void sendJsonError(HttpServletResponse response, int statusCode, String message) throws IOException {
        JsonUtil.writeJsonError(response, statusCode, message);
    }

    /**
     * Handles exceptions uniformly according to the custom exception hierarchy.
     * Maps each exception type to the proper HTTP status code and response payload.
     *
     * @param response HTTP response
     * @param exception thrown exception
     * @throws IOException on I/O failure
     */
    protected void handleException(HttpServletResponse response, Exception exception) throws IOException {
        if (exception instanceof ValidationException valEx) {
            AppLogger.warn(getClass(), "Validation failure: " + valEx.getMessage());
            response.setStatus(valEx.getHttpStatusCode());
            response.setContentType("application/json;charset=UTF-8");
            java.util.Map<String, Object> errData = new java.util.HashMap<>();
            errData.put("success", false);
            errData.put("error", valEx.getMessage());
            errData.put("fieldErrors", valEx.getFieldErrors());
            JsonUtil.writeJsonResponse(response, valEx.getHttpStatusCode(), errData);
        } else if (exception instanceof AuthException authEx) {
            AppLogger.warn(getClass(), "Auth failure: " + authEx.getMessage());
            sendJsonError(response, authEx.getHttpStatusCode(), authEx.getMessage());
        } else if (exception instanceof DataAccessException dae) {
            AppLogger.error(getClass(), "Database failure: " + dae.getMessage(), dae);
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "A database error occurred. Please try again.");
        } else if (exception instanceof TaskTierException tte) {
            AppLogger.error(getClass(), "Application failure: " + tte.getMessage(), tte);
            sendJsonError(response, tte.getHttpStatusCode(), tte.getMessage());
        } else {
            AppLogger.error(getClass(), "Unexpected server error: " + exception.getMessage(), exception);
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected server error occurred.");
        }
    }
}
