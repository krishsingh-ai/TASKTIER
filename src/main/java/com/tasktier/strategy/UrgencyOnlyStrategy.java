package com.tasktier.strategy;

import com.tasktier.model.Task;
import com.tasktier.util.DateUtil;

/**
 * Prioritization strategy evaluating tasks purely on temporal urgency (deadline proximity).
 * Ignores task importance weights completely.
 */
public class UrgencyOnlyStrategy implements PriorityStrategy {

    /**
     * Calculates priority score based solely on remaining time until the deadline.
     *
     * @param task the task to evaluate
     * @return priority score between 0.0 and 100.0
     */
    @Override
    public double calculate(Task task) {
        if (task == null) {
            return 0.0;
        }

        if (task.getDeadline() == null) {
            return 25.0; // Fixed baseline for tasks without deadlines
        }

        double hoursLeft = DateUtil.hoursUntil(task.getDeadline());

        double urgencyScore;
        if (hoursLeft <= 0.0) {
            urgencyScore = 100.0; // Overdue
        } else if (hoursLeft <= 12.0) {
            urgencyScore = 95.0 - (hoursLeft / 12.0) * 10.0;
        } else if (hoursLeft <= 48.0) {
            urgencyScore = 85.0 - ((hoursLeft - 12.0) / 36.0) * 25.0;
        } else if (hoursLeft <= 168.0) {
            urgencyScore = 60.0 - ((hoursLeft - 48.0) / 120.0) * 35.0;
        } else {
            urgencyScore = Math.max(5.0, 25.0 - (hoursLeft / 336.0) * 20.0);
        }

        double boundedScore = Math.max(0.0, Math.min(100.0, urgencyScore));
        return Math.round(boundedScore * 10.0) / 10.0;
    }

    /**
     * Returns the human-readable display name of the strategy.
     *
     * @return strategy display name
     */
    @Override
    public String getStrategyName() {
        return "Urgency-Only Strategy";
    }

    /**
     * Returns a summary description of how the strategy computes priority scores.
     *
     * @return strategy description
     */
    @Override
    public String getDescription() {
        return "Ranks tasks exclusively by deadline urgency. Imminent deadlines receive highest rank.";
    }
}
