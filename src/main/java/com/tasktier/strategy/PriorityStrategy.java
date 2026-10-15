package com.tasktier.strategy;

import com.tasktier.model.Task;

/**
 * Strategy interface defining the contract for calculating task priority scores.
 * Demonstrates the Strategy design pattern and runtime polymorphism.
 */
public interface PriorityStrategy {

    /**
     * Calculates a priority score between 0.0 and 100.0 for the given task.
     *
     * @param task the task to evaluate
     * @return calculated priority score (0.0 to 100.0)
     */
    double calculate(Task task);

    /**
     * Returns the human-readable name of the prioritization strategy.
     *
     * @return strategy display name
     */
    String getStrategyName();

    /**
     * Returns a brief description of the strategy calculation logic.
     *
     * @return strategy description
     */
    String getDescription();
}
