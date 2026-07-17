package org.example.model;

import java.util.List;

public record WorkoutStatistics(
        int totalRecordCount,
        int totalSets,
        double totalVolume,
        List<ExerciseStatistics> exerciseStatistics
) {
    public WorkoutStatistics {
        exerciseStatistics = List.copyOf(exerciseStatistics);
    }
}
