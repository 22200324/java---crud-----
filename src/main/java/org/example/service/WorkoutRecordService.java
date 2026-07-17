package org.example.service;

import org.example.model.WorkoutRecord;
import org.example.repository.WorkoutRecordRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class WorkoutRecordService {
    private final WorkoutRecordRepository repository;

    public WorkoutRecordService(WorkoutRecordRepository repository) {
        this.repository = repository;
    }

    public WorkoutRecord addRecord(WorkoutRecord record) {
        validateRecord(record);
        return repository.save(record);
    }

    public List<WorkoutRecord> getAllRecords() {
        return repository.findAll();
    }

    public Optional<WorkoutRecord> getRecordById(Long id) {
        validateId(id, "조회");
        return repository.findById(id);
    }

    public List<WorkoutRecord> searchRecordsByExerciseName(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException("검색할 운동 이름을 입력해주세요.");
        }

        return repository.findByExerciseNameContaining(keyword.trim());
    }

    public List<WorkoutRecord> searchRecordsByDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("시작 날짜와 종료 날짜를 모두 입력해주세요.");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("시작 날짜는 종료 날짜보다 늦을 수 없습니다.");
        }

        return repository.findByWorkoutDateBetween(startDate, endDate);
    }

    public boolean updateRecord(WorkoutRecord record) {
        validateRecord(record);

        validateId(record.getId(), "수정");

        return repository.update(record);
    }

    public boolean deleteRecord(Long id) {
        validateId(id, "삭제");

        return repository.deleteById(id);
    }

    private void validateRecord(WorkoutRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("운동 기록이 비어 있습니다.");
        }

        if (record.getExerciseName() == null || record.getExerciseName().isBlank()) {
            throw new IllegalArgumentException("운동 이름은 비어 있을 수 없습니다.");
        }

        if (!Double.isFinite(record.getWeight()) || record.getWeight() < 0) {
            throw new IllegalArgumentException("무게는 0 이상이어야 합니다.");
        }

        if (record.getReps() <= 0) {
            throw new IllegalArgumentException("반복 횟수는 1 이상이어야 합니다.");
        }

        if (record.getSets() <= 0) {
            throw new IllegalArgumentException("세트 수는 1 이상이어야 합니다.");
        }

        if (record.getWorkoutDate() == null) {
            throw new IllegalArgumentException("운동 날짜는 비어 있을 수 없습니다.");
        }

        record.setExerciseName(record.getExerciseName().trim());
    }

    private void validateId(Long id, String action) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(action + "할 기록의 id는 1 이상이어야 합니다.");
        }
    }
}
