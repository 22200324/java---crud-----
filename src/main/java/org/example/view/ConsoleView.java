package org.example.view;

import org.example.exception.DataAccessException;
import org.example.model.ExerciseStatistics;
import org.example.model.WorkoutRecord;
import org.example.model.WorkoutStatistics;
import org.example.service.WorkoutRecordService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class ConsoleView {
    private static final String RECORD_SEPARATOR = "-".repeat(90);

    private final WorkoutRecordService service;
    private final Scanner scanner;

    public ConsoleView(WorkoutRecordService service) {
        this(service, new Scanner(System.in));
    }

    public ConsoleView(WorkoutRecordService service, Scanner scanner) {
        this.service = service;
        this.scanner = scanner;
    }

    public void run() {
        while (true) {
            printMenu();

            int choice = readInt("메뉴 선택: ");

            try {
                switch (choice) {
                    case 1:
                        addRecord();
                        break;
                    case 2:
                        showAllRecords();
                        break;
                    case 3:
                        showRecordById();
                        break;
                    case 4:
                        updateRecord();
                        break;
                    case 5:
                        deleteRecord();
                        break;
                    case 6:
                        searchByExerciseName();
                        break;
                    case 7:
                        searchByDateRange();
                        break;
                    case 8:
                        showStatistics();
                        break;
                    case 0:
                        System.out.println("프로그램을 종료합니다.");
                        return;
                    default:
                        System.out.println("잘못된 메뉴입니다.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("입력 오류: " + e.getMessage());
            } catch (DataAccessException e) {
                System.out.println("데이터 처리 오류: " + e.getMessage());
            }
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("===== 운동 기록 관리 시스템 =====");
        System.out.println("1. 운동 기록 추가");
        System.out.println("2. 전체 기록 조회");
        System.out.println("3. 운동 기록 상세 조회");
        System.out.println("4. 운동 기록 수정");
        System.out.println("5. 운동 기록 삭제");
        System.out.println("6. 운동 이름으로 검색");
        System.out.println("7. 날짜 범위로 조회");
        System.out.println("8. 운동 통계");
        System.out.println("0. 종료");
    }

    private void addRecord() {
        System.out.println("\n[운동 기록 추가]");

        String exerciseName = readString("운동 이름: ");
        double weight = readDouble("무게: ");
        int reps = readInt("반복 횟수: ");
        int sets = readInt("세트 수: ");
        LocalDate workoutDate = readDate("운동 날짜(yyyy-MM-dd, 엔터 시 오늘): ", LocalDate.now());
        String memo = readString("메모: ");

        WorkoutRecord record = new WorkoutRecord(
                exerciseName,
                weight,
                reps,
                sets,
                workoutDate,
                memo
        );

        WorkoutRecord savedRecord = service.addRecord(record);
        System.out.println("운동 기록이 저장되었습니다.");
        printRecordDetails(savedRecord);
    }

    private void showAllRecords() {
        System.out.println("\n[전체 운동 기록 조회]");

        List<WorkoutRecord> records = service.getAllRecords();

        if (records.isEmpty()) {
            System.out.println("저장된 운동 기록이 없습니다.");
            return;
        }

        printRecordList(records);
    }

    private void showRecordById() {
        System.out.println("\n[운동 기록 상세 조회]");

        Long id = readLong("조회할 id: ");

        Optional<WorkoutRecord> record = service.getRecordById(id);

        if (record.isPresent()) {
            printRecordDetails(record.get());
        } else {
            System.out.println("해당 id의 운동 기록이 없습니다.");
        }
    }

    private void updateRecord() {
        System.out.println("\n[운동 기록 수정]");

        Long id = readLong("수정할 id: ");

        Optional<WorkoutRecord> foundRecord = service.getRecordById(id);

        if (foundRecord.isEmpty()) {
            System.out.println("해당 id의 운동 기록이 없습니다.");
            return;
        }

        WorkoutRecord existingRecord = foundRecord.get();
        System.out.println("현재 기록:");
        printRecordDetails(existingRecord);

        String exerciseName = readString("새 운동 이름: ");
        double weight = readDouble("새 무게: ");
        int reps = readInt("새 반복 횟수: ");
        int sets = readInt("새 세트 수: ");
        LocalDate workoutDate = readDate(
                "새 운동 날짜(yyyy-MM-dd, 엔터 시 기존 날짜 " + existingRecord.getWorkoutDate() + "): ",
                existingRecord.getWorkoutDate()
        );
        String memo = readString("새 메모: ");

        WorkoutRecord updatedRecord = new WorkoutRecord(
                id,
                exerciseName,
                weight,
                reps,
                sets,
                workoutDate,
                memo
        );

        boolean result = service.updateRecord(updatedRecord);

        if (result) {
            System.out.println("운동 기록이 수정되었습니다.");
            printRecordDetails(updatedRecord);
        } else {
            System.out.println("수정할 운동 기록을 찾지 못했습니다.");
        }
    }

    private void deleteRecord() {
        System.out.println("\n[운동 기록 삭제]");

        Long id = readLong("삭제할 id: ");

        Optional<WorkoutRecord> foundRecord = service.getRecordById(id);

        if (foundRecord.isEmpty()) {
            System.out.println("해당 id의 운동 기록이 없습니다.");
            return;
        }

        System.out.println("삭제할 기록:");
        printRecordDetails(foundRecord.get());

        if (!readConfirmation("정말 삭제하시겠습니까? (y/n): ")) {
            System.out.println("삭제를 취소했습니다.");
            return;
        }

        boolean result = service.deleteRecord(id);

        if (result) {
            System.out.println("운동 기록이 삭제되었습니다.");
        } else {
            System.out.println("해당 id의 운동 기록이 없습니다.");
        }
    }

    private void searchByExerciseName() {
        System.out.println("\n[운동 이름으로 검색]");

        String keyword = readString("검색할 운동 이름: ");
        List<WorkoutRecord> records = service.searchRecordsByExerciseName(keyword);

        if (records.isEmpty()) {
            System.out.println("검색 결과가 없습니다.");
            return;
        }

        System.out.println("검색 결과 " + records.size() + "건:");

        printRecordList(records);
    }

    private void searchByDateRange() {
        System.out.println("\n[날짜 범위로 조회]");

        LocalDate startDate = readRequiredDate("시작 날짜(yyyy-MM-dd): ");
        LocalDate endDate = readRequiredDate("종료 날짜(yyyy-MM-dd): ");
        List<WorkoutRecord> records = service.searchRecordsByDateRange(startDate, endDate);

        if (records.isEmpty()) {
            System.out.println("해당 기간의 운동 기록이 없습니다.");
            return;
        }

        System.out.println("조회 결과 " + records.size() + "건:");

        printRecordList(records);
    }

    private void showStatistics() {
        System.out.println("\n[운동 통계]");

        WorkoutStatistics statistics = service.getStatistics();

        if (statistics.totalRecordCount() == 0) {
            System.out.println("통계를 계산할 운동 기록이 없습니다.");
            return;
        }

        System.out.println("[전체]");
        System.out.println("총 기록 수: " + statistics.totalRecordCount() + "건");
        System.out.println("총 세트 수: " + statistics.totalSets() + "세트");
        System.out.printf("총 볼륨: %,.1fkg%n", statistics.totalVolume());

        System.out.println("\n[운동별]");

        for (ExerciseStatistics exercise : statistics.exerciseStatistics()) {
            System.out.println("- " + exercise.exerciseName());
            System.out.println("  기록 수: " + exercise.recordCount() + "건");
            System.out.printf("  최대 무게: %,.1fkg%n", exercise.maxWeight());
            System.out.println("  총 세트 수: " + exercise.totalSets() + "세트");
            System.out.printf("  누적 볼륨: %,.1fkg%n", exercise.totalVolume());
        }
    }

    private void printRecordList(List<WorkoutRecord> records) {
        System.out.println(RECORD_SEPARATOR);

        for (WorkoutRecord record : records) {
            System.out.printf(
                    "ID %d | %s | %s | %,.1fkg | %d회 × %d세트 | 볼륨 %,.1fkg%n",
                    record.getId(),
                    record.getWorkoutDate(),
                    record.getExerciseName(),
                    record.getWeight(),
                    record.getReps(),
                    record.getSets(),
                    record.calculateVolume()
            );
            System.out.println("메모: " + displayMemo(record));
            System.out.println(RECORD_SEPARATOR);
        }
    }

    private void printRecordDetails(WorkoutRecord record) {
        System.out.println(RECORD_SEPARATOR);
        System.out.println("ID: " + record.getId());
        System.out.println("운동 이름: " + record.getExerciseName());
        System.out.printf("무게: %,.1fkg%n", record.getWeight());
        System.out.println("반복 횟수: " + record.getReps() + "회");
        System.out.println("세트 수: " + record.getSets() + "세트");
        System.out.println("운동 날짜: " + record.getWorkoutDate());
        System.out.printf("운동 볼륨: %,.1fkg%n", record.calculateVolume());
        System.out.println("메모: " + displayMemo(record));
        System.out.println(RECORD_SEPARATOR);
    }

    private String displayMemo(WorkoutRecord record) {
        String memo = record.getMemo();
        return memo == null || memo.isBlank() ? "없음" : memo;
    }

    private String readString(String message) {
        System.out.print(message);
        return scanner.nextLine().trim();
    }

    private int readInt(String message) {
        while (true) {
            try {
                System.out.print(message);
                int value = Integer.parseInt(scanner.nextLine());
                return value;
            } catch (NumberFormatException e) {
                System.out.println("숫자를 입력해주세요.");
            }
        }
    }

    private long readLong(String message) {
        while (true) {
            try {
                System.out.print(message);
                long value = Long.parseLong(scanner.nextLine());
                return value;
            } catch (NumberFormatException e) {
                System.out.println("숫자를 입력해주세요.");
            }
        }
    }

    private double readDouble(String message) {
        while (true) {
            try {
                System.out.print(message);
                double value = Double.parseDouble(scanner.nextLine());
                return value;
            } catch (NumberFormatException e) {
                System.out.println("숫자를 입력해주세요.");
            }
        }
    }

    private LocalDate readDate(String message, LocalDate defaultDate) {
        while (true) {
            System.out.print(message);
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                return defaultDate;
            }

            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException e) {
                System.out.println("날짜는 yyyy-MM-dd 형식으로 입력해주세요.");
            }
        }
    }

    private LocalDate readRequiredDate(String message) {
        while (true) {
            System.out.print(message);
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("날짜를 입력해주세요.");
                continue;
            }

            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException e) {
                System.out.println("날짜는 yyyy-MM-dd 형식으로 입력해주세요.");
            }
        }
    }

    private boolean readConfirmation(String message) {
        while (true) {
            String input = readString(message);

            if (input.equalsIgnoreCase("y")) {
                return true;
            }

            if (input.equalsIgnoreCase("n")) {
                return false;
            }

            System.out.println("y 또는 n을 입력해주세요.");
        }
    }
}
