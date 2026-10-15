package com.tasktier.servlet;

import com.tasktier.dao.CategoryDAO;
import com.tasktier.exception.ValidationException;
import com.tasktier.model.Priority;
import com.tasktier.model.Task;
import com.tasktier.model.TaskStatus;
import com.tasktier.service.TaskService;
import com.tasktier.service.TimeTrackerService;
import com.tasktier.util.AppLogger;
import com.tasktier.util.DateUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * RESTful HTTP Servlet managing Task lifecycle operations.
 * Demonstrates doGet, doPost, doPut, and doDelete endpoints with JSON and JSP rendering.
 * URL Pattern: /tasks
 */
@WebServlet(name = "TaskServlet", urlPatterns = {"/tasks", "/tasks/*", "/tasks.jsp"})
public class TaskServlet extends BaseServlet {

    private final TaskService taskService;
    private final CategoryDAO categoryDAO;
    private final TimeTrackerService timeTrackerService;

    /**
     * Default constructor initializing TaskService, CategoryDAO, and TimeTrackerService.
     */
    public TaskServlet() {
        this.taskService = new TaskService();
        this.categoryDAO = new CategoryDAO();
        this.timeTrackerService = new TimeTrackerService();
    }

    /**
     * Parameterized constructor for dependency injection and testing.
     *
     * @param taskService task management service
     * @param categoryDAO category data access object
     * @param timeTrackerService time tracking service
     */
    public TaskServlet(TaskService taskService, CategoryDAO categoryDAO, TimeTrackerService timeTrackerService) {
        this.taskService = taskService;
        this.categoryDAO = categoryDAO;
        this.timeTrackerService = timeTrackerService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer userId = getAuthenticatedUserId(request);
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }

        String idParam = request.getParameter("id");
        String acceptHeader = request.getHeader("Accept");
        boolean wantsJson = acceptHeader != null && acceptHeader.contains("application/json");

        try {
            boolean isTasksJspRequest = request.getRequestURI().endsWith("tasks.jsp");
            if (idParam != null && !idParam.trim().isEmpty()) {
                // View specific task detail
                int taskId;
                try {
                    taskId = Integer.parseInt(idParam.trim());
                } catch (NumberFormatException nfe) {
                    throw new ValidationException("Invalid task ID format: " + idParam);
                }
                Optional<Task> taskOptional = taskService.getTaskById(taskId, userId);

                if (taskOptional.isEmpty()) {
                    if (wantsJson) {
                        sendJsonError(response, HttpServletResponse.SC_NOT_FOUND, "Task not found.");
                    } else {
                        response.sendRedirect(request.getContextPath() + "/dashboard?error=not_found");
                    }
                    return;
                }

                Task task = taskOptional.get();

                if (wantsJson) {
                    sendJsonResponse(response, HttpServletResponse.SC_OK, task);
                } else {
                    // Populate task detail view attributes
                    request.setAttribute("task", task);
                    request.setAttribute("timeEntries", timeTrackerService.getTaskTimeLogs(taskId));
                    request.setAttribute("activeTimer", timeTrackerService.getActiveTimer(userId, taskId).orElse(null));
                    request.getRequestDispatcher("/WEB-INF/views/tasks.jsp").forward(request, response);
                }
            } else if (isTasksJspRequest) {
                // Direct request to /tasks.jsp: display first ranked task or friendly placeholder
                List<Task> rankedTasks = taskService.getRankedTasks(userId);
                Task taskToView;
                if (!rankedTasks.isEmpty()) {
                    taskToView = rankedTasks.get(0);
                } else {
                    taskToView = new Task(userId, "Sample Focus Task", "Create a task from the dashboard to prioritize and track effort.", null, 1.0, null, Priority.MEDIUM);
                    taskToView.setPriorityScore(50.0);
                }
                request.setAttribute("task", taskToView);
                request.setAttribute("timeEntries", taskToView.getId() > 0 ? timeTrackerService.getTaskTimeLogs(taskToView.getId()) : java.util.Collections.emptyList());
                request.setAttribute("activeTimer", taskToView.getId() > 0 ? timeTrackerService.getActiveTimer(userId, taskToView.getId()).orElse(null) : null);
                request.getRequestDispatcher("/WEB-INF/views/tasks.jsp").forward(request, response);
            } else {
                // List all tasks for user (ranked by priority score)
                List<Task> rankedTasks = taskService.getRankedTasks(userId);

                if (wantsJson) {
                    sendJsonResponse(response, HttpServletResponse.SC_OK, rankedTasks);
                } else {
                    request.setAttribute("tasks", rankedTasks);
                    request.setAttribute("categories", categoryDAO.findAll());
                    request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(request, response);
                }
            }
        } catch (Exception exception) {
            handleException(response, exception);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer userId = getAuthenticatedUserId(request);
        if (userId == null) {
            sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return;
        }

        String acceptHeader = request.getHeader("Accept");
        boolean isAjax = (acceptHeader != null && acceptHeader.contains("application/json")) ||
                "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"));

        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String categoryIdStr = request.getParameter("categoryId");
        String estimatedHoursStr = request.getParameter("estimatedHours");
        String deadlineStr = request.getParameter("deadline");
        if (deadlineStr == null || deadlineStr.trim().isEmpty()) {
            String deadlineDate = request.getParameter("deadlineDate");
            String deadlineTime = request.getParameter("deadlineTime");
            if (deadlineDate != null && !deadlineDate.trim().isEmpty()) {
                if (deadlineTime == null || deadlineTime.trim().isEmpty()) {
                    deadlineTime = "23:59";
                }
                deadlineStr = deadlineDate.trim() + "T" + deadlineTime.trim() + (deadlineTime.trim().length() == 5 ? ":00" : "");
            }
        }
        String importanceStr = request.getParameter("importance");

        try {
            Integer categoryId = null;
            if (categoryIdStr != null && !categoryIdStr.trim().isEmpty()) {
                try {
                    categoryId = Integer.parseInt(categoryIdStr.trim());
                } catch (NumberFormatException nfe) {
                    throw new ValidationException("Invalid category ID format.");
                }
            }

            Double estimatedHours = null;
            if (estimatedHoursStr != null && !estimatedHoursStr.trim().isEmpty()) {
                try {
                    estimatedHours = Double.parseDouble(estimatedHoursStr.trim());
                } catch (NumberFormatException nfe) {
                    throw new ValidationException("Estimated hours must be a valid number.");
                }
            }

            Task created = taskService.createTask(userId, title, description, categoryId,
                    estimatedHours, deadlineStr, importanceStr);

            AppLogger.info(getClass(), "Task created: ID " + created.getId() + " for user " + userId);

            if (isAjax) {
                sendJsonResponse(response, HttpServletResponse.SC_CREATED, java.util.Map.of(
                        "success", true,
                        "message", "Task created successfully!",
                        "task", created
                ));
            } else {
                response.sendRedirect(request.getContextPath() + "/dashboard?created=true");
            }
        } catch (Exception exception) {
            handleException(response, exception);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer userId = getAuthenticatedUserId(request);
        if (userId == null) {
            sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return;
        }

        String taskIdStr = request.getParameter("id");
        String statusStr = request.getParameter("status");
        String title = request.getParameter("title");

        try {
            if (taskIdStr == null || taskIdStr.trim().isEmpty()) {
                throw new ValidationException("Task ID is required for update.");
            }

            int taskId;
            try {
                taskId = Integer.parseInt(taskIdStr.trim());
            } catch (NumberFormatException nfe) {
                throw new ValidationException("Invalid task ID format.");
            }

            // Check if status update only
            if (statusStr != null && !statusStr.trim().isEmpty()) {
                TaskStatus newStatus = TaskStatus.fromString(statusStr);
                boolean updated = taskService.updateTaskStatus(taskId, userId, newStatus);
                sendJsonResponse(response, HttpServletResponse.SC_OK, java.util.Map.of(
                        "success", updated,
                        "message", "Task status updated to " + newStatus.getDisplayName()
                ));
                return;
            }

            // Full task edit
            Optional<Task> existing = taskService.getTaskById(taskId, userId);
            if (existing.isEmpty()) {
                sendJsonError(response, HttpServletResponse.SC_NOT_FOUND, "Task not found.");
                return;
            }

            Task task = existing.get();
            if (title != null && !title.trim().isEmpty()) {
                task.setTitle(title.trim());
            }

            String description = request.getParameter("description");
            if (description != null) {
                task.setDescription(description.trim());
            }

            String categoryIdStr = request.getParameter("categoryId");
            if (categoryIdStr != null && !categoryIdStr.trim().isEmpty()) {
                try {
                    task.setCategoryId(Integer.parseInt(categoryIdStr.trim()));
                } catch (NumberFormatException nfe) {
                    throw new ValidationException("Invalid category ID format.");
                }
            }

            String estimatedHoursStr = request.getParameter("estimatedHours");
            if (estimatedHoursStr != null && !estimatedHoursStr.trim().isEmpty()) {
                try {
                    task.setEstimatedHours(Double.parseDouble(estimatedHoursStr.trim()));
                } catch (NumberFormatException nfe) {
                    throw new ValidationException("Estimated hours must be a valid number.");
                }
            }

            String deadlineStr = request.getParameter("deadline");
            if (deadlineStr != null && !deadlineStr.trim().isEmpty()) {
                task.setDeadline(DateUtil.parse(deadlineStr));
            }

            String importanceStr = request.getParameter("importance");
            if (importanceStr != null && !importanceStr.trim().isEmpty()) {
                task.setImportance(Priority.fromString(importanceStr));
            }

            boolean updated = taskService.updateTask(task, userId);
            sendJsonResponse(response, HttpServletResponse.SC_OK, java.util.Map.of(
                    "success", updated,
                    "message", "Task updated successfully",
                    "task", task
            ));
        } catch (Exception exception) {
            handleException(response, exception);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer userId = getAuthenticatedUserId(request);
        if (userId == null) {
            sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return;
        }

        String taskIdStr = request.getParameter("id");
        String acceptHeader = request.getHeader("Accept");
        boolean isAjax = (acceptHeader != null && acceptHeader.contains("application/json")) ||
                "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"));

        try {
            if (taskIdStr == null || taskIdStr.trim().isEmpty()) {
                throw new ValidationException("Task ID is required for deletion.");
            }

            int taskId;
            try {
                taskId = Integer.parseInt(taskIdStr.trim());
            } catch (NumberFormatException nfe) {
                throw new ValidationException("Invalid task ID format.");
            }
            boolean deleted = taskService.deleteTask(taskId, userId);

            if (isAjax) {
                sendJsonResponse(response, HttpServletResponse.SC_OK, java.util.Map.of(
                        "success", deleted,
                        "message", "Task deleted successfully."
                ));
            } else {
                response.sendRedirect(request.getContextPath() + "/dashboard?deleted=true");
            }
        } catch (Exception exception) {
            handleException(response, exception);
        }
    }
}
