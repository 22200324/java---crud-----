package org.example.repository;

import org.example.exception.DataAccessException;
import org.example.model.WorkoutRecord;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class WorkoutRecordFileRepository implements WorkoutRecordRepository {

    private final Path filePath;

    public WorkoutRecordFileRepository() {
        this(Path.of("data", "record.txt"));
    }

    public WorkoutRecordFileRepository(Path filePath) {
        this.filePath = filePath;

        try {
            Path parent = filePath.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }
        } catch (IOException e) {
            throw new DataAccessException("파일 저장소를 초기화하지 못했습니다.", e);
        }
    }

    @Override
    public synchronized WorkoutRecord save(WorkoutRecord record) {
        List<WorkoutRecord> records = findAll();

        long maxId = 0L;

        for (WorkoutRecord existingRecord : records) {
            if (existingRecord.getId() > maxId) {
                maxId = existingRecord.getId();
            }
        }

        long nextId = maxId + 1;

        record.setId(nextId);

        String line = toLine(record);

        try {
            Files.writeString(
                    filePath,
                    line + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            throw new DataAccessException("운동 기록을 파일에 저장하지 못했습니다.", e);
        }

        return record;
    }

    @Override
    public synchronized List<WorkoutRecord> findAll() {
        try {
            List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
            List<WorkoutRecord> records = new ArrayList<>();

            for (String line : lines) {
                if (!line.isBlank()) {
                    records.add(fromLine(line));
                }
            }

            return records;
        } catch (IOException e) {
            throw new DataAccessException("운동 기록 파일을 읽지 못했습니다.", e);
        } catch (RuntimeException e) {
            throw new DataAccessException("운동 기록 파일의 형식이 올바르지 않습니다.", e);
        }
    }

    @Override
    public Optional<WorkoutRecord> findById(Long id) {
        return findAll().stream()
                .filter(record -> record.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<WorkoutRecord> findByExerciseNameContaining(String keyword) {
        String normalizedKeyword = keyword.toLowerCase(Locale.ROOT);

        return findAll().stream()
                .filter(record -> record.getExerciseName()
                        .toLowerCase(Locale.ROOT)
                        .contains(normalizedKeyword))
                .sorted(Comparator
                        .comparing(WorkoutRecord::getWorkoutDate)
                        .reversed()
                        .thenComparing(WorkoutRecord::getId, Comparator.reverseOrder()))
                .toList();
    }

    @Override
    public synchronized boolean update(WorkoutRecord record) {
        List<WorkoutRecord> records = findAll();
        boolean updated = false;

        for (int i = 0; i < records.size(); i++) {
            WorkoutRecord existingRecord = records.get(i);

            if (existingRecord.getId().equals(record.getId())) {
                records.set(i, record);
                updated = true;
                break;
            }
        }

        if (updated) {
            writeAll(records);
        }

        return updated;
    }

    private void writeAll(List<WorkoutRecord> records) {
        List<String> lines = records.stream()
                .map(this::toLine)
                .toList();

        try {
            Files.write(
                    filePath,
                    lines,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException e) {
            throw new DataAccessException("운동 기록 파일을 다시 쓰지 못했습니다.", e);
        }
    }


    @Override
    public synchronized boolean deleteById(Long id) {
        List<WorkoutRecord> records = findAll();
        boolean deleted = false;

        List<WorkoutRecord> remainingRecords = new ArrayList<>();

        for (WorkoutRecord record : records) {
            if (record.getId().equals(id)) {
                deleted = true;
            } else {
                remainingRecords.add(record);
            }
        }

        if (deleted) {
            writeAll(remainingRecords);
        }

        return deleted;
    }

    private String toLine(WorkoutRecord record) {
        return record.getId() + "|" +
                escape(record.getExerciseName()) + "|" +
                record.getWeight() + "|" +
                record.getReps() + "|" +
                record.getSets() + "|" +
                record.getWorkoutDate() + "|" +
                escape(record.getMemo());
    }

    private WorkoutRecord fromLine(String line) {
        List<String> parts = splitLine(line);

        if (parts.size() != 7) {
            throw new IllegalArgumentException("필드 개수가 7개가 아닙니다: " + line);
        }

        return new WorkoutRecord(
                Long.parseLong(parts.get(0)),
                parts.get(1),
                Double.parseDouble(parts.get(2)),
                Integer.parseInt(parts.get(3)),
                Integer.parseInt(parts.get(4)),
                LocalDate.parse(parts.get(5)),
                parts.get(6)
        );
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("|", "\\|");
    }

    private List<String> splitLine(String line) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char character = line.charAt(i);

            if (character == '\\' && i + 1 < line.length()) {
                char nextCharacter = line.charAt(i + 1);

                if (nextCharacter == '\\' || nextCharacter == '|') {
                    current.append(nextCharacter);
                    i++;
                    continue;
                }
            }

            if (character == '|') {
                parts.add(current.toString());
                current.setLength(0);
            } else {
                current.append(character);
            }
        }

        parts.add(current.toString());
        return parts;
    }
}
