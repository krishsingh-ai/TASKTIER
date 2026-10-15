package com.tasktier.servlet;

import com.tasktier.dao.CategoryDAO;
import com.tasktier.model.Category;
import com.tasktier.model.Task;
import com.tasktier.service.TaskService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Controller servlet for the main application dashboard.
 * URL Pattern: /dashboard
 */
@WebServlet(name = "DashboardServlet", urlPatterns = {"/dashboard", "/dashboard.jsp"})
public class DashboardServlet extends BaseServlet {

    private final TaskService taskService;
    private final CategoryDAO categoryDAO;

    /**
     * Default constructor initializing TaskService and CategoryDAO.
     */
    public DashboardServlet() {
        this.taskService = new TaskService();
        this.categoryDAO = new CategoryDAO();
    }

    /**
     * Parameterized constructor for dependency injection and testing.
     *
     * @param taskService task business service
     * @param categoryDAO category data access object
     */
    public DashboardServlet(TaskService taskService, CategoryDAO categoryDAO) {
        this.taskService = taskService;
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
            List<Task> rankedTasks = taskService.getRankedTasks(userId);
            List<Category> categories = categoryDAO.findAll();

            long totalTasks = rankedTasks.size();
            long completedTasks = rankedTasks.stream().filter(t -> t.getStatus().name().equals("COMPLETED")).count();
            long pendingTasks = totalTasks - completedTasks;
            long criticalTasks = rankedTasks.stream().filter(t -> "Critical".equals(t.getTier()) && !t.getStatus().name().equals("COMPLETED")).count();

            request.setAttribute("tasks", rankedTasks);
            request.setAttribute("categories", categories);
            request.setAttribute("totalTasks", totalTasks);
            request.setAttribute("completedTasks", completedTasks);
            request.setAttribute("pendingTasks", pendingTasks);
            request.setAttribute("criticalTasks", criticalTasks);

            request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(request, response);
        } catch (Exception exception) {
            handleException(response, exception);
        }
    }
}
