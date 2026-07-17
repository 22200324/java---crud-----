package org.example.repository;

import org.example.model.WorkoutRecord;

import java.util.List;
import java.util.Optional;

public interface WorkoutRecordRepository {

    WorkoutRecord save(WorkoutRecord record);

    List<WorkoutRecord> findAll();

    Optional<WorkoutRecord> findById(Long id);

    List<WorkoutRecord> findByExerciseNameContaining(String keyword);

    boolean update(WorkoutRecord record);

    boolean deleteById(Long id);
}
