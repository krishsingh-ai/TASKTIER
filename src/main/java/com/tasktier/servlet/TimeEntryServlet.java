package com.tasktier.servlet;

import com.tasktier.exception.ValidationException;
import com.tasktier.model.TimeEntry;
import com.tasktier.service.TimeTrackerService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Servlet managing timer operations (start/stop) and time logging history.
 * URL Pattern: /time-entries
 */
@WebServlet(name = "TimeEntryServlet", urlPatterns = {"/time-entries"})
public class TimeEntryServlet extends BaseServlet {

    private final TimeTrackerService timeTrackerService;

    /**
     * Default constructor initializing TimeTrackerService.
     */
    public TimeEntryServlet() {
        this.timeTrackerService = new TimeTrackerService();
    }

    /**
     * Parameterized constructor for dependency injection and testing.
     *
     * @param timeTrackerService time tracking service
     */
    public TimeEntryServlet(TimeTrackerService timeTrackerService) {
        this.timeTrackerService = timeTrackerService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer userId = getAuthenticatedUserId(request);
        if (userId == null) {
            sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return;
        }

        String taskIdStr = request.getParameter("taskId");

        try {
            if (taskIdStr != null && !taskIdStr.trim().isEmpty()) {
                int taskId;
                try {
                    taskId = Integer.parseInt(taskIdStr.trim());
                } catch (NumberFormatException nfe) {
                    throw new ValidationException("Invalid task ID format.");
                }
                List<TimeEntry> entries = timeTrackerService.getTaskTimeLogs(taskId);
                sendJsonResponse(response, HttpServletResponse.SC_OK, entries);
            } else {
                List<TimeEntry> entries = timeTrackerService.getUserTimeLogs(userId);
                sendJsonResponse(response, HttpServletResponse.SC_OK, entries);
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

        String action = request.getParameter("action");
        String taskIdStr = request.getParameter("taskId");
        String notes = request.getParameter("notes");

        try {
            if (taskIdStr == null || taskIdStr.trim().isEmpty()) {
                throw new ValidationException("Task ID is required for timer operations.");
            }
            int taskId;
            try {
                taskId = Integer.parseInt(taskIdStr.trim());
            } catch (NumberFormatException nfe) {
                throw new ValidationException("Invalid task ID format.");
            }

            if ("start".equalsIgnoreCase(action)) {
                TimeEntry started = timeTrackerService.startTimer(userId, taskId, notes);
                sendJsonResponse(response, HttpServletResponse.SC_CREATED, java.util.Map.of(
                        "success", true,
                        "action", "started",
                        "entry", started,
                        "message", "Timer started successfully."
                ));
            } else if ("stop".equalsIgnoreCase(action)) {
                TimeEntry stopped = timeTrackerService.stopTimer(userId, taskId);
                sendJsonResponse(response, HttpServletResponse.SC_OK, java.util.Map.of(
                        "success", true,
                        "action", "stopped",
                        "entry", stopped,
                        "message", "Timer stopped. Duration: " + stopped.getDurationMinutes() + " minutes."
                ));
            } else {
                throw new ValidationException("Invalid timer action. Expected 'start' or 'stop'.");
            }
        } catch (Exception exception) {
            handleException(response, exception);
        }
    }
}
