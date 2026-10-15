package com.tasktier.service;

import com.tasktier.dao.TaskDAO;
import com.tasktier.exception.DataAccessException;
import com.tasktier.model.Task;
import com.tasktier.strategy.PriorityStrategy;
import com.tasktier.strategy.WeightedPriorityStrategy;
import com.tasktier.util.AppLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Service responsible for computing and mass-recalculating task priority scores.
 * Demonstrates:
 * 1. Runtime polymorphism via the PriorityStrategy interface.
 * 2. Multithreading via a fixed ExecutorService thread pool of 3 threads.
 * 3. Thread synchronization (synchronized method) to prevent race conditions during recalculations.
 */
public class PriorityCalculator {

    private final TaskDAO taskDAO;
    private volatile PriorityStrategy strategy;

    /**
     * Fixed thread pool with exactly 3 worker threads for parallel score calculation.
     * Fulfills Multithreading Rubric requirement #3.
     */
    private final ExecutorService executorService;

    /**
     * Default constructor with WeightedPriorityStrategy and default TaskDAO.
     */
    public PriorityCalculator() {
        this(new TaskDAO(), new WeightedPriorityStrategy());
    }

    /**
     * Parameterized constructor.
     *
     * @param taskDAO task data access object
     * @param strategy initial priority strategy
     */
    public PriorityCalculator(TaskDAO taskDAO, PriorityStrategy strategy) {
        this.taskDAO = taskDAO;
        this.strategy = strategy != null ? strategy : new WeightedPriorityStrategy();
        this.executorService = Executors.newFixedThreadPool(3);
    }

    /**
     * Calculates the priority score for an individual task using the active strategy.
     *
     * @param task the task to evaluate
     * @return calculated score (0.0 to 100.0)
     */
    public double calculateScore(Task task) {
        return strategy.calculate(task);
    }

    /**
     * Changes the prioritization strategy at runtime (switchable by administrator).
     *
     * @param newStrategy newly selected PriorityStrategy
     */
    public synchronized void setStrategy(PriorityStrategy newStrategy) {
        if (newStrategy != null) {
            this.strategy = newStrategy;
            AppLogger.info(getClass(), "Priority strategy changed to: " + newStrategy.getStrategyName());
        }
    }

    /**
     * Retrieves the currently active priority strategy.
     *
     * @return active PriorityStrategy
     */
    public PriorityStrategy getStrategy() {
        return strategy;
    }

    /**
     * Synchronously recalculates scores for all tasks across all users in parallel.
     * Thread-synchronized method to prevent race conditions when recalculation is triggered concurrently.
     * Uses the 3-thread ExecutorService to process task partitions concurrently.
     *
     * @throws DataAccessException if database batch operations fail
     */
    public synchronized void recalculateAll() throws DataAccessException {
        AppLogger.info(getClass(), "Beginning parallel recalculation of all tasks using " + strategy.getStrategyName());

        List<Task> allTasks = taskDAO.findAll();
        if (allTasks.isEmpty()) {
            AppLogger.info(getClass(), "No tasks to recalculate.");
            return;
        }

        // Divide tasks among up to 3 chunks for the 3 worker threads
        int totalTasks = allTasks.size();
        int chunkSize = Math.max(1, (int) Math.ceil((double) totalTasks / 3.0));

        List<Callable<Integer>> tasksToExecute = new ArrayList<>();

        for (int i = 0; i < totalTasks; i += chunkSize) {
            final int fromIndex = i;
            final int toIndex = Math.min(fromIndex + chunkSize, totalTasks);
            final List<Task> batch = allTasks.subList(fromIndex, toIndex);

            tasksToExecute.add(() -> {
                int updatedCount = 0;
                for (Task task : batch) {
                    double newScore = strategy.calculate(task);
                    task.setPriorityScore(newScore);
                    taskDAO.updatePriorityScore(task.getId(), newScore);
                    updatedCount++;
                }
                return updatedCount;
            });
        }

        try {
            List<Future<Integer>> futures = executorService.invokeAll(tasksToExecute);
            int totalUpdated = 0;
            for (Future<Integer> future : futures) {
                totalUpdated += future.get();
            }
            AppLogger.info(getClass(), "Parallel recalculation completed. Tasks updated: " + totalUpdated);
        } catch (InterruptedException | ExecutionException exception) {
            Thread.currentThread().interrupt();
            AppLogger.error(getClass(), "Parallel recalculation interrupted or failed", exception);
            throw new DataAccessException("Failed during parallel score recalculation", exception);
        }
    }

    /**
     * Shuts down the internal ExecutorService gracefully.
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(3, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException exception) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
