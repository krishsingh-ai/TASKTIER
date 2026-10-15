package com.tasktier.servlet;

import com.tasktier.dao.CategoryDAO;
import com.tasktier.exception.ValidationException;
import com.tasktier.model.Category;
import com.tasktier.util.AppLogger;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Servlet handling CRUD operations for Task categories.
 * URL Pattern: /categories
 */
@WebServlet(name = "CategoryServlet", urlPatterns = {"/categories"})
public class CategoryServlet extends BaseServlet {

    private final CategoryDAO categoryDAO;

    /**
     * Default constructor initializing CategoryDAO.
     */
    public CategoryServlet() {
        this.categoryDAO = new CategoryDAO();
    }

    /**
     * Parameterized constructor for dependency injection and testing.
     *
     * @param categoryDAO category data access object
     */
    public CategoryServlet(CategoryDAO categoryDAO) {
        this.categoryDAO = categoryDAO;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<Category> categories = categoryDAO.findAll();
            sendJsonResponse(response, HttpServletResponse.SC_OK, categories);
        } catch (Exception exception) {
            handleException(response, exception);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String name = request.getParameter("name");
        String description = request.getParameter("description");
        String color = request.getParameter("color");

        String contentType = request.getContentType();
        boolean isJsonRequest = contentType != null && contentType.toLowerCase().contains("application/json");

        if (name == null && isJsonRequest) {
            try {
                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader = request.getReader()) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                }
                String body = sb.toString();
                if (!body.trim().isEmpty()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> jsonMap = com.tasktier.util.JsonUtil.fromJson(body, Map.class);
                    if (jsonMap != null) {
                        if (jsonMap.get("name") != null) {
                            name = String.valueOf(jsonMap.get("name"));
                        }
                        if (jsonMap.get("description") != null) {
                            description = String.valueOf(jsonMap.get("description"));
                        }
                        if (jsonMap.get("color") != null) {
                            color = String.valueOf(jsonMap.get("color"));
                        }
                    }
                }
            } catch (Exception ex) {
                AppLogger.warn(getClass(), "Failed to parse category JSON payload: " + ex.getMessage());
            }
        }

        try {
            if (name == null || name.trim().isEmpty()) {
                sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Category name is required.");
                return;
            }

            String trimmedName = name.trim();
            if (trimmedName.length() > 50) {
                sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Category name cannot exceed 50 characters.");
                return;
            }

            if (categoryDAO.findByName(trimmedName).isPresent()) {
                sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "A category with that name already exists");
                return;
            }

            if (color == null || color.trim().isEmpty()) {
                color = "#8b5cf6";
            }

            Category category = new Category(trimmedName, description, color.trim());
            Category saved = categoryDAO.insert(category);
            AppLogger.info(getClass(), "Category created: " + saved.getName());

            boolean isAjax = isJsonRequest ||
                    "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With")) ||
                    (request.getHeader("Accept") != null && request.getHeader("Accept").contains("application/json"));

            if (isAjax) {
                sendJsonResponse(response, HttpServletResponse.SC_CREATED, saved);
            } else {
                response.sendRedirect(request.getContextPath() + "/dashboard?categoryCreated=true");
            }
        } catch (Exception exception) {
            handleException(response, exception);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        String name = request.getParameter("name");
        String description = request.getParameter("description");
        String color = request.getParameter("color");

        try {
            if (idStr == null || idStr.trim().isEmpty()) {
                throw new ValidationException("Category ID is required for update.");
            }

            int id;
            try {
                id = Integer.parseInt(idStr.trim());
            } catch (NumberFormatException nfe) {
                throw new ValidationException("Invalid category ID format.");
            }
            var existingOptional = categoryDAO.findById(id);
            if (existingOptional.isEmpty()) {
                sendJsonError(response, HttpServletResponse.SC_NOT_FOUND, "Category not found.");
                return;
            }

            Category category = existingOptional.get();
            if (name != null && !name.trim().isEmpty()) {
                category.setName(name.trim());
            }
            if (description != null) {
                category.setDescription(description.trim());
            }
            if (color != null && !color.trim().isEmpty()) {
                category.setColor(color.trim());
            }

            boolean updated = categoryDAO.update(category);
            sendJsonResponse(response, HttpServletResponse.SC_OK, java.util.Map.of(
                    "success", updated,
                    "category", category
            ));
        } catch (Exception exception) {
            handleException(response, exception);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");

        try {
            if (idStr == null || idStr.trim().isEmpty()) {
                throw new ValidationException("Category ID is required for delete.");
            }

            int id;
            try {
                id = Integer.parseInt(idStr.trim());
            } catch (NumberFormatException nfe) {
                throw new ValidationException("Invalid category ID format.");
            }
            boolean deleted = categoryDAO.delete(id);
            sendJsonResponse(response, HttpServletResponse.SC_OK, java.util.Map.of(
                    "success", deleted,
                    "message", "Category deleted."
            ));
        } catch (Exception exception) {
            handleException(response, exception);
        }
    }
}
