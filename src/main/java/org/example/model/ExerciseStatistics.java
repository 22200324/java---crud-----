package org.example.model;

public record ExerciseStatistics(
        String exerciseName,
        int recordCount,
        int totalSets,
        double maxWeight,
        double totalVolume
) {
}
