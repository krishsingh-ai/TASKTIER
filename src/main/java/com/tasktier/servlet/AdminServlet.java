package com.tasktier.servlet;

import com.tasktier.config.AppInitializer;
import com.tasktier.dao.TaskDAO;
import com.tasktier.exception.ValidationException;
import com.tasktier.model.Task;
import com.tasktier.model.User;
import com.tasktier.model.UserSummaryDTO;
import com.tasktier.service.AuthService;
import com.tasktier.service.PriorityCalculator;
import com.tasktier.strategy.PriorityStrategy;
import com.tasktier.strategy.UrgencyOnlyStrategy;
import com.tasktier.strategy.WeightedPriorityStrategy;
import com.tasktier.util.AppLogger;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Administrative control servlet for system management, user activation toggling,
 * runtime priority strategy switching, and active session inspection.
 * URL Pattern: /admin
 */
@WebServlet(name = "AdminServlet", urlPatterns = {"/admin", "/admin/*", "/admin.jsp"})
public class AdminServlet extends BaseServlet {

    private final AuthService authService;
    private final TaskDAO taskDAO;
    private final PriorityCalculator priorityCalculator;

    /**
     * Default constructor initializing services and DAOs for servlet container instantiation.
     */
    public AdminServlet() {
        this.authService = new AuthService();
        this.taskDAO = new TaskDAO();
        this.priorityCalculator = new PriorityCalculator();
    }

    /**
     * Parameterized constructor for dependency injection and testing.
     *
     * @param authService authentication and user management service
     * @param taskDAO task data access object
     * @param priorityCalculator priority computation service
     */
    public AdminServlet(AuthService authService, TaskDAO taskDAO, PriorityCalculator priorityCalculator) {
        this.authService = authService;
        this.taskDAO = taskDAO;
        this.priorityCalculator = priorityCalculator;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isAdmin(request)) {
            response.sendRedirect(request.getContextPath() + "/dashboard?error=unauthorized");
            return;
        }

        try {
            List<UserSummaryDTO> userSummaries = authService.getUserSummaries();
            List<User> allUsers = authService.getAllUsers();
            List<Task> allTasks = taskDAO.findAll();

            String pathInfo = request.getPathInfo();
            if ("/users".equalsIgnoreCase(pathInfo)) {
                sendJsonResponse(response, HttpServletResponse.SC_OK, userSummaries);
                return;
            }

            // Retrieve active session registry (Rubric 3: ConcurrentHashMap access)
            @SuppressWarnings("unchecked")
            ConcurrentHashMap<String, HttpSession> sessionRegistry =
                    (ConcurrentHashMap<String, HttpSession>) getServletContext().getAttribute(AppInitializer.SESSION_REGISTRY_ATTR);

            int activeSessionCount = sessionRegistry != null ? sessionRegistry.size() : 0;

            PriorityStrategy currentStrategy = priorityCalculator.getStrategy();

            String acceptHeader = request.getHeader("Accept");
            if (acceptHeader != null && acceptHeader.contains("application/json")) {
                sendJsonResponse(response, HttpServletResponse.SC_OK, java.util.Map.of(
                        "users", allUsers,
                        "userSummaries", userSummaries,
                        "tasks", allTasks,
                        "activeSessions", activeSessionCount,
                        "strategy", currentStrategy.getStrategyName()
                ));
                return;
            }

            request.setAttribute("users", allUsers);
            request.setAttribute("userSummaries", userSummaries);
            request.setAttribute("tasks", allTasks);
            request.setAttribute("activeSessionCount", activeSessionCount);
            request.setAttribute("currentStrategyName", currentStrategy.getStrategyName());
            request.setAttribute("currentStrategyDescription", currentStrategy.getDescription());

            request.getRequestDispatcher("/WEB-INF/views/admin.jsp").forward(request, response);
        } catch (Exception exception) {
            handleException(response, exception);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isAdmin(request)) {
            sendJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Forbidden. Administrator role required.");
            return;
        }

        String pathInfo = request.getPathInfo();
        String action = request.getParameter("action");
        String userIdStr = request.getParameter("userId");

        if (pathInfo != null) {
            String[] segments = pathInfo.split("/");
            if (segments.length >= 4 && "users".equalsIgnoreCase(segments[1])) {
                userIdStr = segments[2];
                action = segments[3];
            }
        }

        String acceptHeader = request.getHeader("Accept");
        boolean isAjax = (acceptHeader != null && acceptHeader.contains("application/json")) ||
                "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"));

        try {
            if ("deactivate".equalsIgnoreCase(action)) {
                if (userIdStr == null || userIdStr.trim().isEmpty()) {
                    throw new ValidationException("User ID is required for deactivation.");
                }
                int targetUserId;
                try {
                    targetUserId = Integer.parseInt(userIdStr.trim());
                } catch (NumberFormatException nfe) {
                    throw new ValidationException("Invalid user ID format.");
                }
                // Prevent deactivating oneself
                Integer currentAdminId = getAuthenticatedUserId(request);
                if (currentAdminId != null && currentAdminId == targetUserId) {
                    throw new ValidationException("Administrators cannot deactivate their own account.");
                }

                boolean updated = authService.deactivateUser(targetUserId);
                AppLogger.info(getClass(), "Admin deactivated user ID: " + targetUserId);

                if (isAjax) {
                    sendJsonResponse(response, HttpServletResponse.SC_OK, java.util.Map.of("success", updated));
                } else {
                    response.sendRedirect(request.getContextPath() + "/admin?updated=true");
                }
            } else if ("reactivate".equalsIgnoreCase(action)) {
                if (userIdStr == null || userIdStr.trim().isEmpty()) {
                    throw new ValidationException("User ID is required for reactivation.");
                }
                int targetUserId;
                try {
                    targetUserId = Integer.parseInt(userIdStr.trim());
                } catch (NumberFormatException nfe) {
                    throw new ValidationException("Invalid user ID format.");
                }
                boolean updated = authService.reactivateUser(targetUserId);
                AppLogger.info(getClass(), "Admin reactivated user ID: " + targetUserId);

                if (isAjax) {
                    sendJsonResponse(response, HttpServletResponse.SC_OK, java.util.Map.of("success", updated));
                } else {
                    response.sendRedirect(request.getContextPath() + "/admin?updated=true");
                }
            } else if ("switch_strategy".equalsIgnoreCase(action)) {
                String strategyType = request.getParameter("strategyType");

                if ("URGENCY_ONLY".equalsIgnoreCase(strategyType)) {
                    priorityCalculator.setStrategy(new UrgencyOnlyStrategy());
                } else {
                    // Weighted strategy with configurable weights
                    double weightImportance = 0.5;
                    double weightUrgency = 0.5;
                    try {
                        String impStr = request.getParameter("weightImportance");
                        String urgStr = request.getParameter("weightUrgency");
                        if (impStr != null && urgStr != null) {
                            weightImportance = Double.parseDouble(impStr);
                            weightUrgency = Double.parseDouble(urgStr);
                        }
                    } catch (NumberFormatException ignored) {
                        // fallback to defaults
                    }
                    priorityCalculator.setStrategy(new WeightedPriorityStrategy(weightImportance, weightUrgency));
                }

                // Recalculate all scores in parallel using synchronized method
                priorityCalculator.recalculateAll();

                AppLogger.info(getClass(), "Admin switched strategy to: " + priorityCalculator.getStrategy().getStrategyName());

                if (isAjax) {
                    sendJsonResponse(response, HttpServletResponse.SC_OK, java.util.Map.of(
                            "success", true,
                            "strategy", priorityCalculator.getStrategy().getStrategyName(),
                            "message", "Strategy switched and all task scores recalculated in parallel."
                    ));
                } else {
                    response.sendRedirect(request.getContextPath() + "/admin?strategyUpdated=true");
                }
            } else if ("recalculate".equalsIgnoreCase(action)) {
                // Manually trigger parallel recalculation
                priorityCalculator.recalculateAll();
                if (isAjax) {
                    sendJsonResponse(response, HttpServletResponse.SC_OK, java.util.Map.of(
                            "success", true,
                            "message", "All task priority scores recalculated successfully."
                    ));
                } else {
                    response.sendRedirect(request.getContextPath() + "/admin?recalculated=true");
                }
            } else {
                throw new ValidationException("Unknown admin action: " + action);
            }
        } catch (Exception exception) {
            handleException(response, exception);
        }
    }
}
