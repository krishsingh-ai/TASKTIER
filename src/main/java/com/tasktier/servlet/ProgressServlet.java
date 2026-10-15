package com.tasktier.servlet;

import com.tasktier.dao.CategoryDAO;
import com.tasktier.model.Category;
import com.tasktier.model.Task;
import com.tasktier.model.TaskStatus;
import com.tasktier.service.TaskService;
import com.tasktier.service.TimeTrackerService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller servlet for the progress and analytics view.
 * Computes completion metrics, category distribution, and time tracking summaries.
 * URL Pattern: /progress
 */
@WebServlet(name = "ProgressServlet", urlPatterns = {"/progress", "/progress.jsp"})
public class ProgressServlet extends BaseServlet {

    private final TaskService taskService;
    private final TimeTrackerService timeTrackerService;
    private final CategoryDAO categoryDAO;

    /**
     * Default constructor initializing task and time-tracking services.
     */
    public ProgressServlet() {
        this.taskService = new TaskService();
        this.timeTrackerService = new TimeTrackerService();
        this.categoryDAO = new CategoryDAO();
    }

    /**
     * Parameterized constructor for dependency injection and testing.
     *
     * @param taskService task management service
     * @param timeTrackerService time tracking service
     * @param categoryDAO category data access object
     */
    public ProgressServlet(TaskService taskService, TimeTrackerService timeTrackerService, CategoryDAO categoryDAO) {
        this.taskService = taskService;
        this.timeTrackerService = timeTrackerService;
        this.categoryDAO = categoryDAO;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer userId = getAuthenticatedUserId(request);
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }

        try {
            List<Task> tasks = taskService.getUserTasks(userId);
            long totalMinutes = timeTrackerService.getTotalUserMinutes(userId);
            double totalHours = Math.round((totalMinutes / 60.0) * 10.0) / 10.0;

            int totalCount = tasks.size();
            long completedCount = tasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
            long pendingCount = tasks.stream().filter(t -> t.getStatus() == TaskStatus.PENDING).count();
            long inProgressCount = tasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).count();
            long overdueCount = tasks.stream().filter(t -> t.getStatus() == TaskStatus.OVERDUE).count();

            int completionPercent = totalCount > 0 ? (int) Math.round(((double) completedCount / totalCount) * 100.0) : 0;

            // Tier distribution using Streams
            long criticalCount = tasks.stream().filter(t -> "Critical".equals(t.getTier())).count();
            long highCount = tasks.stream().filter(t -> "High".equals(t.getTier())).count();
            long mediumCount = tasks.stream().filter(t -> "Medium".equals(t.getTier())).count();
            long lowCount = tasks.stream().filter(t -> "Low".equals(t.getTier())).count();

            // Category breakdown using Streams
            List<Category> allCategories = categoryDAO.findAll();
            Map<String, Long> categoryTaskCounts = tasks.stream()
                    .collect(Collectors.groupingBy(
                            t -> t.getCategoryName() != null ? t.getCategoryName() : "Uncategorized",
                            Collectors.counting()
                    ));

            request.setAttribute("totalCount", totalCount);
            request.setAttribute("completedCount", completedCount);
            request.setAttribute("pendingCount", pendingCount);
            request.setAttribute("inProgressCount", inProgressCount);
            request.setAttribute("overdueCount", overdueCount);
            request.setAttribute("completionPercent", completionPercent);
            request.setAttribute("totalHours", totalHours);
            request.setAttribute("totalMinutes", totalMinutes);

            request.setAttribute("criticalCount", criticalCount);
            request.setAttribute("highCount", highCount);
            request.setAttribute("mediumCount", mediumCount);
            request.setAttribute("lowCount", lowCount);

            request.setAttribute("categoryTaskCounts", categoryTaskCounts);
            request.setAttribute("allCategories", allCategories);

            request.getRequestDispatcher("/WEB-INF/views/progress.jsp").forward(request, response);
        } catch (Exception exception) {
            handleException(response, exception);
        }
    }
}
