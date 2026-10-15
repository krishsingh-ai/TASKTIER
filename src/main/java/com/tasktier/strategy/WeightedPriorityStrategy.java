package com.tasktier.strategy;

import com.tasktier.model.Task;
import com.tasktier.util.DateUtil;

/**
 * Default prioritization strategy combining task importance and deadline urgency.
 * Formula: Score = (importanceScore * W1) + (urgencyScore * W2) where W1 + W2 = 1.0.
 */
public class WeightedPriorityStrategy implements PriorityStrategy {

    private double weightImportance = 0.5;
    private double weightUrgency = 0.5;

    /**
     * Default constructor with balanced weights (0.5 importance, 0.5 urgency).
     */
    public WeightedPriorityStrategy() {
        this(0.5, 0.5);
    }

    /**
     * Parameterized constructor allowing custom weighting.
     *
     * @param weightImportance weight assigned to importance (0.0 to 1.0)
     * @param weightUrgency weight assigned to urgency (0.0 to 1.0)
     */
    public WeightedPriorityStrategy(double weightImportance, double weightUrgency) {
        setWeights(weightImportance, weightUrgency);
    }

    /**
     * Sets weights and normalizes if needed.
     *
     * @param weightImportance importance weight
     * @param weightUrgency urgency weight
     */
    public final synchronized void setWeights(double weightImportance, double weightUrgency) {
        double total = weightImportance + weightUrgency;
        if (total <= 0.0) {
            this.weightImportance = 0.5;
            this.weightUrgency = 0.5;
        } else {
            this.weightImportance = weightImportance / total;
            this.weightUrgency = weightUrgency / total;
        }
    }

    /**
     * Calculates composite priority score balancing importance and temporal urgency.
     *
     * @param task the task to evaluate
     * @return priority score bounded between 0.0 and 100.0
     */
    @Override
    public double calculate(Task task) {
        if (task == null) {
            return 0.0;
        }

        // 1. Calculate Importance Score (0 - 100)
        // LOW = 25, MEDIUM = 50, HIGH = 75, URGENT = 100
        int weight = task.getImportance() != null ? task.getImportance().getWeight() : 2;
        double importanceScore = (weight / 4.0) * 100.0;

        // 2. Calculate Urgency Score (0 - 100) based on deadline proximity
        double urgencyScore = computeUrgency(task);

        // 3. Compute weighted aggregate score
        double rawScore = (importanceScore * weightImportance) + (urgencyScore * weightUrgency);
        double boundedScore = Math.max(0.0, Math.min(100.0, rawScore));

        // Round to 1 decimal place
        return Math.round(boundedScore * 10.0) / 10.0;
    }

    private double computeUrgency(Task task) {
        if (task.getDeadline() == null) {
            return 30.0; // Default baseline urgency when no deadline is specified
        }

        double hoursLeft = DateUtil.hoursUntil(task.getDeadline());

        if (hoursLeft <= 0.0) {
            return 100.0; // Overdue tasks receive maximum urgency
        } else if (hoursLeft <= 24.0) {
            // Due within 24 hours: 80.0 to 99.0
            return 99.0 - (hoursLeft / 24.0) * 19.0;
        } else if (hoursLeft <= 72.0) {
            // Due within 3 days: 60.0 to 80.0
            return 80.0 - ((hoursLeft - 24.0) / 48.0) * 20.0;
        } else if (hoursLeft <= 168.0) {
            // Due within 1 week: 40.0 to 60.0
            return 60.0 - ((hoursLeft - 72.0) / 96.0) * 20.0;
        } else {
            // Beyond 1 week: drops towards 10.0
            return Math.max(10.0, 40.0 - ((hoursLeft - 168.0) / 336.0) * 30.0);
        }
    }

    /**
     * Returns strategy display name.
     *
     * @return strategy name
     */
    @Override
    public String getStrategyName() {
        return "Weighted Priority (Importance + Urgency)";
    }

    /**
     * Returns human-readable strategy description.
     *
     * @return description formatted with weights
     */
    @Override
    public String getDescription() {
        return String.format("Combines importance (%.0f%%) and deadline proximity (%.0f%%).",
                weightImportance * 100, weightUrgency * 100);
    }

    /**
     * Gets normalized weight allocated to task importance.
     *
     * @return importance weight
     */
    public double getWeightImportance() {
        return weightImportance;
    }

    /**
     * Gets normalized weight allocated to deadline urgency.
     *
     * @return urgency weight
     */
    public double getWeightUrgency() {
        return weightUrgency;
    }
}
